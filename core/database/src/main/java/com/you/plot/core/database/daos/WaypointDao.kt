package com.you.plot.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.you.plot.core.database.model.WaypointEntity

@Dao
interface WaypointDao {
    @Query("SELECT * FROM waypoints WHERE routeId = :routeId ORDER BY orderIndex")
    suspend fun getWaypointsByRoute(routeId: Long): List<WaypointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaypoints(waypoints: List<WaypointEntity>)

    @Query("DELETE FROM waypoints WHERE routeId = :routeId")
    suspend fun deleteWaypointsByRoute(routeId: Long)
}
