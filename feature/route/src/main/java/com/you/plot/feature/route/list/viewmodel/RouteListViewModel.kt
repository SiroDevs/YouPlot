package com.you.plot.feature.route.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.domain.entity.ElevationPoint
import com.you.plot.core.domain.entity.LatLng
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.entity.SportType
import com.you.plot.core.domain.usecase.route.GetAllRoutesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class RouteListUiState(
    val routes: List<Route> = emptyList(),
    val isLoading: Boolean = true,
)

enum class PlotterStage {
    STAGE_1,
    STAGE_2,
    STAGE_3,
    STAGE_4,
    STAGE_5,
    STAGE_6,
}

enum class DestinationMode { PICK_POINT, TARGET_DISTANCE }

data class RouteCandidate(
    val id: Int,
    val waypoints: List<LatLng>,
    val elevationProfile: List<ElevationPoint>,
    val totalDistanceKm: Double,
    val totalElevationGainMeters: Double,
    val totalElevationLossMeters: Double,
    val colorArgb: Long,
)

data class SearchResult(
    val displayName: String,
    val latLng: LatLng,
)

data class RoutePlotterUiState(
    // ── meta ──────────────────────────────────────────────────────────────────
    val stage: PlotterStage = PlotterStage.STAGE_1,

    // ── Stage 1 ───────────────────────────────────────────────────────────────
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val startPoint: LatLng? = null,

    // ── Stage 2 ───────────────────────────────────────────────────────────────
    val destinationMode: DestinationMode = DestinationMode.PICK_POINT,
    val endPoint: LatLng? = null,
    val targetDistanceKm: Double = 10.0,
    val targetDistanceQuery: String = "10",
    val distanceSuggestions: List<SearchResult> = emptyList(), // suggested endpoints for distance mode

    // ── Stage 3 ───────────────────────────────────────────────────────────────
    val manualWaypoints: List<LatLng> = emptyList(),
    val suggestedWaypoints: List<LatLng> = emptyList(),
    val useSuggestedWaypoints: Boolean = false,

    // ── Stage 4 ───────────────────────────────────────────────────────────────
    val routeCandidates: List<RouteCandidate> = emptyList(),
    val selectedCandidateId: Int? = null,

    // ── Stage 5 ───────────────────────────────────────────────────────────────
    val isRoundTrip: Boolean = false,

    // ── Stage 6 ───────────────────────────────────────────────────────────────
    val name: String = "",
    val description: String = "",
    val sportType: SportType = SportType.RUNNING,   // explicit default

    // ── shared ────────────────────────────────────────────────────────────────
    val isSaving: Boolean = false,
    val savedRouteId: Long? = null,
    val error: String? = null,
) {
    val activeWaypoints: List<LatLng>
        get() = if (useSuggestedWaypoints) suggestedWaypoints else manualWaypoints

    val selectedCandidate: RouteCandidate?
        get() = routeCandidates.firstOrNull { it.id == selectedCandidateId }
            ?: routeCandidates.firstOrNull()
}

@HiltViewModel
class RouteListViewModel @Inject constructor(
    getAllRoutesUseCase: GetAllRoutesUseCase,
) : ViewModel() {
    val uiState: StateFlow<RouteListUiState> = getAllRoutesUseCase()
        .map { RouteListUiState(routes = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RouteListUiState())
}
