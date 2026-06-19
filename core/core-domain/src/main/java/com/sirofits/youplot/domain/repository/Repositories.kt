package com.sirofits.youplot.domain.repository

import com.sirofits.youplot.domain.entity.ActivitySession
import com.sirofits.youplot.domain.entity.ActivityPlan
import com.sirofits.youplot.domain.entity.LatLng
import com.sirofits.youplot.domain.entity.Route
import kotlinx.coroutines.flow.Flow

interface RouteRepository {
    fun getAllRoutes(): Flow<List<Route>>
    suspend fun getRouteById(id: Long): Route?
    suspend fun saveRoute(route: Route): Long
    suspend fun deleteRoute(id: Long)
    suspend fun updateRoute(route: Route)
}

interface PlanRepository {
    fun getAllPlans(): Flow<List<ActivityPlan>>
    fun getPlansByRouteId(routeId: Long): Flow<List<ActivityPlan>>
    suspend fun getPlanById(id: Long): ActivityPlan?
    suspend fun savePlan(plan: ActivityPlan): Long
    suspend fun deletePlan(id: Long)
    suspend fun updatePlan(plan: ActivityPlan)
}

interface SessionRepository {
    fun getSessionsByPlanId(planId: Long): Flow<List<ActivitySession>>
    suspend fun getSessionById(id: Long): ActivitySession?
    suspend fun getActiveSession(): ActivitySession?
    suspend fun saveSession(session: ActivitySession): Long
    suspend fun updateSession(session: ActivitySession)
    suspend fun deleteSession(id: Long)
}

/** Wraps FusedLocationProviderClient as a clean-arch interface */
interface LocationRepository {
    fun getLocationUpdates(intervalMs: Long): Flow<LatLng>
    suspend fun getLastKnownLocation(): LatLng?
}
