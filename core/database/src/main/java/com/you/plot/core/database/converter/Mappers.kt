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
    sportType = sportType,
    startDate = startDate, numberOfDays = numberOfDays,
    avgSpeed = avgSpeed, avgDailyDist = avgDailyDist, createdAt = createdAt,
    isFavorite = isFavorite, deletedAt = deletedAt,
)

fun PlanEntity.toDomain(events: List<EventEntity>) = ActivityPlan(
    id = id, routeId = routeId, name = name, description = description,
    sportType = sportType,
    startDate = startDate, numberOfDays = numberOfDays,
    avgSpeed = avgSpeed, avgDailyDist = avgDailyDist,
    events = events.map { it.toDomain() }, createdAt = createdAt,
    isFavorite = isFavorite, deletedAt = deletedAt,
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
