/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.you.plot.feature.plan.planner.utils

import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.PlanEvent
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
    val startDateMillis: Long = todayAtMidnight(),
    val startHour: Int = 6,
    val startMinute: Int = 0,
    val avgDistancePerDayKmOverride: Double? = null,
    val numberOfDays: Int = 1,
    val avgSpeedKmh: Double = 10.0,
    val generatedEvents: List<PlanEvent> = emptyList(),
    val customEvents: List<PlanEvent> = emptyList(),
    val selectedDay: Int = 1,
    val currentStep: Int = 0,
    val isSaving: Boolean = false,
    val isGenerating: Boolean = false,
    val savedPlanId: Long? = null,
    val error: String? = null,
) {
    /** Effective start epoch = date midnight + hours offset */
    val startTimeMillis: Long
        get() = startDateMillis + startHour * 3_600_000L + startMinute * 60_000L

    /** Auto-calculated from route / template, but user can override */
    val avgDistancePerDayKm: Double
        get() = avgDistancePerDayKmOverride
            ?: ((selectedRoute?.totalDistanceKm ?: selectedTemplate?.avgDistancePerDayKm?.times(numberOfDays) ?: 0.0)
                .div(numberOfDays.coerceAtLeast(1)))

    /** All events for the currently selected day, sorted by time */
    val eventsForSelectedDay: List<PlanEvent>
        get() = (generatedEvents + customEvents)
            .filter { it.dayNumber == selectedDay }
            .sortedBy { it.plannedTimeMillis }

    /** Total distance covered by the end of the selected day */
    val dayTotalDistanceKm: Double
        get() = eventsForSelectedDay.maxOfOrNull { it.distanceCoveredKm } ?: 0.0

    /** Distance remaining after this day */
    val remainingDistanceKm: Double
        get() {
            val totalDist = selectedRoute?.totalDistanceKm
                ?: (avgDistancePerDayKm * numberOfDays)
            return (totalDist - dayTotalDistanceKm).coerceAtLeast(0.0)
        }

    /** Adjusted distance per remaining day after today */
    val adjustedRemainingDailyDistanceKm: Double
        get() {
            val daysLeft = (numberOfDays - selectedDay).coerceAtLeast(1)
            return remainingDistanceKm / daysLeft
        }
}

 fun todayAtMidnight(): Long {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}