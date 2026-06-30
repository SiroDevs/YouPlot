package com.you.plot.core.domain.usecase.plan

import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.Event
import com.you.plot.core.domain.repos.PlanRepo
import com.you.plot.core.domain.repos.RouteRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPlansUseCase @Inject constructor(
    private val repository: PlanRepo
) {
    operator fun invoke(): Flow<List<ActivityPlan>> = repository.getAllPlans()
}

class GetPlansByRouteUseCase @Inject constructor(
    private val repository: PlanRepo
) {
    operator fun invoke(routeId: Long): Flow<List<ActivityPlan>> =
        repository.getPlansByRouteId(routeId)
}

class GetPlanByIdUseCase @Inject constructor(
    private val repository: PlanRepo
) {
    suspend operator fun invoke(id: Long): ActivityPlan? = repository.getPlanById(id)
}

class SavePlanUseCase @Inject constructor(
    private val planRepo: PlanRepo,
    private val routeRepo: RouteRepo,
) {
    suspend operator fun invoke(plan: ActivityPlan): Long {
        require(plan.name.isNotBlank()) { "Plan name cannot be empty" }
        requireNotNull(routeRepo.getRouteById(plan.routeId)) {
            "Route ${plan.routeId} does not exist"
        }
        return planRepo.savePlan(plan)
    }
}

class DeletePlanUseCase @Inject constructor(
    private val repository: PlanRepo
) {
    suspend operator fun invoke(id: Long) = repository.deletePlan(id)
}

/** Generates a default daily schedule from route + speed parameters */
class GenerateEventsUseCase @Inject constructor(
    private val routeRepo: RouteRepo,
) {
    suspend operator fun invoke(plan: ActivityPlan): List<Event> {
        val route = routeRepo.getRouteById(plan.routeId) ?: return emptyList()
        val events = mutableListOf<Event>()
        val distancePerDay = if (plan.avgDistPerDay > 0)
            plan.avgDistPerDay else route.totalDist / plan.numberOfDays

        var dayStart = plan.startDateMillis
        var cumulativeDistance = 0.0
        var orderIndex = 0

        for (day in 1..plan.numberOfDays) {
            events += Event(
                planId = plan.id,
                dayNumber = day,
                name = "Begin activity",
                plannedTime = dayStart,
                duration = 0,
                distCovered = cumulativeDistance,
                orderIndex = orderIndex++,
            )

            val dayEndDistance = cumulativeDistance + distancePerDay
            route.waypoints
                .filter { it.orderIndex > 0 }
                .forEach { waypoint ->
                    val waypointDistance = waypoint.orderIndex.toDouble() /
                            route.waypoints.size * route.totalDist
                    if (waypointDistance in cumulativeDistance..dayEndDistance) {
                        val hoursFromStart = (waypointDistance - cumulativeDistance) / plan.avgSpeed
                        events += Event(
                            planId = plan.id,
                            dayNumber = day,
                            name = waypoint.name,
                            waypointId = waypoint.id,
                            plannedTime = dayStart + (hoursFromStart * 3_600_000).toLong(),
                            duration = if (waypoint.isStopPlanned) 15 else 0,
                            distCovered = waypointDistance,
                            orderIndex = orderIndex++,
                        )
                    }
                }

            val hoursForDay = distancePerDay / plan.avgSpeed
            val dayEndMillis = dayStart + (hoursForDay * 3_600_000).toLong()

            events += Event(
                planId = plan.id,
                dayNumber = day,
                name = "End of activity — Day $day",
                plannedTime = dayEndMillis,
                duration = 0,
                distCovered = dayEndDistance.coerceAtMost(route.totalDist),
                orderIndex = orderIndex++,
            )

            cumulativeDistance = dayEndDistance
            dayStart = dayEndMillis + 10 * 3_600_000L
        }
        return events
    }
}
