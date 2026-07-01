package com.you.plot.feature.plan.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.usecase.plan.ClonePlanUseCase
import com.you.plot.core.domain.usecase.plan.DeletePlanUseCase
import com.you.plot.core.domain.usecase.plan.GetAllPlansUseCase
import com.you.plot.core.domain.usecase.plan.GetFavoritePlansUseCase
import com.you.plot.core.domain.usecase.plan.SetPlanFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PlanListTab { RECENT, LISTS, FAVORITES }

data class PlanListUiState(
    val recent: List<ActivityPlan> = emptyList(),
    val favorites: List<ActivityPlan> = emptyList(),
    val selectedTab: PlanListTab = PlanListTab.RECENT,
    val isLoading: Boolean = true,
    val menuTargetId: Long? = null,
    val clonedPlanId: Long? = null,
)

@HiltViewModel
class PlanListViewModel @Inject constructor(
    getAllPlans: GetAllPlansUseCase,
    getFavoritePlans: GetFavoritePlansUseCase,
    private val setFavorite: SetPlanFavoriteUseCase,
    private val deletePlan: DeletePlanUseCase,
    private val clonePlan: ClonePlanUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PlanListUiState())
    val uiState: StateFlow<PlanListUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAllPlans().collect { list ->
                _state.update { it.copy(recent = list, isLoading = false) }
            }
        }
        viewModelScope.launch {
            getFavoritePlans().collect { list -> _state.update { it.copy(favorites = list) } }
        }
    }

    fun selectTab(tab: PlanListTab) = _state.update { it.copy(selectedTab = tab) }
    fun openMenu(id: Long) = _state.update { it.copy(menuTargetId = id) }
    fun dismissMenu() = _state.update { it.copy(menuTargetId = null) }
    fun consumeClonedId() = _state.update { it.copy(clonedPlanId = null) }

    fun toggleFavorite(plan: ActivityPlan) = viewModelScope.launch {
        setFavorite(plan.id, !plan.isFavorite)
    }

    fun delete(id: Long) = viewModelScope.launch {
        deletePlan(id)
        _state.update { it.copy(menuTargetId = null) }
    }

    /**
     * Clones the plan with a fresh start date (default: 7 days out). The caller
     * navigates to the returned plan id so the user can adjust dates or events.
     */
    fun duplicate(plan: ActivityPlan) = viewModelScope.launch {
        val newStart = plan.startDate + 7L * 86_400_000L
        val newId = clonePlan(plan.id, newStart)
        _state.update { it.copy(menuTargetId = null, clonedPlanId = newId) }
    }
}
