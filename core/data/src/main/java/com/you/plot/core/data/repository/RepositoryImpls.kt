package com.you.plot.core.data.repository

import com.you.plot.core.database.converter.*
import com.you.plot.core.database.daos.*
import com.you.plot.core.domain.entity.*
import com.you.plot.core.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RouteRepositoryImpl @Inject constructor(
    private val routeDao: RouteDao,
) : RouteRepository {
    override fun getAllRoutes(): Flow<List<Route>> =
        routeDao.getAllRoutes().map { list -> list.map { it.toDomain() } }

    override suspend fun getRouteById(id: Long): Route? =
        routeDao.getRouteById(id)?.toDomain()

    override suspend fun saveRoute(route: Route): Long =
        routeDao.insertRoute(route.toEntity())

    override suspend fun deleteRoute(id: Long) = routeDao.deleteRoute(id)

    override suspend fun updateRoute(route: Route) = routeDao.updateRoute(route.toEntity())
}

class PlanRepositoryImpl @Inject constructor(
    private val planDao: PlanDao,
    private val planEventDao: PlanEventDao,
) : PlanRepository {
    override fun getAllPlans(): Flow<List<ActivityPlan>> =
        planDao.getAllPlans().map { list ->
            list.map { entity ->
                val events = planEventDao.getEventsByPlan(entity.id)
                entity.toDomain(events)
            }
        }

    override fun getPlansByRouteId(routeId: Long): Flow<List<ActivityPlan>> =
        planDao.getPlansByRouteId(routeId).map { list ->
            list.map { entity ->
                val events = planEventDao.getEventsByPlan(entity.id)
                entity.toDomain(events)
            }
        }

    override suspend fun getPlanById(id: Long): ActivityPlan? {
        val entity = planDao.getPlanById(id) ?: return null
        val events = planEventDao.getEventsByPlan(id)
        return entity.toDomain(events)
    }

    override suspend fun savePlan(plan: ActivityPlan): Long {
        val planId = planDao.insertPlan(plan.toEntity())
        val events = plan.events.map { it.copy(planId = planId).toEntity() }
        planEventDao.insertEvents(events)
        return planId
    }

    override suspend fun deletePlan(id: Long) = planDao.deletePlan(id)

    override suspend fun updatePlan(plan: ActivityPlan) {
        planDao.updatePlan(plan.toEntity())
        planEventDao.deleteEventsByPlan(plan.id)
        planEventDao.insertEvents(plan.events.map { it.toEntity() })
    }
}

class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
) : SessionRepository {
    override fun getSessionsByPlanId(planId: Long): Flow<List<ActivitySession>> =
        sessionDao.getSessionsByPlanId(planId).map { list -> list.map { it.toDomain() } }

    override suspend fun getSessionById(id: Long): ActivitySession? =
        sessionDao.getSessionById(id)?.toDomain()

    override suspend fun getActiveSession(): ActivitySession? =
        sessionDao.getActiveSession()?.toDomain()

    override suspend fun saveSession(session: ActivitySession): Long =
        sessionDao.insertSession(session.toEntity())

    override suspend fun updateSession(session: ActivitySession) =
        sessionDao.updateSession(session.toEntity())

    override suspend fun deleteSession(id: Long) = sessionDao.deleteSession(id)
}
