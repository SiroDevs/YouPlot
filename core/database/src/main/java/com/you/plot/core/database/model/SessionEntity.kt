package com.you.plot.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
