package com.you.plot.core.domain.entity

enum class SportType { RUNNING, CYCLING, HIKING, WALKING }

data class LatLng(val latitude: Double, val longitude: Double)

data class Waypoint(
    val id: Long = 0L,
    val routeId: Long,
    val name: String,
    val position: LatLng,
    val orderIndex: Int,
    val elevationMeters: Double = 0.0,
    val isStopPlanned: Boolean = false,
)

data class ElevationPoint(val distanceKm: Double, val elevationMeters: Double)

data class Route(
    val id: Long = 0L,
    val name: String,
    val description: String = "",
    val sportType: SportType,
    val startPoint: LatLng,
    val endPoint: LatLng,
    val waypoints: List<Waypoint> = emptyList(),
    val elevationProfile: List<ElevationPoint> = emptyList(),
    val totalDistanceKm: Double,
    val totalElevationGainMeters: Double = 0.0,
    val totalElevationLossMeters: Double = 0.0,
    val isRoundTrip: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

data class PlanEvent(
    val id: Long = 0L,
    val planId: Long,
    val dayNumber: Int,
    val name: String,
    val waypointId: Long? = null,
    val plannedTimeMillis: Long,
    val durationMinutes: Int = 0,
    val distanceCoveredKm: Double = 0.0,
    val orderIndex: Int,
)

data class ActivityPlan(
    val id: Long = 0L,
    val routeId: Long,
    val name: String,
    val description: String = "",
    val startDateMillis: Long,
    val numberOfDays: Int,
    val avgSpeedKmh: Double,
    val avgDistancePerDayKm: Double,
    val events: List<PlanEvent> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

enum class SessionStatus { NOT_STARTED, IN_PROGRESS, PAUSED, COMPLETED }

data class WaypointProgress(
    val waypoint: Waypoint,
    val plannedArrivalMillis: Long,
    val estimatedArrivalMillis: Long,
    val distanceRemainingKm: Double,
    val isReached: Boolean = false,
    val wasSkipped: Boolean = false,
)

data class ActivitySession(
    val id: Long = 0L,
    val planId: Long,
    val routeId: Long,
    val status: SessionStatus = SessionStatus.NOT_STARTED,
    val startedAtMillis: Long? = null,
    val elapsedTimeSeconds: Long = 0L,
    val currentLocation: LatLng? = null,
    val currentSpeedKmh: Double = 0.0,
    val distanceCoveredKm: Double = 0.0,
    val waypointProgress: List<WaypointProgress> = emptyList(),
    val estimatedCompletionMillis: Long? = null,
)
