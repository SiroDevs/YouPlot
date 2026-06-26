package com.you.plot.feature.route.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.common.entity.DestinationMode
import com.you.plot.core.common.entity.PlotterStage
import com.you.plot.core.common.entity.RouteCandidate
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.domain.entity.SearchResult
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

data class RoutePlotterUiState(
    val stage: PlotterStage = PlotterStage.STAGE_1,
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val startPoint: LatLng? = null,
    val destinationMode: DestinationMode = DestinationMode.PICK_POINT,
    val endPoint: LatLng? = null,
    val targetDistanceKm: Double = 10.0,
    val targetDistanceQuery: String = "10",
    val distanceSuggestions: List<SearchResult> = emptyList(),
    val manualWaypoints: List<LatLng> = emptyList(),
    val suggestedWaypoints: List<LatLng> = emptyList(),
    val useSuggestedWaypoints: Boolean = false,
    val routeCandidates: List<RouteCandidate> = emptyList(),
    val selectedCandidateId: Int? = null,
    val isRoundTrip: Boolean = false,
    val name: String = "",
    val description: String = "",
    val sportType: SportType = SportType.RUNNING,
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
