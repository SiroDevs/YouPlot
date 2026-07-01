package com.you.plot.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.you.plot.core.database.model.StartPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StartPointDao {
    @Query(
        "SELECT * FROM start_points WHERE deletedAt IS NULL " +
            "ORDER BY usageCount DESC, lastUsedAt DESC, createdAt DESC"
    )
    fun getAll(): Flow<List<StartPointEntity>>

    @Query("SELECT * FROM start_points WHERE deletedAt IS NULL AND isFavorite = 1 " +
        "ORDER BY usageCount DESC, lastUsedAt DESC")
    fun getFavorites(): Flow<List<StartPointEntity>>

    @Query("SELECT * FROM start_points WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getTrashed(): Flow<List<StartPointEntity>>

    @Query("SELECT * FROM start_points WHERE id = :id")
    suspend fun getById(id: Long): StartPointEntity?

    /**
     * Looks up an existing start point at approximately the same location so the caller
     * can increment usage instead of creating duplicates. Uses a small lat/lng epsilon
     * (~11m).
     */
    @Query(
        "SELECT * FROM start_points WHERE deletedAt IS NULL " +
            "AND ABS(latitude - :lat) < 0.0001 AND ABS(longitude - :lng) < 0.0001 LIMIT 1"
    )
    suspend fun findNearby(lat: Double, lng: Double): StartPointEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(startPoint: StartPointEntity): Long

    @Update
    suspend fun update(startPoint: StartPointEntity)

    @Query("UPDATE start_points SET usageCount = usageCount + 1, lastUsedAt = :now WHERE id = :id")
    suspend fun incrementUsage(id: Long, now: Long)

    @Query("UPDATE start_points SET deletedAt = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long)

    @Query("UPDATE start_points SET deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("UPDATE start_points SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("DELETE FROM start_points WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("DELETE FROM start_points WHERE deletedAt IS NOT NULL AND deletedAt < :cutoff")
    suspend fun purgeExpired(cutoff: Long)
}
