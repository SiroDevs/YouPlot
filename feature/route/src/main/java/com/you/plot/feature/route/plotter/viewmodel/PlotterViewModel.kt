package com.you.plot.feature.route.plotter.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.common.entity.DestinationMode
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.PlotterStage
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.common.utils.buildWaypointSuggestions
import com.you.plot.core.data.repos.PlotterRepo
import com.you.plot.core.data.service.CurrentLocationResolver
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.entity.Waypoint
import com.you.plot.core.domain.entity.WaypointSearchResult
import com.you.plot.core.domain.usecase.route.DeleteRouteUseCase
import com.you.plot.core.domain.usecase.route.SaveRouteUseCase
import com.you.plot.core.domain.usecase.startpoint.GetStartPointByIdUseCase
import com.you.plot.core.domain.usecase.startpoint.RecordStartPointUsageUseCase
import com.you.plot.feature.route.plotter.utils.PlotterUiState
import com.you.plot.feature.route.plotter.utils.PlotterUtils
import com.you.plot.feature.route.plotter.utils.buildRouteWaypoints
import com.you.plot.feature.route.plotter.utils.deriveAutoRouteName
import com.you.plot.feature.route.plotter.utils.hasLocationPermission
import com.you.plot.feature.route.plotter.utils.previousStage
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
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlotterViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveRouteUseCase: SaveRouteUseCase,
    private val deleteRouteUseCase: DeleteRouteUseCase,
    private val plotterRepo: PlotterRepo,
    private val getStartPointById: GetStartPointByIdUseCase,
    private val recordStartPointUsage: RecordStartPointUsageUseCase,
    private val currentLocationResolver: CurrentLocationResolver,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(PlotterUiState())
    val state: StateFlow<PlotterUiState> = _state.asStateFlow()
    private var searchJob: Job? = null

    init {
        val startPointId = savedStateHandle.get<Long>("startPointId") ?: 0L
        if (startPointId > 0L) viewModelScope.launch {
            val sp = getStartPointById(startPointId) ?: return@launch
            _state.update {
                it.copy(
                    startPoint = sp.position,
                    startPointName = sp.name,
                    searchQuery = sp.name,
                    selectedCtryCode = sp.countryCode.ifBlank { it.selectedCtryCode },
                )
            }
        }
    }

    fun advanceStage() {
        val s = _state.value
        when (s.stage) {
            PlotterStage.STAGE_1 -> {
                if (s.startPoint == null) {
                    setError("Set a start point first"); return
                }
                _state.update {
                    it.copy(
                        stage = PlotterStage.STAGE_2,
                        searchQuery = "",
                        searchResults = emptyList(),
                    )
                }
            }

            PlotterStage.STAGE_2 -> {
                when (s.destinationMode) {
                    DestinationMode.PICK_POINT ->
                        if (s.endPoint == null) {
                            setError("Set a destination on the map or search"); return
                        }

                    DestinationMode.TARGET_DISTANCE ->
                        if (s.endPoint == null) {
                            setError("Select one of the suggested destinations"); return
                        }
                }
                val start = s.startPoint!!
                val end = s.endPoint!!
                val suggested = buildWaypointSuggestions(start, end)
                _state.update {
                    it.copy(
                        stage = PlotterStage.STAGE_3,
                        suggestedWaypoints = suggested,
                        searchQuery = "",
                        searchResults = emptyList(),
                    )
                }
            }

            PlotterStage.STAGE_3 -> {
                _state.update {
                    it.copy(
                        stage = PlotterStage.STAGE_4,
                        routeCandidates = emptyList(),
                    )
                }
                fetchRouteCandidates()
            }

            PlotterStage.STAGE_4 -> {
                if (s.selectedCandidate == null) {
                    setError("Select a route to continue"); return
                }
                _state.update { it.copy(stage = PlotterStage.STAGE_5) }
            }

            PlotterStage.STAGE_5 -> saveRoute()
        }
    }

    fun goBack() {
        val prev = _state.value.stage.previousStage()
        if (prev != null) _state.update { it.copy(stage = prev) }
    }

    fun setCountryCode(code: String) = _state.update { it.copy(selectedCtryCode = code) }

    fun onQryClear() {
        _state.update { it.copy(searchQuery = "", searchResults = emptyList()) }
        searchJob?.cancel()
    }

    fun onSearch(query: String) {
        _state.update { it.copy(searchQuery = query, searchResults = emptyList()) }
        if (query.length < 3) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            _state.update { it.copy(isSearching = true) }
            val results = performSearch(query)
            _state.update { it.copy(isSearching = false, searchResults = results) }
        }
    }

    private suspend fun performSearch(query: String): List<WaypointSearchResult> {
        val cc = _state.value.selectedCtryCode
        return if (cc.isNotBlank()) plotterRepo.searchLocationsWithCountry(query, cc)
        else plotterRepo.searchLocations(query)
    }

    fun onWaypointSearchResultSelected(result: WaypointSearchResult) {
        when (_state.value.stage) {
            PlotterStage.STAGE_1 -> _state.update {
                it.copy(
                    startPoint = result.latLng,
                    startPointName = result.displayName.substringBefore(",").trim(),
                    searchQuery = result.displayName,
                    searchResults = emptyList(),
                )
            }

            PlotterStage.STAGE_2 -> _state.update {
                it.copy(
                    endPoint = result.latLng,
                    endPointName = result.displayName.substringBefore(",").trim(),
                    searchQuery = result.displayName,
                    searchResults = emptyList(),
                )
            }

            else -> {}
        }
    }

    fun onMapTap(latLng: LatLng) {
        when (_state.value.stage) {
            PlotterStage.STAGE_1 -> handleStartPointTap(latLng)
            PlotterStage.STAGE_2 -> handleEndPointTap(latLng)
            PlotterStage.STAGE_3 -> _state.update { it.copy(manualWaypoints = it.manualWaypoints + latLng) }
            else -> {}
        }
    }

    private fun handleStartPointTap(latLng: LatLng) {
        _state.update {
            it.copy(
                startPoint = latLng,
                startPointName = "Locating ...",
                isReverseGeocoding = true,
            )
        }
        viewModelScope.launch {
            val name = resolveLocationName(latLng)
            _state.update {
                it.copy(
                    startPointName = name,
                    searchQuery = name,
                    isReverseGeocoding = false,
                )
            }
        }
    }

    private fun handleEndPointTap(latLng: LatLng) {
        if (_state.value.destinationMode == DestinationMode.PICK_POINT) {
            _state.update {
                it.copy(
                    endPoint = latLng,
                    endPointName = "Locating ...",
                    isReverseGeocoding = true,
                )
            }
            viewModelScope.launch {
                val name = resolveLocationName(latLng)
                _state.update { it.copy(endPointName = name, isReverseGeocoding = false) }
            }
        }
    }

    private suspend fun resolveLocationName(latLng: LatLng): String = withContext(Dispatchers.IO) {
        plotterRepo.reverseGeocode(latLng) ?: plotterRepo.resolveAreaLabel(latLng)
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
            it.copy(
                manualWaypoints = it.manualWaypoints.toMutableList()
                    .also { l -> l.removeAt(index) },
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun onUseMyLocation() {
        if (!context.hasLocationPermission()) {
            _state.update { it.copy(needsLocationPermission = true) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true) }
            val latLng = currentLocationResolver.resolve()
            if (latLng == null) {
                _state.update { it.copy(isSearching = false) }
                setError("Couldn't get your location. Make sure location is enabled.")
                return@launch
            }
            _state.update { it.copy(isReverseGeocoding = true) }
            val geocodedName = withContext(Dispatchers.IO) { plotterRepo.reverseGeocode(latLng) }
            val name = geocodedName ?: plotterRepo.resolveAreaLabel(latLng)
            _state.update { it.copy(isSearching = false, isReverseGeocoding = false) }
            when (_state.value.stage) {
                PlotterStage.STAGE_1 -> _state.update {
                    it.copy(
                        startPoint = latLng, startPointName = name,
                        searchQuery = name, searchResults = emptyList(),
                    )
                }

                PlotterStage.STAGE_2 -> _state.update {
                    it.copy(endPoint = latLng, endPointName = name, searchResults = emptyList())
                }

                else -> {}
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(needsLocationPermission = false) }
        if (granted) onUseMyLocation()
        else setError("Location permission denied. Enable it in System Settings → Apps → YouPlot.")
    }

    fun clearNeedsPermission() = _state.update { it.copy(needsLocationPermission = false) }

    fun setDestinationMode(mode: DestinationMode) =
        _state.update {
            it.copy(
                destinationMode = mode,
                endPoint = null,
                distSuggestions = emptyList(),
            )
        }

    fun onTargetDistanceChange(text: String) {
        _state.update {
            it.copy(
                targetDistQry = text,
                targetDist = text.toDoubleOrNull() ?: it.targetDist,
            )
        }
    }

    fun suggestDestinationsForDistance() {
        val s = _state.value
        val start = s.startPoint ?: return
        val suggestions = PlotterUtils.buildDistanceSuggestions(start, s.targetDist)
        _state.update { it.copy(distSuggestions = suggestions) }
    }

    fun selectDistanceSuggestion(result: WaypointSearchResult) =
        _state.update { it.copy(endPoint = result.latLng, distSuggestions = emptyList()) }

    fun toggleSuggestedWaypoints(use: Boolean) =
        _state.update { it.copy(useSuggestedWaypoints = use) }

    fun selectCandidate(id: Int) = _state.update { it.copy(selectedCandidateId = id) }

    private fun fetchRouteCandidates() {
        val s = _state.value
        val start = s.startPoint ?: return
        val end = s.endPoint ?: return
        val via = s.activeWaypoints

        viewModelScope.launch {
            _state.update { it.copy(isSearching = true) }
            val candidates = plotterRepo.fetchRouteCandidates(start, end, via)
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
                val totalDist =
                    if (s.isRoundTrip) candidate.totalDist * 2 else candidate.totalDist
                val waypointEntities = buildRouteWaypoints(
                    start = start,
                    end = end,
                    intermediates = s.activeWaypoints,
                    isRoundTrip = s.isRoundTrip,
                    totalDist = totalDist,
                    startName = s.startPointName,
                    endName = s.endPointName,
                    countryCode = s.selectedCtryCode,
                    resolveName = { latLng ->
                        withContext(Dispatchers.IO) { plotterRepo.reverseGeocode(latLng) }
                    },
                )
                val autoName = deriveAutoRouteName(s.startPointName, s.endPointName)
                val routeId = saveRouteUseCase(
                    Route(
                        name = s.name.ifBlank { autoName },
                        description = s.description,
                        sportType = s.sportType,
                        startPoint = start,
                        endPoint = end,
                        waypoints = waypointEntities,
                        elevationProfile = candidate.elevationProfile,
                        totalDist = totalDist,
                        elevationGain = candidate.elevationGain,
                        elevationLoss = candidate.elevationLoss,
                        isRoundTrip = s.isRoundTrip,
                        polyline = candidate.waypoints,
                    ),
                )
                recordStartPointUsage(
                    name = s.startPointName.ifBlank { "Start" },
                    position = start,
                    countryCode = s.selectedCtryCode,
                )
                routeId
            }.onSuccess { id -> _state.update { it.copy(isSaving = false, savedRouteId = id) } }
                .onFailure { e -> _state.update { it.copy(isSaving = false, error = e.message) } }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
    private fun setError(msg: String) = _state.update { it.copy(error = msg) }
}
