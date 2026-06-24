package com.you.plot.feature.plan.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.you.plot.core.domain.entity.*
import com.you.plot.core.domain.usecase.plan.*
import com.you.plot.core.domain.usecase.route.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── Plan List ────────────────────────────────────────────────────────────────

data class PlanListUiState(
    val plans: List<ActivityPlan> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class PlanListViewModel @Inject constructor(
    getAllPlansUseCase: GetAllPlansUseCase,
) : ViewModel() {
    val uiState: StateFlow<PlanListUiState> = getAllPlansUseCase()
        .map { PlanListUiState(plans = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlanListUiState())
}

// ─── Plan Creator ─────────────────────────────────────────────────────────────

data class PlanCreatorUiState(
    val routes: List<Route> = emptyList(),
    val selectedRoute: Route? = null,
    val planName: String = "",
    val description: String = "",
    val startDateMillis: Long = System.currentTimeMillis(),
    val numberOfDays: Int = 1,
    val avgSpeedKmh: Double = 10.0,
    val avgDistancePerDayKm: Double = 0.0,
    val generatedEvents: List<PlanEvent> = emptyList(),
    val customEvents: List<PlanEvent> = emptyList(),
    val currentStep: Int = 0,
    val isSaving: Boolean = false,
    val savedPlanId: Long? = null,
    val error: String? = null,
)

@HiltViewModel
class PlanCreatorViewModel @Inject constructor(
    private val getAllRoutesUseCase: GetAllRoutesUseCase,
    private val savePlanUseCase: SavePlanUseCase,
    private val deletePlanUseCase: DeletePlanUseCase,
    private val generateEventsUseCase: GeneratePlanEventsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PlanCreatorUiState())
    val state: StateFlow<PlanCreatorUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAllRoutesUseCase().collect { routes -> _state.update { it.copy(routes = routes) } }
        }
    }

    fun selectRoute(route: Route) = _state.update {
        it.copy(selectedRoute = route, avgDistancePerDayKm = route.totalDistanceKm, planName = "${route.name} Plan")
    }
    fun setPlanName(name: String) = _state.update { it.copy(planName = name) }
    fun setDescription(desc: String) = _state.update { it.copy(description = desc) }
    fun setNumberOfDays(days: Int) = _state.update {
        it.copy(numberOfDays = days.coerceAtLeast(1),
            avgDistancePerDayKm = it.selectedRoute?.totalDistanceKm?.div(days.coerceAtLeast(1)) ?: it.avgDistancePerDayKm)
    }
    fun setAvgSpeed(speed: Double) = _state.update { it.copy(avgSpeedKmh = speed.coerceAtLeast(1.0)) }
    fun setStartDate(millis: Long) = _state.update { it.copy(startDateMillis = millis) }
    fun clearError() = _state.update { it.copy(error = null) }

    fun nextStep() {
        val s = _state.value
        when (s.currentStep) {
            0 -> {
                if (s.selectedRoute == null) { _state.update { it.copy(error = "Please select a route first") }; return }
                _state.update { it.copy(currentStep = 1) }
            }
            1 -> viewModelScope.launch { generateEvents() }
            2 -> _state.update { it.copy(currentStep = 3) }
        }
    }

    fun prevStep() = _state.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }

    private suspend fun generateEvents() {
        val s = _state.value
        val route = s.selectedRoute ?: return
        val draft = ActivityPlan(
            routeId = route.id, name = s.planName, description = s.description,
            startDateMillis = s.startDateMillis, numberOfDays = s.numberOfDays,
            avgSpeedKmh = s.avgSpeedKmh, avgDistancePerDayKm = s.avgDistancePerDayKm,
        )
        val events = generateEventsUseCase(draft)
        _state.update { it.copy(generatedEvents = events, currentStep = 2) }
    }

    fun addCustomEvent(event: PlanEvent) = _state.update { it.copy(customEvents = it.customEvents + event) }
    fun removeCustomEvent(index: Int) = _state.update {
        it.copy(customEvents = it.customEvents.toMutableList().also { l -> l.removeAt(index) })
    }

    fun savePlan() {
        val s = _state.value
        val route = s.selectedRoute ?: return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                val allEvents = (s.generatedEvents + s.customEvents)
                    .sortedWith(compareBy({ it.dayNumber }, { it.plannedTimeMillis }))
                    .mapIndexed { i, e -> e.copy(orderIndex = i) }
                val plan = ActivityPlan(
                    routeId = route.id, name = s.planName.ifBlank { "Plan" },
                    description = s.description, startDateMillis = s.startDateMillis,
                    numberOfDays = s.numberOfDays, avgSpeedKmh = s.avgSpeedKmh,
                    avgDistancePerDayKm = s.avgDistancePerDayKm, events = allEvents,
                )
                savePlanUseCase(plan)
            }.onSuccess { id -> _state.update { it.copy(isSaving = false, savedPlanId = id) }
            }.onFailure { e -> _state.update { it.copy(isSaving = false, error = e.message) } }
        }
    }

    fun deletePlan(id: Long) = viewModelScope.launch { deletePlanUseCase(id) }
}

// ─── Plan Detail ──────────────────────────────────────────────────────────────

data class PlanDetailUiState(
    val plan: ActivityPlan? = null,
    val selectedDay: Int = 1,
    val isLoading: Boolean = true,
)

@HiltViewModel
class PlanDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPlanByIdUseCase: GetPlanByIdUseCase,
) : ViewModel() {

    private val planId: Long = checkNotNull(savedStateHandle["planId"])

    private val _state = MutableStateFlow(PlanDetailUiState())
    val state: StateFlow<PlanDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val plan = getPlanByIdUseCase(planId)
            _state.update { it.copy(plan = plan, isLoading = false) }
        }
    }

    fun selectDay(day: Int) = _state.update { it.copy(selectedDay = day) }
}
