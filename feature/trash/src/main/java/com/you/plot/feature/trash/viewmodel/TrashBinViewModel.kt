package com.you.plot.feature.trash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.entity.StartPoint
import com.you.plot.core.domain.usecase.plan.GetTrashedPlansUseCase
import com.you.plot.core.domain.usecase.plan.PermanentlyDeletePlanUseCase
import com.you.plot.core.domain.usecase.plan.RestorePlanUseCase
import com.you.plot.core.domain.usecase.route.GetTrashedRoutesUseCase
import com.you.plot.core.domain.usecase.route.PermanentlyDeleteRouteUseCase
import com.you.plot.core.domain.usecase.route.RestoreRouteUseCase
import com.you.plot.core.domain.usecase.startpoint.GetTrashedStartPointsUseCase
import com.you.plot.core.domain.usecase.startpoint.PermanentlyDeleteStartPointUseCase
import com.you.plot.core.domain.usecase.startpoint.RestoreStartPointUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrashBinUiState(
    val trashedRoutes: List<Route> = emptyList(),
    val trashedPlans: List<ActivityPlan> = emptyList(),
    val trashedStartPoints: List<StartPoint> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class TrashBinViewModel @Inject constructor(
    getTrashedRoutes: GetTrashedRoutesUseCase,
    getTrashedPlans: GetTrashedPlansUseCase,
    getTrashedStartPoints: GetTrashedStartPointsUseCase,
    private val restoreRouteUseCase: RestoreRouteUseCase,
    private val restorePlanUseCase: RestorePlanUseCase,
    private val restoreStartPointUseCase: RestoreStartPointUseCase,
    private val permaDeleteRoute: PermanentlyDeleteRouteUseCase,
    private val permaDeletePlan: PermanentlyDeletePlanUseCase,
    private val permaDeleteStartPoint: PermanentlyDeleteStartPointUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(TrashBinUiState())
    val state: StateFlow<TrashBinUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getTrashedRoutes().collect { list ->
                _state.update { it.copy(trashedRoutes = list, isLoading = false) }
            }
        }
        viewModelScope.launch {
            getTrashedPlans().collect { list -> _state.update { it.copy(trashedPlans = list) } }
        }
        viewModelScope.launch {
            getTrashedStartPoints().collect { list ->
                _state.update { it.copy(trashedStartPoints = list) }
            }
        }
    }

    fun restoreRoute(id: Long) = viewModelScope.launch { restoreRouteUseCase(id) }
    fun restorePlan(id: Long) = viewModelScope.launch { restorePlanUseCase(id) }
    fun restoreStartPoint(id: Long) = viewModelScope.launch { restoreStartPointUseCase(id) }

    fun permanentlyDeleteRoute(id: Long) = viewModelScope.launch { permaDeleteRoute(id) }
    fun permanentlyDeletePlan(id: Long) = viewModelScope.launch { permaDeletePlan(id) }
    fun permanentlyDeleteStartPoint(id: Long) = viewModelScope.launch { permaDeleteStartPoint(id) }
}
