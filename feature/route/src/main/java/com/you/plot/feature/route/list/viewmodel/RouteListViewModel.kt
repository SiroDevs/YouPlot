package com.you.plot.feature.route.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.common.entity.DestinationMode
import com.you.plot.core.common.entity.PlotterStage
import com.you.plot.core.common.entity.RouteCandidate
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.common.entity.WaypointSearchResult
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

@HiltViewModel
class RouteListViewModel @Inject constructor(
    getAllRoutesUseCase: GetAllRoutesUseCase,
) : ViewModel() {
    val uiState: StateFlow<RouteListUiState> = getAllRoutesUseCase()
        .map { RouteListUiState(routes = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RouteListUiState())
}
