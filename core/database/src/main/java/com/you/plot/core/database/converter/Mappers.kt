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

package com.you.plot.core.database.converter

import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.SessionStatus
import com.you.plot.core.database.model.PlanEntity
import com.you.plot.core.database.model.PlanEventEntity
import com.you.plot.core.database.model.SessionEntity
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.ActivitySession
import com.you.plot.core.domain.entity.PlanEvent

fun ActivityPlan.toEntity() = PlanEntity(
    id = id, routeId = routeId, name = name, description = description,
    startDateMillis = startDateMillis, numberOfDays = numberOfDays,
    avgSpeedKmh = avgSpeedKmh, avgDistancePerDayKm = avgDistancePerDayKm, createdAt = createdAt,
)

fun PlanEntity.toDomain(events: List<PlanEventEntity>) = ActivityPlan(
    id = id, routeId = routeId, name = name, description = description,
    startDateMillis = startDateMillis, numberOfDays = numberOfDays,
    avgSpeedKmh = avgSpeedKmh, avgDistancePerDayKm = avgDistancePerDayKm,
    events = events.map { it.toDomain() }, createdAt = createdAt,
)

fun PlanEvent.toEntity() = PlanEventEntity(
    id = id, planId = planId, dayNumber = dayNumber, name = name,
    waypointId = waypointId, plannedTimeMillis = plannedTimeMillis,
    durationMinutes = durationMinutes, distanceCoveredKm = distanceCoveredKm, orderIndex = orderIndex,
)

fun PlanEventEntity.toDomain() = PlanEvent(
    id = id, planId = planId, dayNumber = dayNumber, name = name,
    waypointId = waypointId, plannedTimeMillis = plannedTimeMillis,
    durationMinutes = durationMinutes, distanceCoveredKm = distanceCoveredKm, orderIndex = orderIndex,
)

fun ActivitySession.toEntity() = SessionEntity(
    id = id, planId = planId, routeId = routeId, status = status.name,
    startedAtMillis = startedAtMillis, elapsedTimeSeconds = elapsedTimeSeconds,
    currentLat = currentLocation?.latitude, currentLng = currentLocation?.longitude,
    currentSpeedKmh = currentSpeedKmh, distanceCoveredKm = distanceCoveredKm,
    waypointProgressJson = waypointProgress.toJson(),
    estimatedCompletionMillis = estimatedCompletionMillis,
)

fun SessionEntity.toDomain() = ActivitySession(
    id = id, planId = planId, routeId = routeId,
    status = SessionStatus.valueOf(status),
    startedAtMillis = startedAtMillis, elapsedTimeSeconds = elapsedTimeSeconds,
    currentLocation = if (currentLat != null && currentLng != null) LatLng(
        currentLat,
        currentLng
    ) else null,
    currentSpeedKmh = currentSpeedKmh, distanceCoveredKm = distanceCoveredKm,
    waypointProgress = waypointProgressJson.toWaypointProgressList(),
    estimatedCompletionMillis = estimatedCompletionMillis,
)
