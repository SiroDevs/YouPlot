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

package com.you.plot.feature.plan.details.utils

import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.PlanEvent

data class ReminderEntry(
    val index: Int,
    val label: String,
    val fireAtMillis: Long,
)

data class PlanDetailUiState(
    val plan: ActivityPlan? = null,
    val selectedDay: Int = 1,
    val isLoading: Boolean = true,
    val reminders: List<ReminderEntry> = emptyList(),
    val showAddReminderDialog: Boolean = false,
) {
    /** Events for the selected day, sorted by planned time */
    val eventsForDay: List<PlanEvent>
        get() = plan?.events
            ?.filter { it.dayNumber == selectedDay }
            ?.sortedBy { it.plannedTimeMillis }
            ?: emptyList()

    /** Distance covered at the last event of the selected day */
    val dayTotalDistanceKm: Double
        get() = eventsForDay.maxOfOrNull { it.distanceCoveredKm } ?: 0.0

    /** Distance remaining after the selected day */
    val remainingDistanceKm: Double
        get() {
            val total = plan?.run { avgDistancePerDayKm * numberOfDays } ?: 0.0
            return (total - dayTotalDistanceKm).coerceAtLeast(0.0)
        }

    /** Adjusted daily distance for the days still to go */
    val adjustedDailyDistanceKm: Double
        get() {
            val daysLeft = (plan?.numberOfDays ?: 1) - selectedDay
            return if (daysLeft > 0) remainingDistanceKm / daysLeft else 0.0
        }
}
