package com.you.plot.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.you.plot.core.database.model.RouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun getAllRoutes(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE deletedAt IS NULL AND isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteRoutes(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getTrashedRoutes(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE id = :id")
    suspend fun getRouteById(id: Long): RouteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity): Long

    @Update
    suspend fun updateRoute(route: RouteEntity)

    @Query("UPDATE routes SET deletedAt = :now WHERE id = :id")
    suspend fun softDeleteRoute(id: Long, now: Long)

    @Query("UPDATE routes SET deletedAt = NULL WHERE id = :id")
    suspend fun restoreRoute(id: Long)

    @Query("UPDATE routes SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("DELETE FROM routes WHERE id = :id")
    suspend fun deleteRoute(id: Long)

    @Query("DELETE FROM routes WHERE deletedAt IS NOT NULL AND deletedAt < :cutoff")
    suspend fun purgeExpired(cutoff: Long)

    @Query("SELECT COUNT(*) FROM plans WHERE routeId = :routeId AND deletedAt IS NULL")
    suspend fun countActivePlansForRoute(routeId: Long): Int
}
