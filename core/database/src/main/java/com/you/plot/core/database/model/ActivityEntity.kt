package com.you.plot.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activitys",
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
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val planId: Long,
    val routeId: Long,
    val status: String,
    val startedAt: Long?,
    val elapsedTime: Long,
    val currentLat: Double?,
    val currentLng: Double?,
    val currentSpeed: Double,
    val distCovered: Double,
    val waypointProgress: String,
    val estimatedCompletion: Long?,
)
