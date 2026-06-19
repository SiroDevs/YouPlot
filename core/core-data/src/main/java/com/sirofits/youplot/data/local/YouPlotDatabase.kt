package com.sirofits.youplot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sirofits.youplot.data.local.dao.*
import com.sirofits.youplot.data.local.entity.*

@Database(
    entities = [
        RouteEntity::class,
        WaypointEntity::class,
        PlanEntity::class,
        PlanEventEntity::class,
        SessionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class YouPlotDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
    abstract fun waypointDao(): WaypointDao
    abstract fun planDao(): PlanDao
    abstract fun planEventDao(): PlanEventDao
    abstract fun sessionDao(): SessionDao

    companion object {
        const val DATABASE_NAME = "youplot.db"
    }
}
