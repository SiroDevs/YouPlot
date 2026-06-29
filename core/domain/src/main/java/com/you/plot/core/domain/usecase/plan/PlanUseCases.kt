package com.you.plot.core.domain.usecase.plan

import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.PlanEvent
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
class GeneratePlanEventsUseCase @Inject constructor(
    private val routeRepo: RouteRepo,
) {
    suspend operator fun invoke(plan: ActivityPlan): List<PlanEvent> {
        val route = routeRepo.getRouteById(plan.routeId) ?: return emptyList()
        val events = mutableListOf<PlanEvent>()
        val days = plan.numberOfDays.coerceAtLeast(1)
        val speed = plan.avgSpeedKmh.coerceAtLeast(0.1)
        val distancePerDay = if (plan.avgDistancePerDayKm > 0)
            plan.avgDistancePerDayKm else route.totalDistanceKm / days

        var dayStart = plan.startDateMillis
        var cumulativeDistance = 0.0
        var orderIndex = 0
        // Synthetic negative ids keep generated rows uniquely keyable in the UI
        // before they're persisted (the DB later assigns positive autoincrement ids).
        fun nextSyntheticId() = -(orderIndex + 1L)

        for (day in 1..days) {
            events += PlanEvent(
                id = nextSyntheticId(),
                planId = plan.id,
                dayNumber = day,
                name = "Begin activity",
                plannedTimeMillis = dayStart,
                durationMinutes = 0,
                distanceCoveredKm = cumulativeDistance,
                orderIndex = orderIndex++,
            )

            val dayEndDistance = cumulativeDistance + distancePerDay
            val waypointCount = route.waypoints.size.coerceAtLeast(1)
            route.waypoints
                .filter { it.orderIndex > 0 }
                .forEach { waypoint ->
                    val waypointDistance = waypoint.orderIndex.toDouble() /
                            waypointCount * route.totalDistanceKm
                    if (waypointDistance in cumulativeDistance..dayEndDistance) {
                        val hoursFromStart = (waypointDistance - cumulativeDistance) / speed
                        events += PlanEvent(
                            id = nextSyntheticId(),
                            planId = plan.id,
                            dayNumber = day,
                            name = waypoint.name,
                            waypointId = waypoint.id,
                            plannedTimeMillis = dayStart + (hoursFromStart * 3_600_000).toLong(),
                            durationMinutes = if (waypoint.isStopPlanned) 15 else 0,
                            distanceCoveredKm = waypointDistance,
                            orderIndex = orderIndex++,
                        )
                    }
                }

            val hoursForDay = distancePerDay / speed
            val dayEndMillis = dayStart + (hoursForDay * 3_600_000).toLong()

            events += PlanEvent(
                id = nextSyntheticId(),
                planId = plan.id,
                dayNumber = day,
                name = "End of activity — Day $day",
                plannedTimeMillis = dayEndMillis,
                durationMinutes = 0,
                distanceCoveredKm = dayEndDistance.coerceAtMost(route.totalDistanceKm),
                orderIndex = orderIndex++,
            )

            cumulativeDistance = dayEndDistance
            dayStart = dayEndMillis + 10 * 3_600_000L
        }
        return events
    }
}
