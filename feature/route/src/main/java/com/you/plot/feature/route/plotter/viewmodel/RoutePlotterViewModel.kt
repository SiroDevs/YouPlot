package com.you.plot.feature.route.plotter.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.common.entity.DestinationMode
import com.you.plot.core.common.entity.PlotterStage
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.domain.entity.Waypoint
import com.you.plot.core.domain.usecase.route.DeleteRouteUseCase
import com.you.plot.core.domain.usecase.route.SaveRouteUseCase
import com.you.plot.feature.route.list.viewmodel.RoutePlotterUiState
import com.you.plot.core.data.repos.RoutePlotterRepo
import com.you.plot.core.common.utils.buildWaypointSuggestions
import com.you.plot.core.common.utils.destinationPoint
import com.you.plot.core.common.utils.bearingLabel
import com.you.plot.core.common.utils.fmt
import com.you.plot.core.domain.entity.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.firstOrNull
import kotlin.coroutines.resume

@HiltViewModel
class RoutePlotterViewModel @Inject constructor(
    private val saveRouteUseCase: SaveRouteUseCase,
    private val deleteRouteUseCase: DeleteRouteUseCase,
    private val repo: RoutePlotterRepo,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(RoutePlotterUiState())
    val state: StateFlow<RoutePlotterUiState> = _state.asStateFlow()
    private var searchJob: Job? = null

    fun advanceStage() {
        val s = _state.value
        when (s.stage) {
            PlotterStage.STAGE_1 -> {
                if (s.startPoint == null) { setError("Set a start point first"); return }
                _state.update { it.copy(stage = PlotterStage.STAGE_2, searchQuery = "", searchResults = emptyList()) }
            }
            PlotterStage.STAGE_2 -> {
                when (s.destinationMode) {
                    DestinationMode.PICK_POINT ->
                        if (s.endPoint == null) { setError("Set a destination on the map or search"); return }
                    DestinationMode.TARGET_DISTANCE ->
                        if (s.endPoint == null) { setError("Select one of the suggested destinations"); return }
                }
                val start = s.startPoint!!
                val end = s.endPoint!!
                val suggested = buildWaypointSuggestions(start, end)
                _state.update { it.copy(
                    stage = PlotterStage.STAGE_3,
                    suggestedWaypoints = suggested,
                    searchQuery = "",
                    searchResults = emptyList(),
                ) }
            }
            PlotterStage.STAGE_3 -> {
                _state.update { it.copy(stage = PlotterStage.STAGE_4, routeCandidates = emptyList()) }
                fetchRouteCandidates()
            }
            PlotterStage.STAGE_4 -> {
                if (s.selectedCandidate == null) { setError("Select a route to continue"); return }
                _state.update { it.copy(stage = PlotterStage.STAGE_5) }
            }
            PlotterStage.STAGE_5 -> saveRoute()
        }
    }

    fun goBack() {
        val prev = when (_state.value.stage) {
            PlotterStage.STAGE_1 -> null
            PlotterStage.STAGE_2 -> PlotterStage.STAGE_1
            PlotterStage.STAGE_3 -> PlotterStage.STAGE_2
            PlotterStage.STAGE_4 -> PlotterStage.STAGE_3
            PlotterStage.STAGE_5 -> PlotterStage.STAGE_4
        }
        if (prev != null) _state.update { it.copy(stage = prev) }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query, searchResults = emptyList()) }
        if (query.length < 3) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            _state.update { it.copy(isSearching = true) }
            val results = repo.searchLocations(query)
            _state.update { it.copy(isSearching = false, searchResults = results) }
        }
    }

    fun onSearchResultSelected(result: SearchResult) {
        when (_state.value.stage) {
            PlotterStage.STAGE_1 -> _state.update {
                it.copy(startPoint = result.latLng, startPointName = result.displayName.substringBefore(",").trim(), searchQuery = result.displayName, searchResults = emptyList())
            }
            PlotterStage.STAGE_2 -> _state.update {
                it.copy(endPoint = result.latLng, endPointName = result.displayName.substringBefore(",").trim(), searchQuery = result.displayName, searchResults = emptyList())
            }
            else -> {}
        }
    }

    fun onMapTap(latLng: LatLng) {
        when (_state.value.stage) {
            PlotterStage.STAGE_1 -> _state.update {
                it.copy(startPoint = latLng, searchQuery = "${latLng.latitude.fmt()}, ${latLng.longitude.fmt()}")
            }
            PlotterStage.STAGE_2 -> {
                if (_state.value.destinationMode == DestinationMode.PICK_POINT) {
                    _state.update { it.copy(endPoint = latLng) }
                }
            }
            PlotterStage.STAGE_3 -> _state.update { it.copy(manualWaypoints = it.manualWaypoints + latLng) }
            else -> {}
        }
    }

    fun onWaypointMoved(index: Int, newLatLng: LatLng) {
        _state.update {
            val updated = it.manualWaypoints.toMutableList()
            if (index in updated.indices) updated[index] = newLatLng
            it.copy(manualWaypoints = updated)
        }
    }

    fun removeManualWaypoint(index: Int) {
        _state.update {
            it.copy(manualWaypoints = it.manualWaypoints.toMutableList().also { l -> l.removeAt(index) })
        }
    }

    @SuppressLint("MissingPermission")
    fun onUseMyLocation() {
        // Check for location permission before accessing location services
        val hasPermission = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            setError("Location permission is required. Please grant it in Settings.")
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true) }
            val latLng = withContext(Dispatchers.IO) { resolveCurrentLocation() }
            _state.update { it.copy(isSearching = false) }
            if (latLng == null) {
                setError("Couldn't get your location. Make sure location is enabled.")
                return@launch
            }
            when (_state.value.stage) {
                PlotterStage.STAGE_1 -> _state.update {
                    it.copy(startPoint = latLng, startPointName = "My Location", searchQuery = "${latLng.latitude.fmt()}, ${latLng.longitude.fmt()}", searchResults = emptyList())
                }
                PlotterStage.STAGE_2 -> _state.update { it.copy(endPoint = latLng, endPointName = "My Location", searchResults = emptyList()) }
                else -> {}
            }
        }
    }

    fun setDestinationMode(mode: DestinationMode) =
        _state.update { it.copy(destinationMode = mode, endPoint = null, distanceSuggestions = emptyList()) }

    fun onTargetDistanceChange(text: String) {
        _state.update {
            it.copy(
                targetDistanceQuery = text,
                targetDistanceKm = text.toDoubleOrNull() ?: it.targetDistanceKm,
            )
        }
    }

    fun suggestDestinationsForDistance() {
        val s = _state.value
        val start = s.startPoint ?: return
        val suggestions = listOf(0.0, 45.0, 90.0, 135.0).map { b ->
            SearchResult(
                displayName = bearingLabel(b) + " (≈ ${s.targetDistanceKm.toInt()} km)",
                latLng = destinationPoint(start, b, s.targetDistanceKm),
            )
        }
        _state.update { it.copy(distanceSuggestions = suggestions) }
    }

    fun selectDistanceSuggestion(result: SearchResult) =
        _state.update { it.copy(endPoint = result.latLng, distanceSuggestions = emptyList()) }
    fun toggleSuggestedWaypoints(use: Boolean) = _state.update { it.copy(useSuggestedWaypoints = use) }
    fun selectCandidate(id: Int) = _state.update { it.copy(selectedCandidateId = id) }

    private fun fetchRouteCandidates() {
        val s = _state.value
        val start = s.startPoint ?: return
        val end = s.endPoint ?: return
        val via = s.activeWaypoints

        viewModelScope.launch {
            _state.update { it.copy(isSearching = true) }
            val candidates = repo.fetchRouteCandidates(start, end, via)
            _state.update {
                it.copy(
                    isSearching = false,
                    routeCandidates = candidates,
                    selectedCandidateId = candidates.firstOrNull()?.id,
                )
            }
        }
    }

    fun setRoundTrip(round: Boolean) = _state.update { it.copy(isRoundTrip = round) }
    fun setName(name: String) = _state.update { it.copy(name = name) }
    fun setDescription(desc: String) = _state.update { it.copy(description = desc) }
    fun setSportType(type: SportType) = _state.update { it.copy(sportType = type) }
    fun deleteRoute(id: Long) = viewModelScope.launch { deleteRouteUseCase(id) }

    private fun saveRoute() {
        val s = _state.value
        val candidate = s.selectedCandidate ?: run { setError("No route selected"); return }
        val start = s.startPoint ?: run { setError("Start point missing"); return }
        val end = s.endPoint ?: run { setError("Destination missing"); return }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                val allPoints = buildList {
                    add(start); addAll(s.activeWaypoints); add(end)
                    if (s.isRoundTrip) add(start)
                }
                val waypointEntities = allPoints.mapIndexed { index, latLng ->
                    Waypoint(
                        routeId = 0L,
                        name = when (index) {
                            0 -> "Start"
                            allPoints.lastIndex -> if (s.isRoundTrip) "Return to Start" else "Finish"
                            else -> "Waypoint $index"
                        },
                        position = latLng,
                        orderIndex = index,
                        isStopPlanned = index != 0 && index != allPoints.lastIndex,
                    )
                }
                // Auto-name from start and end point names if user hasn't typed anything
                val autoName = if (s.startPointName.isNotBlank() && s.endPointName.isNotBlank()) {
                    "${s.startPointName} → ${s.endPointName}"
                } else if (s.startPointName.isNotBlank()) {
                    "${s.startPointName} Route"
                } else {
                    "New Route"
                }
                saveRouteUseCase(
                    Route(
                        name = s.name.ifBlank { autoName },
                        description = s.description,
                        sportType = s.sportType,
                        startPoint = start,
                        endPoint = end,
                        waypoints = waypointEntities,
                        elevationProfile = candidate.elevationProfile,
                        totalDistanceKm = if (s.isRoundTrip) candidate.totalDistanceKm * 2 else candidate.totalDistanceKm,
                        totalElevationGainMeters = candidate.totalElevationGainMeters,
                        totalElevationLossMeters = candidate.totalElevationLossMeters,
                        isRoundTrip = s.isRoundTrip,
                    ),
                )
            }.onSuccess { id -> _state.update { it.copy(isSaving = false, savedRouteId = id) } }
                .onFailure { e -> _state.update { it.copy(isSaving = false, error = e.message) } }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
    private fun setError(msg: String) = _state.update { it.copy(error = msg) }

    @SuppressLint("MissingPermission")
    private suspend fun resolveCurrentLocation(): LatLng? = withContext(Dispatchers.IO) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
        if (providers.isEmpty()) return@withContext null

        val twoMinMs = 2 * 60 * 1000L
        val fresh = providers.firstNotNullOfOrNull { p ->
            lm.getLastKnownLocation(p)?.takeIf { System.currentTimeMillis() - it.time < twoMinMs }
        }
        if (fresh != null) return@withContext LatLng(fresh.latitude, fresh.longitude)

        val live = suspendCancellableCoroutine<android.location.Location?> { cont ->
            val listener = object : android.location.LocationListener {
                override fun onLocationChanged(loc: android.location.Location) {
                    lm.removeUpdates(this); if (cont.isActive) cont.resume(loc)
                }
                @Deprecated("Deprecated in API 29")
                override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) {}
            }
            try {
                lm.requestLocationUpdates(providers.first(), 0L, 0f, listener, android.os.Looper.getMainLooper())
                cont.invokeOnCancellation { lm.removeUpdates(listener) }
                viewModelScope.launch {
                    delay(5_000); lm.removeUpdates(listener); if (cont.isActive) cont.resume(null)
                }
            } catch (_: SecurityException) { cont.resume(null) }
        }
        live?.let { LatLng(it.latitude, it.longitude) }
            ?: providers.firstNotNullOfOrNull { lm.getLastKnownLocation(it) }?.let { LatLng(it.latitude, it.longitude) }
    }
}
