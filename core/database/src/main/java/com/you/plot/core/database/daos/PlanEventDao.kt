/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.you.plot.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.you.plot.core.database.model.PlanEventEntity

@Dao
interface PlanEventDao {
    @Query("SELECT * FROM plan_events WHERE planId = :planId ORDER BY dayNumber, orderIndex")
    suspend fun getEventsByPlan(planId: Long): List<PlanEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<PlanEventEntity>)

    @Query("DELETE FROM plan_events WHERE planId = :planId")
    suspend fun deleteEventsByPlan(planId: Long)
}
