package com.you.plot.feature.route.plotter.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.common.entity.DestinationMode
import com.you.plot.core.common.entity.PlotterStage
import com.you.plot.core.common.utils.AppConstants
import com.you.plot.core.domain.entity.ElevationPoint
import com.you.plot.core.domain.entity.LatLng
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.entity.SportType
import com.you.plot.core.domain.entity.Waypoint
import com.you.plot.core.domain.usecase.route.DeleteRouteUseCase
import com.you.plot.core.domain.usecase.route.SaveRouteUseCase
import com.you.plot.feature.route.list.viewmodel.RouteCandidate
import com.you.plot.feature.route.list.viewmodel.RoutePlotterUiState
import com.you.plot.feature.route.list.viewmodel.SearchResult
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
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.coroutines.resume

private val CANDIDATE_COLORS = listOf(0xFF2196F3L, 0xFFE91E63L, 0xFF4CAF50L, 0xFFFF9800L)

@HiltViewModel
class RoutePlotterViewModel @Inject constructor(
    private val saveRouteUseCase: SaveRouteUseCase,
    private val deleteRouteUseCase: DeleteRouteUseCase,
    @ApplicationContext private val context: Context,
    @Named("osm_user_agent") private val osmUserAgent: String
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
                buildWaypointSuggestions()
                _state.update { it.copy(stage = PlotterStage.STAGE_3) }
            }
            PlotterStage.STAGE_3 -> {
                _state.update { it.copy(stage = PlotterStage.STAGE_4, routeCandidates = emptyList()) }
                fetchRouteCandidates()
            }
            PlotterStage.STAGE_4 -> {
                if (s.selectedCandidate == null) { setError("Select a route to continue"); return }
                _state.update { it.copy(stage = PlotterStage.STAGE_5) }
            }
            PlotterStage.STAGE_5 -> _state.update { it.copy(stage = PlotterStage.STAGE_6) }
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
            delay(400)
            _state.update { it.copy(isSearching = true) }
            val results = nominatimSearch(query)
            _state.update { it.copy(isSearching = false, searchResults = results) }
        }
    }

    fun onSearchResultSelected(result: SearchResult) {
        when (_state.value.stage) {
            PlotterStage.STAGE_1 -> _state.update {
                it.copy(startPoint = result.latLng, searchQuery = result.displayName, searchResults = emptyList())
            }
            PlotterStage.STAGE_2 -> _state.update {
                it.copy(endPoint = result.latLng, searchQuery = result.displayName, searchResults = emptyList())
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
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true) }
            val latLng = withContext(Dispatchers.IO) { resolveCurrentLocation() }
            _state.update { it.copy(isSearching = false) }

            if (latLng == null) {
                setError("Couldn't get your location. Check that location is enabled.")
                return@launch
            }
            when (_state.value.stage) {
                PlotterStage.STAGE_1 -> _state.update {
                    it.copy(startPoint = latLng, searchQuery = "${latLng.latitude.fmt()}, ${latLng.longitude.fmt()}", searchResults = emptyList())
                }
                PlotterStage.STAGE_2 -> _state.update { it.copy(endPoint = latLng, searchResults = emptyList()) }
                else -> {}
            }
        }
    }

    fun setDestinationMode(mode: DestinationMode) =
        _state.update { it.copy(destinationMode = mode, endPoint = null, distanceSuggestions = emptyList()) }

    fun onTargetDistanceChange(text: String) {
        _state.update { it.copy(targetDistanceQuery = text, targetDistanceKm = text.toDoubleOrNull() ?: it.targetDistanceKm) }
    }

    fun suggestDestinationsForDistance() {
        val s = _state.value
        val start = s.startPoint ?: return
        viewModelScope.launch {
            val suggestions = generateDistanceSuggestions(start, s.targetDistanceKm)
            _state.update { it.copy(distanceSuggestions = suggestions) }
        }
    }

    fun selectDistanceSuggestion(result: SearchResult) =
        _state.update { it.copy(endPoint = result.latLng, distanceSuggestions = emptyList()) }

    fun toggleSuggestedWaypoints(use: Boolean) = _state.update { it.copy(useSuggestedWaypoints = use) }
    fun selectCandidate(id: Int) = _state.update { it.copy(selectedCandidateId = id) }
    fun setRoundTrip(round: Boolean) = _state.update { it.copy(isRoundTrip = round) }
    fun setName(name: String) = _state.update { it.copy(name = name) }
    fun setDescription(desc: String) = _state.update { it.copy(description = desc) }
    fun setSportType(type: SportType) = _state.update { it.copy(sportType = type) }
    fun deleteRoute(id: Long) = viewModelScope.launch { deleteRouteUseCase(id) }
    fun clearError() = _state.update { it.copy(error = null) }

    private fun fetchRouteCandidates() {
        val s = _state.value
        val start = s.startPoint ?: return
        val end = s.endPoint ?: return
        val via = s.activeWaypoints

        viewModelScope.launch {
            _state.update { it.copy(isSearching = true) }

            val allPoints = listOf(start) + via + listOf(end)
            val coordStr = allPoints.joinToString(";") { "${it.longitude},${it.latitude}" }
            val url = "${AppConstants.OSRM_ROUTER}/$coordStr" +
                "?overview=full&geometries=polyline6&alternatives=true&steps=false"

            val candidates = withContext(Dispatchers.IO) {
                runCatching {

                    val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                        setRequestProperty("User-Agent", osmUserAgent)
                        connectTimeout = 10_000
                        readTimeout = 10_000
                    }
                    val json = JSONObject(conn.inputStream.bufferedReader().readText())
                    conn.disconnect()

                    if (json.getString("code") != "Ok") return@runCatching emptyList()

                    val routes = json.getJSONArray("routes")
                    (0 until routes.length()).map { i ->
                        val route = routes.getJSONObject(i)
                        val geometry = route.getString("geometry")
                        val decodedPoints = decodePolyline6(geometry)
                        val distKm = route.getDouble("distance") / 1000.0

                        val elevProfile = decodedPoints.mapIndexed { idx, _ ->
                            val d = distKm * idx.toDouble() / decodedPoints.size
                            ElevationPoint(distanceKm = d, elevationMeters = 1400.0 + 80 * sin(idx.toDouble() / decodedPoints.size * PI * 2))
                        }

                        var gain = 0.0; var loss = 0.0
                        elevProfile.zipWithNext().forEach { (a, b) ->
                            val diff = b.elevationMeters - a.elevationMeters
                            if (diff > 0) gain += diff else loss += abs(diff)
                        }

                        RouteCandidate(
                            id = i,
                            waypoints = decodedPoints,
                            elevationProfile = elevProfile,
                            totalDistanceKm = distKm,
                            totalElevationGainMeters = gain,
                            totalElevationLossMeters = loss,
                            colorArgb = CANDIDATE_COLORS[i % CANDIDATE_COLORS.size],
                        )
                    }
                }.getOrElse { e ->
                    e.printStackTrace()
                    buildFallbackCandidates(start, end, via)
                }
            }

            _state.update {
                it.copy(
                    isSearching = false,
                    routeCandidates = candidates,
                    selectedCandidateId = candidates.firstOrNull()?.id,
                )
            }
        }
    }

    private fun decodePolyline6(encoded: String): List<LatLng> {
        val result = mutableListOf<LatLng>()
        var index = 0; var lat = 0; var lng = 0
        while (index < encoded.length) {
            var shift = 0; var b: Int; var result5 = 0
            do { b = encoded[index++].code - 63; result5 = result5 or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
            lat += if (result5 and 1 != 0) (result5 shr 1).inv() else result5 shr 1
            shift = 0; result5 = 0
            do { b = encoded[index++].code - 63; result5 = result5 or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
            lng += if (result5 and 1 != 0) (result5 shr 1).inv() else result5 shr 1
            result.add(LatLng(lat / 1e6, lng / 1e6))
        }
        return result
    }

    private fun buildFallbackCandidates(start: LatLng, end: LatLng, via: List<LatLng>): List<RouteCandidate> {
        val base = listOf(start) + via + listOf(end)
        return listOf(
            buildFallbackCandidate(0, base),
            buildFallbackCandidate(1, listOf(start) + listOf(LatLng((start.latitude + end.latitude) / 2 + 0.008, (start.longitude + end.longitude) / 2)) + via + listOf(end)),
        )
    }

    private fun buildFallbackCandidate(id: Int, points: List<LatLng>): RouteCandidate {
        val segs = points.zipWithNext().map { (a, b) -> haversineKm(a, b) }
        val dist = segs.sum()
        val elev = points.mapIndexed { i, _ -> ElevationPoint(segs.take(i).sum(), 1400.0 + 80 * sin(i.toDouble() / points.size * PI * 2)) }
        var gain = 0.0; var loss = 0.0
        elev.zipWithNext().forEach { (a, b) -> val d = b.elevationMeters - a.elevationMeters; if (d > 0) gain += d else loss += abs(d) }
        return RouteCandidate(id, points, elev, dist, gain, loss, CANDIDATE_COLORS[id % CANDIDATE_COLORS.size])
    }

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
                viewModelScope.launch { delay(5_000); lm.removeUpdates(listener); if (cont.isActive) cont.resume(null) }
            } catch (_: SecurityException) { cont.resume(null) }
        }
        live?.let { LatLng(it.latitude, it.longitude) }
            ?: providers.firstNotNullOfOrNull { lm.getLastKnownLocation(it) }?.let { LatLng(it.latitude, it.longitude) }
    }

    private suspend fun nominatimSearch(query: String): List<SearchResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val urlString = "${AppConstants.OSRM_BASE}/search?q=$encoded&format=jsonv2&limit=10&addressdetails=1&extratags=1&namedetails=1"
                val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    setRequestProperty("User-Agent", osmUserAgent)
                    setRequestProperty("Accept-Language", "en")
                    connectTimeout = 8_000; readTimeout = 8_000
                }
                val json = conn.inputStream.bufferedReader().readText(); conn.disconnect()
                val arr = JSONArray(json)
                (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    SearchResult(obj.getString("display_name").take(80), LatLng(obj.getDouble("lat"), obj.getDouble("lon")))
                }
            }.getOrElse { e -> e.printStackTrace(); emptyList() }
        }

    private fun buildWaypointSuggestions() {
        val s = _state.value
        val start = s.startPoint ?: return
        val end = s.endPoint ?: return
        val suggested = (1..3).map { i ->
            val t = i.toDouble() / 4.0
            LatLng(start.latitude + t * (end.latitude - start.latitude), start.longitude + t * (end.longitude - start.longitude))
        }
        _state.update { it.copy(suggestedWaypoints = suggested) }
    }

    private fun saveRoute() {
        val s = _state.value
        val candidate = s.selectedCandidate ?: run { setError("No route selected"); return }
        val start = s.startPoint ?: run { setError("Start point missing"); return }
        val end = s.endPoint ?: run { setError("Destination missing"); return }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                val allPoints = buildList { add(start); addAll(s.activeWaypoints); add(end); if (s.isRoundTrip) add(start) }
                val waypointEntities = allPoints.mapIndexed { index, latLng ->
                    Waypoint(
                        routeId = 0L,
                        name = when (index) { 0 -> "Start"; allPoints.lastIndex -> if (s.isRoundTrip) "Return to Start" else "Finish"; else -> "Waypoint $index" },
                        position = latLng, orderIndex = index,
                        isStopPlanned = index != 0 && index != allPoints.lastIndex,
                    )
                }
                saveRouteUseCase(Route(
                    name = s.name.ifBlank { "New Route" }, description = s.description, sportType = s.sportType,
                    startPoint = start, endPoint = end, waypoints = waypointEntities,
                    elevationProfile = candidate.elevationProfile,
                    totalDistanceKm = if (s.isRoundTrip) candidate.totalDistanceKm * 2 else candidate.totalDistanceKm,
                    totalElevationGainMeters = candidate.totalElevationGainMeters,
                    totalElevationLossMeters = candidate.totalElevationLossMeters, isRoundTrip = s.isRoundTrip,
                ))
            }.onSuccess { id -> _state.update { it.copy(isSaving = false, savedRouteId = id) } }
                .onFailure { e -> _state.update { it.copy(isSaving = false, error = e.message) } }
        }
    }

    private suspend fun generateDistanceSuggestions(start: LatLng, targetKm: Double) =
        withContext(Dispatchers.IO) {
            listOf(0.0, 45.0, 90.0, 135.0).map { b ->
                SearchResult(bearingLabel(b) + " (≈ ${targetKm.toInt()} km)", destinationPoint(start, b, targetKm))
            }
        }

    private fun haversineKm(a: LatLng, b: LatLng): Double {
        val r = 6371.0; val dLat = Math.toRadians(b.latitude - a.latitude); val dLng = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2).pow(2) + cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLng / 2).pow(2)
        return r * 2 * atan2(sqrt(h), sqrt(1 - h))
    }

    private fun destinationPoint(start: LatLng, bearingDeg: Double, distanceKm: Double): LatLng {
        val r = 6371.0; val d = distanceKm / r; val brng = Math.toRadians(bearingDeg)
        val lat1 = Math.toRadians(start.latitude); val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.asin(sin(lat1) * cos(d) + cos(lat1) * sin(d) * cos(brng))
        val lon2 = lon1 + atan2(sin(brng) * sin(d) * cos(lat1), cos(d) - sin(lat1) * sin(lat2))
        return LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2))
    }

    private fun bearingLabel(b: Double) = when {
        b < 22.5 -> "North"; b < 67.5 -> "North-East"; b < 112.5 -> "East"; b < 157.5 -> "South-East"
        b < 202.5 -> "South"; b < 247.5 -> "South-West"; b < 292.5 -> "West"; b < 337.5 -> "North-West"
        else -> "North"
    }

    private fun Double.fmt() = "%.5f".format(this)
    private fun setError(msg: String) = _state.update { it.copy(error = msg) }
}