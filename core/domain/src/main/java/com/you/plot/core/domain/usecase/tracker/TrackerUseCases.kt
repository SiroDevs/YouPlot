package com.you.plot.core.domain.usecase.tracker

import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.ActivityStatus
import com.you.plot.core.domain.entity.ActivityActivity
import com.you.plot.core.domain.entity.WaypointProgress
import com.you.plot.core.domain.repos.PlanRepo
import com.you.plot.core.domain.repos.RouteRepo
import com.you.plot.core.domain.repos.ActivityRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.math.*

class StartActivityUseCase @Inject constructor(
    private val activityRepo: ActivityRepo,
    private val planRepo: PlanRepo,
    private val routeRepo: RouteRepo,
) {
    suspend operator fun invoke(planId: Long): Long {
        val plan = requireNotNull(planRepo.getPlanById(planId)) { "Plan not found" }
        val route = requireNotNull(routeRepo.getRouteById(plan.routeId)) { "Route not found" }

        val waypointProgress = route.waypoints.map { wp ->
            val event = plan.events.firstOrNull { it.waypointId == wp.id }
            WaypointProgress(
                waypoint = wp,
                plannedArrival = event?.plannedTime ?: System.currentTimeMillis(),
                estimatedArrival = event?.plannedTime ?: System.currentTimeMillis(),
                distRemaining = route.totalDist,
            )
        }

        val nowMs = System.currentTimeMillis()
        // Estimated completion = start + (total distance / avg speed) hours
        val estimatedCompletion =
            nowMs + ((route.totalDist / plan.avgSpeed) * 3_600_000L).toLong()

        val activity = ActivityActivity(
            planId = planId,
            routeId = plan.routeId,
            status = ActivityStatus.IN_PROGRESS,
            startedAt = nowMs,
            waypointProgress = waypointProgress,
            estimatedCompletion = estimatedCompletion,
        )
        return activityRepo.saveActivity(activity)
    }
}

class UpdateActivityLocationUseCase @Inject constructor(
    private val activityRepo: ActivityRepo,
) {
    suspend operator fun invoke(
        activityId: Long,
        newLocation: LatLng,
        speedKmh: Double,
        elapsedSeconds: Long,
    ) {
        val activity = activityRepo.getActivityById(activityId) ?: return
        val updated = activity.recalculate(newLocation, speedKmh, elapsedSeconds)
        activityRepo.updateActivity(updated)
    }
}

class PauseActivityUseCase @Inject constructor(private val activityRepo: ActivityRepo) {
    suspend operator fun invoke(activityId: Long) {
        val activity = activityRepo.getActivityById(activityId) ?: return
        activityRepo.updateActivity(activity.copy(status = ActivityStatus.PAUSED))
    }
}

class ResumeActivityUseCase @Inject constructor(private val activityRepo: ActivityRepo) {
    suspend operator fun invoke(activityId: Long) {
        val activity = activityRepo.getActivityById(activityId) ?: return
        activityRepo.updateActivity(activity.copy(status = ActivityStatus.IN_PROGRESS))
    }
}

class CompleteActivityUseCase @Inject constructor(private val activityRepo: ActivityRepo) {
    suspend operator fun invoke(activityId: Long) {
        val activity = activityRepo.getActivityById(activityId) ?: return
        activityRepo.updateActivity(activity.copy(status = ActivityStatus.COMPLETED))
    }
}

class GetActiveActivityUseCase @Inject constructor(private val activityRepo: ActivityRepo) {
    suspend operator fun invoke(): ActivityActivity? = activityRepo.getActiveActivity()
}

class GetActivitysByPlanUseCase @Inject constructor(private val activityRepo: ActivityRepo) {
    operator fun invoke(planId: Long): Flow<List<ActivityActivity>> =
        activityRepo.getActivitysByPlanId(planId)
}

// ─── recalculate: ETAs, distances, estimated completion ──────────────────────

fun ActivityActivity.recalculate(
    newLocation: LatLng,
    speedKmh: Double,
    elapsedSeconds: Long,
): ActivityActivity {
    val distanceDelta = currentLocation?.distanceTo(newLocation) ?: 0.0
    val newDistance = distCovered + distanceDelta
    val nowMs = System.currentTimeMillis()

    val updatedProgress = waypointProgress.map { wp ->
        if (wp.isReached) return@map wp
        val waypointFraction = wp.waypoint.orderIndex.toDouble() /
            waypointProgress.size.coerceAtLeast(1)
        val totalDist = waypointProgress.lastOrNull()?.distRemaining
            ?.plus(newDistance) ?: newDistance
        val remaining = (waypointFraction * totalDist - newDistance).coerceAtLeast(0.0)
        val etaMs = if (speedKmh > 0)
            nowMs + ((remaining / speedKmh) * 3_600_000L).toLong()
        else wp.estimatedArrival

        wp.copy(
            distRemaining = remaining,
            estimatedArrival = etaMs,
            isReached = remaining <= 0.05,
        )
    }

    // Recalculate estimated completion from current position and speed
    val lastWp = updatedProgress.lastOrNull()
    val distToFinish = lastWp?.distRemaining ?: 0.0
    val newCompletion = if (speedKmh > 0)
        nowMs + ((distToFinish / speedKmh) * 3_600_000L).toLong()
    else estimatedCompletion

    return copy(
        currentLocation = newLocation,
        currentSpeed = speedKmh,
        distCovered = newDistance,
        elapsedTime = elapsedSeconds,
        waypointProgress = updatedProgress,
        estimatedCompletion = newCompletion,
    )
}

/** Haversine distance between two LatLng points in km */
fun LatLng.distanceTo(other: LatLng): Double {
    val r = 6371.0
    val dLat = Math.toRadians(other.latitude - latitude)
    val dLng = Math.toRadians(other.longitude - longitude)
    val a = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(latitude)) * cos(Math.toRadians(other.latitude)) *
        sin(dLng / 2).pow(2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}
