package com.sirofits.youplot.data.local.dao

import androidx.room.*
import com.sirofits.youplot.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes ORDER BY createdAt DESC")
    fun getAllRoutes(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE id = :id")
    suspend fun getRouteById(id: Long): RouteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity): Long

    @Update
    suspend fun updateRoute(route: RouteEntity)

    @Query("DELETE FROM routes WHERE id = :id")
    suspend fun deleteRoute(id: Long)
}

@Dao
interface WaypointDao {
    @Query("SELECT * FROM waypoints WHERE routeId = :routeId ORDER BY orderIndex")
    suspend fun getWaypointsByRoute(routeId: Long): List<WaypointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaypoints(waypoints: List<WaypointEntity>)

    @Query("DELETE FROM waypoints WHERE routeId = :routeId")
    suspend fun deleteWaypointsByRoute(routeId: Long)
}

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans WHERE routeId = :routeId ORDER BY createdAt DESC")
    fun getPlansByRouteId(routeId: Long): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans WHERE id = :id")
    suspend fun getPlanById(id: Long): PlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: PlanEntity): Long

    @Update
    suspend fun updatePlan(plan: PlanEntity)

    @Query("DELETE FROM plans WHERE id = :id")
    suspend fun deletePlan(id: Long)
}

@Dao
interface PlanEventDao {
    @Query("SELECT * FROM plan_events WHERE planId = :planId ORDER BY dayNumber, orderIndex")
    suspend fun getEventsByPlan(planId: Long): List<PlanEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<PlanEventEntity>)

    @Query("DELETE FROM plan_events WHERE planId = :planId")
    suspend fun deleteEventsByPlan(planId: Long)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE planId = :planId ORDER BY startedAtMillis DESC")
    fun getSessionsByPlanId(planId: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SessionEntity?

    @Query("SELECT * FROM sessions WHERE status = 'IN_PROGRESS' OR status = 'PAUSED' LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)
}
