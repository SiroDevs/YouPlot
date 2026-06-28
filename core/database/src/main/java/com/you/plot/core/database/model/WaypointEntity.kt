package com.you.plot.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val distanceFromStartKm: Double = 0.0,
    val isStopPlanned: Boolean,
)