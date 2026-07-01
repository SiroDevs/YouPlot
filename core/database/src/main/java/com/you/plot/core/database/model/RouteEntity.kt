package com.you.plot.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.you.plot.core.common.entity.SportType

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
    val waypoints: String,
    val elevationProfile: String,
    val totalDist: Double,
    val elevationGain: Double,
    val elevationLoss: Double,
    val isRoundTrip: Boolean,
    val createdAt: Long,
    val polyline: String = "[]",
    val isFavorite: Boolean = false,
    val deletedAt: Long? = null,
)
