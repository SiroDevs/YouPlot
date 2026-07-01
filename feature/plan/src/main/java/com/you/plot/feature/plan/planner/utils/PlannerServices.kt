package com.you.plot.feature.plan.planner.utils

import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.Event

/**
 * Pure helpers extracted from [com.you.plot.feature.plan.planner.viewmodel.PlannerViewModel]
 * so the viewmodel focuses on state transitions and orchestration, not domain math.
 */

/** Validation for the "Details" step. Returns null when everything looks OK. */
fun PlannerUiState.validateDetailsStep(): String? = when {
    (selectedRoute?.id ?: selectedTemplate?.routeId) == null ->
        "No route associated with this plan"
    numberOfDays < 1 -> "Number of days must be at least 1"
    avgSpeed <= 0 -> "Speed must be greater than 0"
    else -> null
}

/** Assemble the base plan record used to seed [ClonePlanUseCase] / schedule generation. */
fun PlannerUiState.toDraftPlan(routeId: Long): ActivityPlan = ActivityPlan(
    routeId = routeId,
    name = planName,
    description = description,
    sportType = sportType,
    startDate = startTime,
    numberOfDays = numberOfDays,
    avgSpeed = avgSpeed.coerceAtLeast(1.0),
    avgDailyDist = avgDailyDist.coerceAtLeast(0.1),
)

/**
 * When a plan is being cloned from a template, shift the template's custom events
 * (those without a linked waypoint) to sit on top of the newly chosen start date.
 */
fun cloneTemplateCustomEvents(
    template: ActivityPlan?,
    newStartTime: Long,
): List<Event> {
    if (template == null) return emptyList()
    val offset = newStartTime - template.startDate
    return template.events
        .filter { it.waypointId == null }
        .map { it.copy(id = 0L, planId = 0L, plannedTime = it.plannedTime + offset) }
}

/** Merge and reorder generated + custom events, giving each a fresh sequential index. */
fun mergeAndOrder(generated: List<Event>, custom: List<Event>): List<Event> =
    (generated + custom)
        .sortedWith(compareBy({ it.dayNumber }, { it.plannedTime }))
        .mapIndexed { i, e -> e.copy(orderIndex = i) }
