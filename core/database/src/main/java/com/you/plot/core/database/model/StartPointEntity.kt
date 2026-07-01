package com.you.plot.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "start_points")
data class StartPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val countryCode: String = "",
    val usageCount: Int = 0,
    val lastUsedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val deletedAt: Long? = null,
)
