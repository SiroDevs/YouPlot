package com.you.plot.core.domain.repos

import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.domain.entity.ActivityActivity
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.entity.StartPoint
import kotlinx.coroutines.flow.Flow

interface RouteRepo {
    fun getAllRoutes(): Flow<List<Route>>
    fun getFavoriteRoutes(): Flow<List<Route>>
    fun getTrashedRoutes(): Flow<List<Route>>
    suspend fun getRouteById(id: Long): Route?
    suspend fun saveRoute(route: Route): Long
    suspend fun deleteRoute(id: Long)
    suspend fun updateRoute(route: Route)
    suspend fun softDeleteRoute(id: Long)
    suspend fun restoreRoute(id: Long)
    suspend fun setRouteFavorite(id: Long, favorite: Boolean)
    suspend fun countActivePlansForRoute(routeId: Long): Int
    suspend fun purgeExpiredRoutes(cutoff: Long)
}

interface PlanRepo {
    fun getAllPlans(): Flow<List<ActivityPlan>>
    fun getFavoritePlans(): Flow<List<ActivityPlan>>
    fun getTrashedPlans(): Flow<List<ActivityPlan>>
    fun getPlansByRouteId(routeId: Long): Flow<List<ActivityPlan>>
    suspend fun getPlanById(id: Long): ActivityPlan?
    suspend fun savePlan(plan: ActivityPlan): Long
    suspend fun deletePlan(id: Long)
    suspend fun updatePlan(plan: ActivityPlan)
    suspend fun softDeletePlan(id: Long)
    suspend fun restorePlan(id: Long)
    suspend fun setPlanFavorite(id: Long, favorite: Boolean)
    suspend fun purgeExpiredPlans(cutoff: Long)
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

interface StartPointRepo {
    fun getAll(): Flow<List<StartPoint>>
    fun getFavorites(): Flow<List<StartPoint>>
    fun getTrashed(): Flow<List<StartPoint>>
    suspend fun getById(id: Long): StartPoint?
    /** Increments the existing start point at this location, or creates a new one. */
    suspend fun recordUsage(name: String, position: LatLng, countryCode: String): Long
    suspend fun save(startPoint: StartPoint): Long
    suspend fun update(startPoint: StartPoint)
    suspend fun incrementUsage(id: Long)
    suspend fun softDelete(id: Long)
    suspend fun restore(id: Long)
    suspend fun setFavorite(id: Long, favorite: Boolean)
    suspend fun deletePermanently(id: Long)
    suspend fun purgeExpired(cutoff: Long)
}
