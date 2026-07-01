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
import com.you.plot.core.common.entity.ActivityStatus
import com.you.plot.core.database.model.PlanEntity
import com.you.plot.core.database.model.EventEntity
import com.you.plot.core.database.model.ActivityEntity
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.ActivityActivity
import com.you.plot.core.domain.entity.Event

fun ActivityPlan.toEntity() = PlanEntity(
    id = id, routeId = routeId, name = name, description = description,
    startDate = startDate, numberOfDays = numberOfDays,
    avgSpeed = avgSpeed, avgDailyDist = avgDailyDist, createdAt = createdAt,
)

fun PlanEntity.toDomain(events: List<EventEntity>) = ActivityPlan(
    id = id, routeId = routeId, name = name, description = description,
    startDate = startDate, numberOfDays = numberOfDays,
    avgSpeed = avgSpeed, avgDailyDist = avgDailyDist,
    events = events.map { it.toDomain() }, createdAt = createdAt,
)

fun Event.toEntity() = EventEntity(
    id = id, planId = planId, dayNumber = dayNumber, name = name,
    waypointId = waypointId, plannedTime = plannedTime,
    duration = duration, distCovered = distCovered, orderIndex = orderIndex,
)

fun EventEntity.toDomain() = Event(
    id = id, planId = planId, dayNumber = dayNumber, name = name,
    waypointId = waypointId, plannedTime = plannedTime,
    duration = duration, distCovered = distCovered, orderIndex = orderIndex,
)

fun ActivityActivity.toEntity() = ActivityEntity(
    id = id, planId = planId, routeId = routeId, status = status.name,
    startedAt = startedAt, elapsedTime = elapsedTime,
    currentLat = currentLocation?.latitude, currentLng = currentLocation?.longitude,
    currentSpeed = currentSpeed, distCovered = distCovered,
    waypointProgress = waypointProgress.toJson(),
    estimatedCompletion = estimatedCompletion,
)

fun ActivityEntity.toDomain() = ActivityActivity(
    id = id, planId = planId, routeId = routeId,
    status = ActivityStatus.valueOf(status),
    startedAt = startedAt, elapsedTime = elapsedTime,
    currentLocation = if (currentLat != null && currentLng != null) LatLng(
        currentLat,
        currentLng
    ) else null,
    currentSpeed = currentSpeed, distCovered = distCovered,
    waypointProgress = waypointProgress.toWaypointProgressList(),
    estimatedCompletion = estimatedCompletion,
)
