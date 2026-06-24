package com.you.plot.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.you.plot.core.database.daos.*
import com.you.plot.core.database.model.*

@Database(
    entities = [
        RouteEntity::class,
        WaypointEntity::class,
        PlanEntity::class,
        PlanEventEntity::class,
        SessionEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
    abstract fun waypointDao(): WaypointDao
    abstract fun planDao(): PlanDao
    abstract fun planEventDao(): PlanEventDao
    abstract fun sessionDao(): SessionDao

    companion object {
        const val DATABASE_NAME = "youplot.db"
    }
}
