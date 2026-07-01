package com.you.plot.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.you.plot.core.database.model.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE planId = :planId ORDER BY dayNumber, orderIndex")
    suspend fun getEventsByPlan(planId: Long): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Query("DELETE FROM events WHERE planId = :planId")
    suspend fun deleteEventsByPlan(planId: Long)
}
