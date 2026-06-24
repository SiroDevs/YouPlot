package com.you.plot.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.you.plot.core.domain.entity.SportType

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String,
    val sportType: SportType,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val waypointsJson: String,
    val elevationProfileJson: String,
    val totalDistanceKm: Double,
    val totalElevationGainMeters: Double,
    val totalElevationLossMeters: Double,
    val isRoundTrip: Boolean,
    val createdAt: Long,
)

@Entity(
    tableName = "waypoints",
    foreignKeys = [
        ForeignKey(
            entity = RouteEntity::class,
            parentColumns = ["id"],
            childColumns = ["routeId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("routeId")],
)
data class WaypointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val routeId: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val orderIndex: Int,
    val elevationMeters: Double,
    val isStopPlanned: Boolean,
)

@Entity(
    tableName = "plans",
    foreignKeys = [
        ForeignKey(
            entity = RouteEntity::class,
            parentColumns = ["id"],
            childColumns = ["routeId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("routeId")],
)
data class PlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val routeId: Long,
    val name: String,
    val description: String,
    val startDateMillis: Long,
    val numberOfDays: Int,
    val avgSpeedKmh: Double,
    val avgDistancePerDayKm: Double,
    val createdAt: Long,
)

@Entity(
    tableName = "plan_events",
    foreignKeys = [
        ForeignKey(
            entity = PlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("planId")],
)
data class PlanEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val planId: Long,
    val dayNumber: Int,
    val name: String,
    val waypointId: Long?,
    val plannedTimeMillis: Long,
    val durationMinutes: Int,
    val distanceCoveredKm: Double,
    val orderIndex: Int,
)

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = PlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("planId")],
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val planId: Long,
    val routeId: Long,
    val status: String,
    val startedAtMillis: Long?,
    val elapsedTimeSeconds: Long,
    val currentLat: Double?,
    val currentLng: Double?,
    val currentSpeedKmh: Double,
    val distanceCoveredKm: Double,
    val waypointProgressJson: String,
    val estimatedCompletionMillis: Long?,
)
