package com.you.plot.feature.route.plotter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.domain.entity.ElevationPoint
import com.you.plot.core.domain.entity.LatLng
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.entity.SportType
import com.you.plot.core.domain.entity.Waypoint
import com.you.plot.core.domain.usecase.route.DeleteRouteUseCase
import com.you.plot.core.domain.usecase.route.SaveRouteUseCase
import com.you.plot.feature.route.list.viewmodel.DestinationMode
import com.you.plot.feature.route.list.viewmodel.PlotterStage
import com.you.plot.feature.route.list.viewmodel.RouteCandidate
import com.you.plot.feature.route.list.viewmodel.RoutePlotterUiState
import com.you.plot.feature.route.list.viewmodel.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private val CANDIDATE_COLORS = listOf(0xFF2196F3L, 0xFFE91E63L, 0xFF4CAF50L, 0xFFFF9800L)

@HiltViewModel
class RoutePlotterViewModel @Inject constructor(
    private val saveRouteUseCase: SaveRouteUseCase,
    private val deleteRouteUseCase: DeleteRouteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RoutePlotterUiState())
    val state: StateFlow<RoutePlotterUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

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
                        searchResults = emptyList()
                    )
                }
            }

            PlotterStage.STAGE_2 -> {
                when (s.destinationMode) {
                    DestinationMode.PICK_POINT -> {
                        if (s.endPoint == null) {
                            setError("Set a destination on the map or search"); return
                        }
                    }

                    DestinationMode.TARGET_DISTANCE -> {
                        if (s.endPoint == null) {
                            setError("Select one of the suggested destinations"); return
                        }
                    }
                }
                buildWaypointSuggestions()
                _state.update { it.copy(stage = PlotterStage.STAGE_3) }
            }

            PlotterStage.STAGE_3 -> {
                buildRouteCandidates()
                _state.update { it.copy(stage = PlotterStage.STAGE_4) }
            }

            PlotterStage.STAGE_4 -> {
                if (s.selectedCandidate == null) {
                    setError("Select a route to continue"); return
                }
                _state.update { it.copy(stage = PlotterStage.STAGE_5) }
            }

            PlotterStage.STAGE_5 -> {
                _state.update { it.copy(stage = PlotterStage.STAGE_6) }
            }

            PlotterStage.STAGE_6 -> saveRoute()
        }
    }

    fun goBack() {
        val prev = when (_state.value.stage) {
            PlotterStage.STAGE_1 -> null
            PlotterStage.STAGE_2 -> PlotterStage.STAGE_1
            PlotterStage.STAGE_3 -> PlotterStage.STAGE_2
            PlotterStage.STAGE_4 -> PlotterStage.STAGE_3
            PlotterStage.STAGE_5 -> PlotterStage.STAGE_4
            PlotterStage.STAGE_6 -> PlotterStage.STAGE_5
        }
        if (prev != null) _state.update { it.copy(stage = prev) }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query, searchResults = emptyList()) }
        if (query.length < 3) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400) // debounce
            _state.update { it.copy(isSearching = true) }
            val results = nominatimSearch(query)
            _state.update { it.copy(isSearching = false, searchResults = results) }
        }
    }

    fun onSearchResultSelected(result: SearchResult) {
        when (_state.value.stage) {
            PlotterStage.STAGE_1 -> {
                _state.update {
                    it.copy(
                        startPoint = result.latLng,
                        searchQuery = result.displayName,
                        searchResults = emptyList(),
                    )
                }
            }

            PlotterStage.STAGE_2 -> {
                _state.update {
                    it.copy(
                        endPoint = result.latLng,
                        searchQuery = result.displayName,
                        searchResults = emptyList(),
                    )
                }
            }

            else -> {}
        }
    }

    fun onMapTap(latLng: LatLng) {
        when (_state.value.stage) {
            PlotterStage.STAGE_1 -> {
                _state.update {
                    it.copy(
                        startPoint = latLng,
                        searchQuery = "${latLng.latitude.fmt()}, ${latLng.longitude.fmt()}"
                    )
                }
            }

            PlotterStage.STAGE_2 -> {
                if (_state.value.destinationMode == DestinationMode.PICK_POINT) {
                    _state.update { it.copy(endPoint = latLng) }
                }
            }

            PlotterStage.STAGE_3 -> {
                _state.update { it.copy(manualWaypoints = it.manualWaypoints + latLng) }
            }

            else -> {}
        }
    }

    fun setDestinationMode(mode: DestinationMode) {
        _state.update {
            it.copy(
                destinationMode = mode,
                endPoint = null,
                distanceSuggestions = emptyList()
            )
        }
    }

    fun onTargetDistanceChange(text: String) {
        val km = text.toDoubleOrNull()
        _state.update {
            it.copy(
                targetDistanceQuery = text,
                targetDistanceKm = km ?: it.targetDistanceKm
            )
        }
    }

    fun suggestDestinationsForDistance() {
        val s = _state.value
        val start = s.startPoint ?: return
        viewModelScope.launch {
            val suggestions = generateDistanceSuggestions(start, s.targetDistanceKm)
            _state.update { it.copy(distanceSuggestions = suggestions) }
        }
    }

    fun selectDistanceSuggestion(result: SearchResult) {
        _state.update { it.copy(endPoint = result.latLng, distanceSuggestions = emptyList()) }
    }

    fun removeManualWaypoint(index: Int) {
        _state.update {
            it.copy(
                manualWaypoints = it.manualWaypoints.toMutableList()
                    .also { l -> l.removeAt(index) })
        }
    }

    fun toggleSuggestedWaypoints(use: Boolean) {
        _state.update { it.copy(useSuggestedWaypoints = use) }
    }

    fun selectCandidate(id: Int) {
        _state.update { it.copy(selectedCandidateId = id) }
    }

    fun setRoundTrip(round: Boolean) = _state.update { it.copy(isRoundTrip = round) }

    fun setName(name: String) = _state.update { it.copy(name = name) }
    fun setDescription(desc: String) = _state.update { it.copy(description = desc) }
    fun setSportType(type: SportType) = _state.update { it.copy(sportType = type) }

    private fun saveRoute() {
        val s = _state.value
        val candidate = s.selectedCandidate ?: run { setError("No route selected"); return }
        val start = s.startPoint ?: run { setError("Start point missing"); return }
        val end = s.endPoint ?: run { setError("Destination missing"); return }

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                val allPoints = buildList {
                    add(start)
                    addAll(s.activeWaypoints)
                    add(end)
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

                val totalDistance = if (s.isRoundTrip) candidate.totalDistanceKm * 2
                else candidate.totalDistanceKm

                val route = Route(
                    name = s.name.ifBlank { "New Route" },
                    description = s.description,
                    sportType = s.sportType,
                    startPoint = start,
                    endPoint = end,
                    waypoints = waypointEntities,
                    elevationProfile = candidate.elevationProfile,
                    totalDistanceKm = totalDistance,
                    totalElevationGainMeters = candidate.totalElevationGainMeters,
                    totalElevationLossMeters = candidate.totalElevationLossMeters,
                    isRoundTrip = s.isRoundTrip,
                )
                saveRouteUseCase(route)
            }.onSuccess { id ->
                _state.update { it.copy(isSaving = false, savedRouteId = id) }
            }.onFailure { e ->
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun deleteRoute(id: Long) = viewModelScope.launch { deleteRouteUseCase(id) }

    fun clearError() = _state.update { it.copy(error = null) }

    private fun buildRouteCandidates() {
        val s = _state.value
        val start = s.startPoint ?: return
        val end = s.endPoint ?: return

        val baseWaypoints = s.activeWaypoints
        val candidates = buildList {
            add(buildCandidate(0, listOf(start) + baseWaypoints + listOf(end)))

            if (start != end) {
                val mid = LatLng(
                    (start.latitude + end.latitude) / 2 + 0.008,
                    (start.longitude + end.longitude) / 2,
                )
                add(buildCandidate(1, listOf(start, mid) + baseWaypoints + listOf(end)))
            }

            if (start != end) {
                val mid = LatLng(
                    (start.latitude + end.latitude) / 2 - 0.008,
                    (start.longitude + end.longitude) / 2 + 0.005,
                )
                add(buildCandidate(2, listOf(start, mid) + baseWaypoints + listOf(end)))
            }
        }

        val firstId = candidates.firstOrNull()?.id
        _state.update { it.copy(routeCandidates = candidates, selectedCandidateId = firstId) }
    }

    private fun buildCandidate(id: Int, points: List<LatLng>): RouteCandidate {
        val segmentDistances = points.zipWithNext().map { (a, b) -> haversineKm(a, b) }
        val totalDist = segmentDistances.sum()

        val elevProfile = points.mapIndexed { i, _ ->
            val d = segmentDistances.take(i).sum()
            val elev = 1400.0 + 80 * sin(i.toDouble() / points.size * PI * 2)
            ElevationPoint(distanceKm = d, elevationMeters = elev)
        }

        var gain = 0.0;
        var loss = 0.0
        elevProfile.zipWithNext().forEach { (a, b) ->
            val diff = b.elevationMeters - a.elevationMeters
            if (diff > 0) gain += diff else loss += abs(diff)
        }

        return RouteCandidate(
            id = id,
            waypoints = points,
            elevationProfile = elevProfile,
            totalDistanceKm = totalDist,
            totalElevationGainMeters = gain,
            totalElevationLossMeters = loss,
            colorArgb = CANDIDATE_COLORS[id % CANDIDATE_COLORS.size],
        )
    }

    private fun buildWaypointSuggestions() {
        val s = _state.value
        val start = s.startPoint ?: return
        val end = s.endPoint ?: return

        val suggested = (1..3).map { i ->
            val t = i.toDouble() / 4.0
            LatLng(
                latitude = start.latitude + t * (end.latitude - start.latitude),
                longitude = start.longitude + t * (end.longitude - start.longitude),
            )
        }
        _state.update { it.copy(suggestedWaypoints = suggested) }
    }

    private suspend fun nominatimSearch(query: String): List<SearchResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val url =
                    "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=5"
                val json = URL(url).readText(Charsets.UTF_8)
                val arr = JSONArray(json)
                (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    SearchResult(
                        displayName = obj.getString("display_name").take(80),
                        latLng = LatLng(obj.getDouble("lat"), obj.getDouble("lon")),
                    )
                }
            }.getOrDefault(emptyList())
        }

    private suspend fun generateDistanceSuggestions(
        start: LatLng,
        targetKm: Double
    ): List<SearchResult> =
        withContext(Dispatchers.IO) {
            // Bearing offsets: N, NE, E, SE
            val bearings = listOf(0.0, 45.0, 90.0, 135.0)
            bearings.map { bearing ->
                val dest = destinationPoint(start, bearing, targetKm)
                SearchResult(
                    displayName = bearingLabel(bearing) + " (≈ ${targetKm.toInt()} km)",
                    latLng = dest,
                )
            }
        }

    private fun haversineKm(a: LatLng, b: LatLng): Double {
        val r = 6371.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLng = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLng / 2).pow(2)
        return r * 2 * atan2(sqrt(h), sqrt(1 - h))
    }

    private fun destinationPoint(start: LatLng, bearingDeg: Double, distanceKm: Double): LatLng {
        val r = 6371.0
        val d = distanceKm / r
        val brng = Math.toRadians(bearingDeg)
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.asin(sin(lat1) * cos(d) + cos(lat1) * sin(d) * cos(brng))
        val lon2 = lon1 + atan2(sin(brng) * sin(d) * cos(lat1), cos(d) - sin(lat1) * sin(lat2))
        return LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2))
    }

    private fun bearingLabel(bearing: Double) = when {
        bearing < 22.5 -> "North"
        bearing < 67.5 -> "North-East"
        bearing < 112.5 -> "East"
        bearing < 157.5 -> "South-East"
        bearing < 202.5 -> "South"
        bearing < 247.5 -> "South-West"
        bearing < 292.5 -> "West"
        bearing < 337.5 -> "North-West"
        else -> "North"
    }

    private fun Double.fmt() = "%.5f".format(this)

    private fun setError(msg: String) = _state.update { it.copy(error = msg) }
}
