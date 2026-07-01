package com.you.plot.feature.plan.planner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.Event
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.usecase.plan.DeletePlanUseCase
import com.you.plot.core.domain.usecase.plan.GenerateEventsUseCase
import com.you.plot.core.domain.usecase.plan.GetAllPlansUseCase
import com.you.plot.core.domain.usecase.plan.SavePlanUseCase
import com.you.plot.core.domain.usecase.route.GetAllRoutesUseCase
import com.you.plot.feature.plan.planner.utils.PlanSource
import com.you.plot.feature.plan.planner.utils.PlannerUiState
import com.you.plot.feature.plan.planner.utils.cloneTemplateCustomEvents
import com.you.plot.feature.plan.planner.utils.mergeAndOrder
import com.you.plot.feature.plan.planner.utils.toDraftPlan
import com.you.plot.feature.plan.planner.utils.validateDetailsStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlannerViewModel @Inject constructor(
    savedStateHandle: androidx.lifecycle.SavedStateHandle,
    private val getAllRoutesUseCase: GetAllRoutesUseCase,
    private val getAllPlansUseCase: GetAllPlansUseCase,
    private val savePlanUseCase: SavePlanUseCase,
    private val deletePlanUseCase: DeletePlanUseCase,
    private val generateEventsUseCase: GenerateEventsUseCase,
    private val getRouteByIdUseCase: com.you.plot.core.domain.usecase.route.GetRouteByIdUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PlannerUiState())
    val state: StateFlow<PlannerUiState> = _state.asStateFlow()

    init {
        val preselectedRouteId = savedStateHandle.get<Long>("routeId")

        viewModelScope.launch {
            getAllRoutesUseCase().collect { routes ->
                _state.update { it.copy(routes = routes) }
            }
        }
        viewModelScope.launch {
            getAllPlansUseCase().collect { plans ->
                _state.update { it.copy(templatePlans = plans) }
            }
        }
        if (preselectedRouteId != null && preselectedRouteId > 0L) {
            viewModelScope.launch {
                val route = getRouteByIdUseCase(preselectedRouteId)
                if (route != null) {
                    _state.update {
                        it.copy(
                            selectedRoute = route,
                            planName = "${route.name} Plan",
                            sportType = route.sportType,
                            currentStep = 1,   // skip Step 0 – route already chosen
                        )
                    }
                }
            }
        }
    }

    fun setSportType(type: SportType) = _state.update { it.copy(sportType = type) }

    fun setPlanSource(source: PlanSource) {
        _state.update {
            it.copy(
                planSource = source,
                selectedRoute = null,
                selectedTemplate = null
            )
        }
    }

    fun selectRoute(route: Route) {
        _state.update {
            it.copy(
                selectedRoute = route,
                selectedTemplate = null,
                planName = "${route.name} Plan",
                sportType = route.sportType,
                avgDailyDistOverride = null,
            )
        }
    }

    fun selectTemplate(plan: ActivityPlan) {
        _state.update {
            it.copy(
                selectedTemplate = plan,
                selectedRoute = null,
                planName = "${plan.name} (Copy)",
                description = plan.description,
                sportType = plan.sportType,
                numberOfDays = plan.numberOfDays,
                avgSpeed = plan.avgSpeed,
                avgDailyDistOverride = plan.avgDailyDist,
            )
        }
    }

    fun setPlanName(name: String) = _state.update { it.copy(planName = name) }
    fun setDescription(desc: String) = _state.update { it.copy(description = desc) }
    fun setStartDate(millis: Long) = _state.update { it.copy(startDate = millis) }
    fun setStartTime(hour: Int, minute: Int) =
        _state.update { it.copy(startHour = hour, startMinute = minute) }

    fun setNumberOfDays(days: Int) = _state.update {
        it.copy(numberOfDays = days.coerceAtLeast(1), avgDailyDistOverride = null)
    }

    fun setAvgSpeed(speed: Double) =
        _state.update { it.copy(avgSpeed = speed.coerceAtLeast(1.0)) }

    fun setAvgDistancePerDay(km: Double) =
        _state.update { it.copy(avgDailyDistOverride = km.coerceAtLeast(0.1)) }

    fun selectDay(day: Int) = _state.update { it.copy(selectedDay = day) }

    fun addCustomEvent(name: String, hour: Int, minute: Int, duration: Int, day: Int) {
        val s = _state.value
        val dayStartMillis = s.startTime + (day - 1) * 86_400_000L
        val eventMillis = dayStartMillis + hour * 3_600_000L + minute * 60_000L
        val prevDist = s.eventsForSelectedDay
            .filter { it.plannedTime <= eventMillis }
            .maxOfOrNull { it.distCovered } ?: 0.0
        val event = Event(
            id = System.currentTimeMillis(),
            planId = 0L,
            dayNumber = day,
            name = name,
            plannedTime = eventMillis,
            duration = duration,
            distCovered = prevDist,
            orderIndex = s.customEvents.size,
        )
        _state.update { it.copy(customEvents = it.customEvents + event) }
    }

    fun removeCustomEvent(eventId: Long) {
        _state.update { it.copy(customEvents = it.customEvents.filter { e -> e.id != eventId }) }
    }

    fun removeGeneratedEvent(eventId: Long) {
        _state.update { it.copy(generatedEvents = it.generatedEvents.filter { e -> e.id != eventId }) }
    }

    fun nextStep() {
        val s = _state.value
        when (s.currentStep) {
            0 -> {
                val hasSelection = s.selectedRoute != null || s.selectedTemplate != null
                if (!hasSelection) {
                    setError("Select a route or template first"); return
                }
                _state.update { it.copy(currentStep = 1) }
            }

            1 -> {
                s.validateDetailsStep()?.let { setError(it); return }
                _state.update { it.copy(isGenerating = true) }
                viewModelScope.launch {
                    runCatching { generateEvents() }
                        .onFailure { e ->
                            _state.update {
                                it.copy(
                                    isGenerating = false,
                                    error = "Failed to generate schedule: ${e.message}",
                                )
                            }
                        }
                }
            }

            2 -> _state.update { it.copy(currentStep = 3) }
        }
    }

    fun prevStep() = _state.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }

    private suspend fun generateEvents() {
        val s = _state.value
        val routeId = s.selectedRoute?.id ?: s.selectedTemplate?.routeId ?: run {
            setError("No route associated"); return
        }
        val events = generateEventsUseCase(s.toDraftPlan(routeId))
        val templateCustom = cloneTemplateCustomEvents(s.selectedTemplate, s.startTime)
        _state.update {
            it.copy(
                generatedEvents = events,
                customEvents = templateCustom,
                currentStep = 2,
                selectedDay = 1,
                isGenerating = false,
            )
        }
    }

    fun savePlan() {
        val s = _state.value
        val routeId = s.selectedRoute?.id ?: s.selectedTemplate?.routeId ?: run {
            setError("No route associated"); return
        }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                val orderedEvents = mergeAndOrder(s.generatedEvents, s.customEvents)
                val plan = s.toDraftPlan(routeId).copy(
                    name = s.planName.ifBlank { "Plan" },
                    events = orderedEvents,
                )
                savePlanUseCase(plan)
            }.onSuccess { id ->
                _state.update { it.copy(isSaving = false, savedPlanId = id) }
            }.onFailure { e ->
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun deletePlan(id: Long) = viewModelScope.launch { deletePlanUseCase(id) }
    fun clearError() = _state.update { it.copy(error = null) }
    private fun setError(msg: String) = _state.update { it.copy(error = msg) }
}
