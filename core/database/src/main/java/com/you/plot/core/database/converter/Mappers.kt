package com.you.plot.core.database.converter

import com.you.plot.core.database.model.*
import com.you.plot.core.domain.entity.*
import org.json.JSONArray
import org.json.JSONObject

// ─── Waypoint JSON ───────────────────────────────────────────────────────────

fun List<Waypoint>.toJson(): String {
    val arr = JSONArray()
    forEach { wp ->
        arr.put(JSONObject().apply {
            put("id", wp.id); put("routeId", wp.routeId); put("name", wp.name)
            put("lat", wp.position.latitude); put("lng", wp.position.longitude)
            put("orderIndex", wp.orderIndex); put("elevation", wp.elevationMeters)
            put("isStopPlanned", wp.isStopPlanned)
        })
    }
    return arr.toString()
}

fun String.toWaypointList(): List<Waypoint> {
    val arr = JSONArray(this)
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        Waypoint(
            id = obj.getLong("id"), routeId = obj.getLong("routeId"),
            name = obj.getString("name"),
            position = LatLng(obj.getDouble("lat"), obj.getDouble("lng")),
            orderIndex = obj.getInt("orderIndex"),
            elevationMeters = obj.getDouble("elevation"),
            isStopPlanned = obj.getBoolean("isStopPlanned"),
        )
    }
}

fun List<ElevationPoint>.toJson(): String {
    val arr = JSONArray()
    forEach { ep -> arr.put(JSONObject().apply { put("d", ep.distanceKm); put("e", ep.elevationMeters) }) }
    return arr.toString()
}

fun String.toElevationList(): List<ElevationPoint> {
    val arr = JSONArray(this)
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        ElevationPoint(obj.getDouble("d"), obj.getDouble("e"))
    }
}

fun List<WaypointProgress>.toJson(): String {
    val arr = JSONArray()
    forEach { wp ->
        arr.put(JSONObject().apply {
            put("waypointJson", wp.waypoint.run {
                JSONObject().apply {
                    put("id", id); put("routeId", routeId); put("name", name)
                    put("lat", position.latitude); put("lng", position.longitude)
                    put("orderIndex", orderIndex); put("elevation", elevationMeters)
                    put("isStopPlanned", isStopPlanned)
                }
            })
            put("planned", wp.plannedArrivalMillis); put("estimated", wp.estimatedArrivalMillis)
            put("remaining", wp.distanceRemainingKm); put("reached", wp.isReached)
            put("skipped", wp.wasSkipped)
        })
    }
    return arr.toString()
}

fun String.toWaypointProgressList(): List<WaypointProgress> {
    val arr = JSONArray(this)
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        val wObj = obj.getJSONObject("waypointJson")
        WaypointProgress(
            waypoint = Waypoint(
                id = wObj.getLong("id"), routeId = wObj.getLong("routeId"),
                name = wObj.getString("name"),
                position = LatLng(wObj.getDouble("lat"), wObj.getDouble("lng")),
                orderIndex = wObj.getInt("orderIndex"),
                elevationMeters = wObj.getDouble("elevation"),
                isStopPlanned = wObj.getBoolean("isStopPlanned"),
            ),
            plannedArrivalMillis = obj.getLong("planned"),
            estimatedArrivalMillis = obj.getLong("estimated"),
            distanceRemainingKm = obj.getDouble("remaining"),
            isReached = obj.getBoolean("reached"),
            wasSkipped = obj.getBoolean("skipped"),
        )
    }
}

// ─── Domain ↔ Room mappers ───────────────────────────────────────────────────

fun Route.toEntity() = RouteEntity(
    id = id, name = name, description = description, sportType = sportType,
    startLat = startPoint.latitude, startLng = startPoint.longitude,
    endLat = endPoint.latitude, endLng = endPoint.longitude,
    waypointsJson = waypoints.toJson(), elevationProfileJson = elevationProfile.toJson(),
    totalDistanceKm = totalDistanceKm, totalElevationGainMeters = totalElevationGainMeters,
    totalElevationLossMeters = totalElevationLossMeters, isRoundTrip = isRoundTrip,
    createdAt = createdAt,
)

fun RouteEntity.toDomain() = Route(
    id = id, name = name, description = description, sportType = sportType,
    startPoint = LatLng(startLat, startLng), endPoint = LatLng(endLat, endLng),
    waypoints = waypointsJson.toWaypointList(), elevationProfile = elevationProfileJson.toElevationList(),
    totalDistanceKm = totalDistanceKm, totalElevationGainMeters = totalElevationGainMeters,
    totalElevationLossMeters = totalElevationLossMeters, isRoundTrip = isRoundTrip,
    createdAt = createdAt,
)

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
    status = com.you.plot.core.domain.entity.SessionStatus.valueOf(status),
    startedAtMillis = startedAtMillis, elapsedTimeSeconds = elapsedTimeSeconds,
    currentLocation = if (currentLat != null && currentLng != null) LatLng(currentLat, currentLng) else null,
    currentSpeedKmh = currentSpeedKmh, distanceCoveredKm = distanceCoveredKm,
    waypointProgress = waypointProgressJson.toWaypointProgressList(),
    estimatedCompletionMillis = estimatedCompletionMillis,
)
