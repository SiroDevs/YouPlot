package com.you.plot.core.domain.repos

import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.domain.entity.ActivityActivity
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.Route
import kotlinx.coroutines.flow.Flow

interface RouteRepo {
    fun getAllRoutes(): Flow<List<Route>>
    suspend fun getRouteById(id: Long): Route?
    suspend fun saveRoute(route: Route): Long
    suspend fun deleteRoute(id: Long)
    suspend fun updateRoute(route: Route)
}

interface PlanRepo {
    fun getAllPlans(): Flow<List<ActivityPlan>>
    fun getPlansByRouteId(routeId: Long): Flow<List<ActivityPlan>>
    suspend fun getPlanById(id: Long): ActivityPlan?
    suspend fun savePlan(plan: ActivityPlan): Long
    suspend fun deletePlan(id: Long)
    suspend fun updatePlan(plan: ActivityPlan)
}

interface ActivityRepo {
    fun getActivitysByPlanId(planId: Long): Flow<List<ActivityActivity>>
    suspend fun getActivityById(id: Long): ActivityActivity?
    suspend fun getActiveActivity(): ActivityActivity?
    suspend fun saveActivity(activity: ActivityActivity): Long
    suspend fun updateActivity(activity: ActivityActivity)
    suspend fun deleteActivity(id: Long)
}

interface LocationRepo {
    fun getLocationUpdates(intervalMs: Long): Flow<LatLng>
    suspend fun getLastKnownLocation(): LatLng?
}
