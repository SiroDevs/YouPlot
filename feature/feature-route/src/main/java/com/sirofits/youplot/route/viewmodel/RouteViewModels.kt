package com.sirofits.youplot.route.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.sirofits.youplot.domain.entity.*
import com.sirofits.youplot.domain.usecase.route.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RouteListUiState(
    val routes: List<Route> = emptyList(),
    val isLoading: Boolean = true,
)

data class RoutePlotterUiState(
    val name: String = "",
    val description: String = "",
    val sportType: SportType = SportType.RUNNING,
    val startPoint: LatLng? = null,
    val endPoint: LatLng? = null,
    val waypoints: List<LatLng> = emptyList(),
    val isRoundTrip: Boolean = false,
    val isSaving: Boolean = false,
    val savedRouteId: Long? = null,
    val error: String? = null,
)

@HiltViewModel
class RouteListViewModel @Inject constructor(
    getAllRoutesUseCase: GetAllRoutesUseCase,
) : ViewModel() {
    val uiState: StateFlow<RouteListUiState> = getAllRoutesUseCase()
        .map { RouteListUiState(routes = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RouteListUiState())
}

@HiltViewModel
class RoutePlotterViewModel @Inject constructor(
    private val saveRouteUseCase: SaveRouteUseCase,
    private val deleteRouteUseCase: DeleteRouteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RoutePlotterUiState())
    val state: StateFlow<RoutePlotterUiState> = _state.asStateFlow()

    fun setName(name: String) = _state.update { it.copy(name = name) }
    fun setDescription(desc: String) = _state.update { it.copy(description = desc) }
    fun setSportType(type: SportType) = _state.update { it.copy(sportType = type) }
    fun setRoundTrip(isRound: Boolean) = _state.update { it.copy(isRoundTrip = isRound) }

    fun setStartPoint(latLng: LatLng) = _state.update { it.copy(startPoint = latLng) }
    fun setEndPoint(latLng: LatLng) = _state.update { it.copy(endPoint = latLng) }

    fun addWaypoint(latLng: LatLng) = _state.update {
        it.copy(waypoints = it.waypoints + latLng)
    }

    fun removeWaypoint(index: Int) = _state.update {
        it.copy(waypoints = it.waypoints.toMutableList().also { list -> list.removeAt(index) })
    }

    fun clearError() = _state.update { it.copy(error = null) }

    fun saveRoute() {
        val s = _state.value
        val start = s.startPoint ?: run {
            _state.update { it.copy(error = "Set a start point on the map") }
            return
        }
        val end = s.endPoint ?: run {
            _state.update { it.copy(error = "Set a destination on the map") }
            return
        }

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                val waypoints = buildWaypoints(start, s.waypoints, end)
                val route = Route(
                    name = s.name.ifBlank { "New Route" },
                    description = s.description,
                    sportType = s.sportType,
                    startPoint = start,
                    endPoint = end,
                    waypoints = waypoints,
                    totalDistanceKm = estimateDistance(start, s.waypoints, end),
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

    fun deleteRoute(id: Long) {
        viewModelScope.launch { deleteRouteUseCase(id) }
    }

    private fun buildWaypoints(
        start: LatLng,
        middle: List<LatLng>,
        end: LatLng,
    ): List<Waypoint> {
        val all = listOf(start) + middle + listOf(end)
        return all.mapIndexed { index, latLng ->
            Waypoint(
                routeId = 0L,
                name = when (index) {
                    0 -> "Start"
                    all.lastIndex -> "Finish"
                    else -> "Waypoint $index"
                },
                position = latLng,
                orderIndex = index,
                isStopPlanned = index != 0 && index != all.lastIndex,
            )
        }
    }

    /** Rough sum of haversine segments */
    private fun estimateDistance(start: LatLng, middle: List<LatLng>, end: LatLng): Double {
        val all = listOf(start) + middle + listOf(end)
        return all.zipWithNext().sumOf { (a, b) -> haversineKm(a, b) }
    }

    private fun haversineKm(a: LatLng, b: LatLng): Double {
        val r = 6371.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLng = Math.toRadians(b.longitude - a.longitude)
        val h = Math.sin(dLat / 2).let { it * it } +
                Math.cos(Math.toRadians(a.latitude)) * Math.cos(Math.toRadians(b.latitude)) *
                Math.sin(dLng / 2).let { it * it }
        return r * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h))
    }
}
