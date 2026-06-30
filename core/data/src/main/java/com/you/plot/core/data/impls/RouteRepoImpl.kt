package com.you.plot.core.data.impls

import com.you.plot.core.database.converter.toDomain
import com.you.plot.core.database.converter.toEntity
import com.you.plot.core.database.daos.PlanDao
import com.you.plot.core.database.daos.EventDao
import com.you.plot.core.database.daos.RouteDao
import com.you.plot.core.database.daos.ActivityDao
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.ActivityActivity
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.repos.PlanRepo
import com.you.plot.core.domain.repos.RouteRepo
import com.you.plot.core.domain.repos.ActivityRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RouteRepoImpl @Inject constructor(
    private val routeDao: RouteDao,
) : RouteRepo {
    override fun getAllRoutes(): Flow<List<Route>> =
        routeDao.getAllRoutes().map { list -> list.map { it.toDomain() } }

    override suspend fun getRouteById(id: Long): Route? =
        routeDao.getRouteById(id)?.toDomain()

    override suspend fun saveRoute(route: Route): Long =
        routeDao.insertRoute(route.toEntity())

    override suspend fun deleteRoute(id: Long) = routeDao.deleteRoute(id)

    override suspend fun updateRoute(route: Route) = routeDao.updateRoute(route.toEntity())
}

class PlanRepoImpl @Inject constructor(
    private val planDao: PlanDao,
    private val eventDao: EventDao,
) : PlanRepo {
    override fun getAllPlans(): Flow<List<ActivityPlan>> =
        planDao.getAllPlans().map { list ->
            list.map { entity ->
                val events = eventDao.getEventsByPlan(entity.id)
                entity.toDomain(events)
            }
        }

    override fun getPlansByRouteId(routeId: Long): Flow<List<ActivityPlan>> =
        planDao.getPlansByRouteId(routeId).map { list ->
            list.map { entity ->
                val events = eventDao.getEventsByPlan(entity.id)
                entity.toDomain(events)
            }
        }

    override suspend fun getPlanById(id: Long): ActivityPlan? {
        val entity = planDao.getPlanById(id) ?: return null
        val events = eventDao.getEventsByPlan(id)
        return entity.toDomain(events)
    }

    override suspend fun savePlan(plan: ActivityPlan): Long {
        val planId = planDao.insertPlan(plan.toEntity())
        val events = plan.events.map { it.copy(planId = planId).toEntity() }
        eventDao.insertEvents(events)
        return planId
    }

    override suspend fun deletePlan(id: Long) = planDao.deletePlan(id)

    override suspend fun updatePlan(plan: ActivityPlan) {
        planDao.updatePlan(plan.toEntity())
        eventDao.deleteEventsByPlan(plan.id)
        eventDao.insertEvents(plan.events.map { it.toEntity() })
    }
}

class ActivityRepoImpl @Inject constructor(
    private val activityDao: ActivityDao,
) : ActivityRepo {
    override fun getActivitysByPlanId(planId: Long): Flow<List<ActivityActivity>> =
        activityDao.getActivitysByPlanId(planId).map { list -> list.map { it.toDomain() } }

    override suspend fun getActivityById(id: Long): ActivityActivity? =
        activityDao.getActivityById(id)?.toDomain()

    override suspend fun getActiveActivity(): ActivityActivity? =
        activityDao.getActiveActivity()?.toDomain()

    override suspend fun saveActivity(activity: ActivityActivity): Long =
        activityDao.insertActivity(activity.toEntity())

    override suspend fun updateActivity(activity: ActivityActivity) =
        activityDao.updateActivity(activity.toEntity())

    override suspend fun deleteActivity(id: Long) = activityDao.deleteActivity(id)
}
