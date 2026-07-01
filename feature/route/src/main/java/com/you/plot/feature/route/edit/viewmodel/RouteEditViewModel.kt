package com.you.plot.feature.route.edit.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.usecase.route.GetRouteByIdUseCase
import com.you.plot.core.domain.usecase.route.UpdateRouteUseCase
import com.you.plot.feature.route.plotter.utils.statsFor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RouteEditUiState(
    val original: Route? = null,
    val name: String = "",
    val description: String = "",
    val sportType: SportType = SportType.RUNNING,
    val isRoundTrip: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val error: String? = null,
) {
    val hasChanges: Boolean
        get() {
            val o = original ?: return false
            return name != o.name ||
                description != o.description ||
                sportType != o.sportType ||
                isRoundTrip != o.isRoundTrip
        }
}

/**
 * Edits metadata on an existing route: name, description, sport type, round-trip
 * toggle. The geometry (start, end, waypoints, polyline) is left alone — that
 * requires re-plotting via [com.you.plot.feature.route.plotter.viewmodel.PlotterViewModel].
 * Toggling round-trip re-derives distance and elevation stats through
 * [com.you.plot.feature.route.plotter.utils.statsFor] using the original one-way
 * candidate values reconstructed from the stored route.
 */
@HiltViewModel
class RouteEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRouteByIdUseCase: GetRouteByIdUseCase,
    private val updateRouteUseCase: UpdateRouteUseCase,
) : ViewModel() {

    private val routeId: Long = checkNotNull(savedStateHandle["routeId"])
    private val _state = MutableStateFlow(RouteEditUiState())
    val state: StateFlow<RouteEditUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val route = getRouteByIdUseCase(routeId)
            if (route == null) {
                _state.update { it.copy(isLoading = false, error = "Route not found") }
                return@launch
            }
            _state.update {
                it.copy(
                    original = route,
                    name = route.name,
                    description = route.description,
                    sportType = route.sportType,
                    isRoundTrip = route.isRoundTrip,
                    isLoading = false,
                )
            }
        }
    }

    fun setName(name: String) = _state.update { it.copy(name = name) }
    fun setDescription(desc: String) = _state.update { it.copy(description = desc) }
    fun setSportType(type: SportType) = _state.update { it.copy(sportType = type) }
    fun setRoundTrip(round: Boolean) = _state.update { it.copy(isRoundTrip = round) }
    fun clearError() = _state.update { it.copy(error = null) }

    fun save() {
        val s = _state.value
        val original = s.original ?: return
        if (s.name.isBlank()) {
            _state.update { it.copy(error = "Route name cannot be empty") }
            return
        }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                val updated = original
                    .withRoundTripAdjusted(s.isRoundTrip)
                    .copy(
                        name = s.name,
                        description = s.description,
                        sportType = s.sportType,
                    )
                updateRouteUseCase(updated)
            }.onSuccess {
                _state.update { it.copy(isSaving = false, savedSuccessfully = true) }
            }.onFailure { e ->
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}

/**
 * Recomputes distance / elevation / polyline when the user toggles round-trip.
 * Uses [statsFor] against a one-way baseline reconstructed from the stored route.
 */
private fun Route.withRoundTripAdjusted(newRoundTrip: Boolean): Route {
    if (newRoundTrip == isRoundTrip) return this
    // Reconstruct the one-way candidate from persisted state. When switching from
    // round-trip → one-way we halve; when the other way we double — mirroring
    // exactly what PlotterViewModel does at save time.
    val oneWayDist = if (isRoundTrip) totalDist / 2.0 else totalDist
    val oneWayGain = if (isRoundTrip) elevationGain / 2.0 else elevationGain
    val oneWayLoss = if (isRoundTrip) elevationLoss / 2.0 else elevationLoss
    val oneWayPolyline = if (isRoundTrip && polyline.isNotEmpty()) {
        // Trim the mirrored return leg back off.
        polyline.subList(0, (polyline.size + 1) / 2)
    } else polyline
    val oneWayProfile = if (isRoundTrip && elevationPoints.isNotEmpty()) {
        elevationPoints.subList(0, (elevationPoints.size + 1) / 2)
    } else elevationPoints

    val candidate = com.you.plot.core.common.entity.RouteCandidate(
        id = 0,
        waypoints = oneWayPolyline,
        elevationPoints = oneWayProfile,
        totalDist = oneWayDist,
        elevationGain = oneWayGain,
        elevationLoss = oneWayLoss,
        colorArgb = 0L,
    )
    val stats = candidate.statsFor(newRoundTrip)
    return copy(
        isRoundTrip = newRoundTrip,
        totalDist = stats.totalDist,
        elevationGain = stats.elevationGain,
        elevationLoss = stats.elevationLoss,
        elevationPoints = stats.elevationPoints,
        polyline = stats.polyline,
    )
}
