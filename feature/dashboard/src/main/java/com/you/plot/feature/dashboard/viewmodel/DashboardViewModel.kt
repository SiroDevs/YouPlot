package com.you.plot.feature.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.domain.usecase.plan.GetAllPlansUseCase
import com.you.plot.core.domain.usecase.route.GetAllRoutesUseCase
import com.you.plot.feature.dashboard.dashboard.utils.DashboardUiState
import com.you.plot.feature.dashboard.dashboard.utils.PlanFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getAllRoutesUseCase: GetAllRoutesUseCase,
    getAllPlansUseCase: GetAllPlansUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                getAllRoutesUseCase(),
                getAllPlansUseCase(),
            ) { routes, plans ->
                DashboardUiState(
                    recentRoutes = routes.sortedByDescending { it.createdAt }.take(5),
                    plans = plans.sortedByDescending { it.startDate },
                    routesById = routes.associateBy { it.id },
                    planFilter = _state.value.planFilter,
                    isLoading = false,
                )
            }.collect { newState ->
                _state.update { current -> newState.copy(planFilter = current.planFilter) }
            }
        }
    }

    fun setPlanFilter(filter: PlanFilter) = _state.update { it.copy(planFilter = filter) }
}
