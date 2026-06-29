package com.you.plot.core.domain.usecase

import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.ActivitySession
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.repos.LocationRepo
import com.you.plot.core.domain.repos.PlanRepo
import com.you.plot.core.domain.repos.RouteRepo
import com.you.plot.core.domain.repos.SessionRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeRouteRepo : RouteRepo {
    private val routes = MutableStateFlow<List<Route>>(emptyList())
    private var nextId = 1L

    override fun getAllRoutes(): Flow<List<Route>> = routes.asStateFlow()

    override suspend fun getRouteById(id: Long): Route? = routes.value.firstOrNull { it.id == id }

    override suspend fun saveRoute(route: Route): Long {
        val id = if (route.id == 0L) nextId++ else route.id
        val stored = route.copy(id = id)
        routes.value = routes.value.filterNot { it.id == id } + stored
        return id
    }

    override suspend fun deleteRoute(id: Long) {
        routes.value = routes.value.filterNot { it.id == id }
    }

    override suspend fun updateRoute(route: Route) {
        routes.value = routes.value.map { if (it.id == route.id) route else it }
    }
}

class FakePlanRepo : PlanRepo {
    private val plans = MutableStateFlow<List<ActivityPlan>>(emptyList())
    private var nextId = 1L

    override fun getAllPlans(): Flow<List<ActivityPlan>> = plans.asStateFlow()

    override fun getPlansByRouteId(routeId: Long): Flow<List<ActivityPlan>> =
        plans.map { list -> list.filter { it.routeId == routeId } }

    override suspend fun getPlanById(id: Long): ActivityPlan? = plans.value.firstOrNull { it.id == id }

    override suspend fun savePlan(plan: ActivityPlan): Long {
        val id = if (plan.id == 0L) nextId++ else plan.id
        val stored = plan.copy(id = id)
        plans.value = plans.value.filterNot { it.id == id } + stored
        return id
    }

    override suspend fun deletePlan(id: Long) {
        plans.value = plans.value.filterNot { it.id == id }
    }

    override suspend fun updatePlan(plan: ActivityPlan) {
        plans.value = plans.value.map { if (it.id == plan.id) plan else it }
    }
}

class FakeSessionRepo : SessionRepo {
    private val sessions = MutableStateFlow<List<ActivitySession>>(emptyList())
    private var nextId = 1L

    override fun getSessionsByPlanId(planId: Long): Flow<List<ActivitySession>> =
        sessions.map { list -> list.filter { it.planId == planId } }

    override suspend fun getSessionById(id: Long): ActivitySession? =
        sessions.value.firstOrNull { it.id == id }

    override suspend fun getActiveSession(): ActivitySession? =
        sessions.value.firstOrNull {
            it.status == com.you.plot.core.common.entity.SessionStatus.IN_PROGRESS
        }

    override suspend fun saveSession(session: ActivitySession): Long {
        val id = if (session.id == 0L) nextId++ else session.id
        val stored = session.copy(id = id)
        sessions.value = sessions.value.filterNot { it.id == id } + stored
        return id
    }

    override suspend fun updateSession(session: ActivitySession) {
        sessions.value = sessions.value.map { if (it.id == session.id) session else it }
    }

    override suspend fun deleteSession(id: Long) {
        sessions.value = sessions.value.filterNot { it.id == id }
    }
}

class FakeLocationRepo(
    private val lastKnown: LatLng? = null,
    private val updates: Flow<LatLng> = kotlinx.coroutines.flow.emptyFlow(),
) : LocationRepo {
    override fun getLocationUpdates(intervalMs: Long): Flow<LatLng> = updates
    override suspend fun getLastKnownLocation(): LatLng? = lastKnown
}
