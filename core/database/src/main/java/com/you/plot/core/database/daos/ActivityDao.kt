package com.you.plot.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.you.plot.core.database.model.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activitys WHERE planId = :planId ORDER BY startedAt DESC")
    fun getActivitysByPlanId(planId: Long): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activitys WHERE id = :id")
    suspend fun getActivityById(id: Long): ActivityEntity?

    @Query("SELECT * FROM activitys WHERE status = 'IN_PROGRESS' OR status = 'PAUSED' LIMIT 1")
    suspend fun getActiveActivity(): ActivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity): Long

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Query("DELETE FROM activitys WHERE id = :id")
    suspend fun deleteActivity(id: Long)
}