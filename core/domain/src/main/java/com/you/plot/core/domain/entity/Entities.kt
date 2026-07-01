package com.you.plot.core.domain.entity

import com.you.plot.core.common.entity.ElevationPoint
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.ActivityStatus
import com.you.plot.core.common.entity.SportType

data class WaypointSearchResult(
    val displayName: String,
    val latLng: LatLng,
)

data class Waypoint(
    val id: Long = 0L,
    val routeId: Long,
    val name: String,
    val position: LatLng,
    val orderIndex: Int,
    val elevation: Double = 0.0,
    val distFromStart: Double = 0.0,
    val isStopPlanned: Boolean = false,
    val countryCode: String = "",
)

data class Route(
    val id: Long = 0L,
    val name: String,
    val description: String = "",
    val sportType: SportType,
    val startPoint: LatLng,
    val endPoint: LatLng,
    val waypoints: List<Waypoint> = emptyList(),
    val elevationProfile: List<ElevationPoint> = emptyList(),
    val totalDist: Double,
    val elevationGain: Double = 0.0,
    val elevationLoss: Double = 0.0,
    val isRoundTrip: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val polyline: List<LatLng> = emptyList(),
    val isFavorite: Boolean = false,
    val deletedAt: Long? = null,
)

data class Event(
    val id: Long = 0L,
    val planId: Long,
    val dayNumber: Int,
    val name: String,
    val waypointId: Long? = null,
    val plannedTime: Long,
    val duration: Int = 0,
    val distCovered: Double = 0.0,
    val orderIndex: Int,
)

data class ActivityPlan(
    val id: Long = 0L,
    val routeId: Long,
    val name: String,
    val description: String = "",
    val sportType: SportType = SportType.RUNNING,
    val startDate: Long,
    val numberOfDays: Int,
    val avgSpeed: Double,
    val avgDailyDist: Double,
    val events: List<Event> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val deletedAt: Long? = null,
)

data class StartPoint(
    val id: Long = 0L,
    val name: String,
    val position: LatLng,
    val countryCode: String = "",
    val usageCount: Int = 0,
    val lastUsedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val deletedAt: Long? = null,
)

data class WaypointProgress(
    val waypoint: Waypoint,
    val plannedArrival: Long,
    val estimatedArrival: Long,
    val distRemaining: Double,
    val isReached: Boolean = false,
    val wasSkipped: Boolean = false,
)

data class ActivityActivity(
    val id: Long = 0L,
    val planId: Long,
    val routeId: Long,
    val status: ActivityStatus = ActivityStatus.NOT_STARTED,
    val startedAt: Long? = null,
    val elapsedTime: Long = 0L,
    val currentLocation: LatLng? = null,
    val currentSpeed: Double = 0.0,
    val distCovered: Double = 0.0,
    val waypointProgress: List<WaypointProgress> = emptyList(),
    val estimatedCompletion: Long? = null,
)
