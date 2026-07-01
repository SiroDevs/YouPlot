package com.you.plot.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.you.plot.core.database.model.PlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans WHERE deletedAt IS NULL AND isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoritePlans(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getTrashedPlans(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans WHERE routeId = :routeId AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun getPlansByRouteId(routeId: Long): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans WHERE id = :id")
    suspend fun getPlanById(id: Long): PlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: PlanEntity): Long

    @Update
    suspend fun updatePlan(plan: PlanEntity)

    @Query("UPDATE plans SET deletedAt = :now WHERE id = :id")
    suspend fun softDeletePlan(id: Long, now: Long)

    @Query("UPDATE plans SET deletedAt = NULL WHERE id = :id")
    suspend fun restorePlan(id: Long)

    @Query("UPDATE plans SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("DELETE FROM plans WHERE id = :id")
    suspend fun deletePlan(id: Long)

    @Query("DELETE FROM plans WHERE deletedAt IS NOT NULL AND deletedAt < :cutoff")
    suspend fun purgeExpired(cutoff: Long)
}
