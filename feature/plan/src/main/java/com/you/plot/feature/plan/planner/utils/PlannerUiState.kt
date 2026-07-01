package com.you.plot.feature.plan.planner.utils

import com.you.plot.core.common.entity.SportType
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.Event
import com.you.plot.core.domain.entity.Route
import java.util.Calendar

enum class PlanSource { ROUTE, TEMPLATE }

data class PlannerUiState(
    val planSource: PlanSource = PlanSource.ROUTE,
    val routes: List<Route> = emptyList(),
    val templatePlans: List<ActivityPlan> = emptyList(),
    val selectedRoute: Route? = null,
    val selectedTemplate: ActivityPlan? = null,
    val planName: String = "",
    val description: String = "",
    val sportType: SportType = SportType.RUNNING,
    val startDate: Long = todayAtMidnight(),
    val startHour: Int = 6,
    val startMinute: Int = 0,
    val avgDailyDistOverride: Double? = null,
    val numberOfDays: Int = 1,
    val avgSpeed: Double = 10.0,
    val generatedEvents: List<Event> = emptyList(),
    val customEvents: List<Event> = emptyList(),
    val selectedDay: Int = 1,
    val currentStep: Int = 0,
    val isSaving: Boolean = false,
    val isGenerating: Boolean = false,
    val savedPlanId: Long? = null,
    val error: String? = null,
) {
    /** Effective start epoch = date midnight + hours offset */
    val startTime: Long
        get() = startDate + startHour * 3_600_000L + startMinute * 60_000L

    /** Auto-calculated from route / template, but user can override */
    val avgDailyDist: Double
        get() = avgDailyDistOverride
            ?: ((selectedRoute?.totalDist ?: selectedTemplate?.avgDailyDist?.times(numberOfDays) ?: 0.0)
                .div(numberOfDays.coerceAtLeast(1)))

    /** All events for the currently selected day, sorted by time */
    val eventsForSelectedDay: List<Event>
        get() = (generatedEvents + customEvents)
            .filter { it.dayNumber == selectedDay }
            .sortedBy { it.plannedTime }

    /** Total distance covered by the end of the selected day */
    val dayTotalDist: Double
        get() = eventsForSelectedDay.maxOfOrNull { it.distCovered } ?: 0.0

    /** Distance remaining after this day */
    val remainingDist: Double
        get() {
            val totalDist = selectedRoute?.totalDist
                ?: (avgDailyDist * numberOfDays)
            return (totalDist - dayTotalDist).coerceAtLeast(0.0)
        }

    /** Adjusted distance per remaining day after today */
    val adjustedRemainingDailyDist: Double
        get() {
            val daysLeft = (numberOfDays - selectedDay).coerceAtLeast(1)
            return remainingDist / daysLeft
        }
}

 fun todayAtMidnight(): Long {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}