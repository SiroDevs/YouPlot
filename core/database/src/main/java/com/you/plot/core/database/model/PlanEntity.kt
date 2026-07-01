package com.you.plot.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.you.plot.core.common.entity.SportType

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
    val sportType: SportType = SportType.RUNNING,
    val startDate: Long,
    val numberOfDays: Int,
    val avgSpeed: Double,
    val avgDailyDist: Double,
    val createdAt: Long,
    val isFavorite: Boolean = false,
    val deletedAt: Long? = null,
)