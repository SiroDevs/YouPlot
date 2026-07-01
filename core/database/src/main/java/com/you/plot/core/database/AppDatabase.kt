package com.you.plot.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.you.plot.core.database.daos.PlanDao
import com.you.plot.core.database.daos.EventDao
import com.you.plot.core.database.daos.ListingDao
import com.you.plot.core.database.daos.RouteDao
import com.you.plot.core.database.daos.ActivityDao
import com.you.plot.core.database.daos.StartPointDao
import com.you.plot.core.database.daos.WaypointDao
import com.you.plot.core.database.model.ListingEntity
import com.you.plot.core.database.model.ListingItemEntity
import com.you.plot.core.database.model.PlanEntity
import com.you.plot.core.database.model.EventEntity
import com.you.plot.core.database.model.RouteEntity
import com.you.plot.core.database.model.ActivityEntity
import com.you.plot.core.database.model.StartPointEntity
import com.you.plot.core.database.model.WaypointEntity

@Database(
    entities = [
        ActivityEntity::class,
        EventEntity::class,
        ListingEntity::class,
        ListingItemEntity::class,
        PlanEntity::class,
        RouteEntity::class,
        StartPointEntity::class,
        WaypointEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun eventDao(): EventDao
    abstract fun listingDao(): ListingDao
    abstract fun planDao(): PlanDao
    abstract fun routeDao(): RouteDao
    abstract fun startPointDao(): StartPointDao
    abstract fun waypointDao(): WaypointDao

    companion object {
        const val DATABASE_NAME = "youplot.db"
    }
}
