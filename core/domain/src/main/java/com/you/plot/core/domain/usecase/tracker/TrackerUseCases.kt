package com.you.plot.core.domain.usecase.tracker

import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.SessionStatus
import com.you.plot.core.domain.entity.ActivitySession
import com.you.plot.core.domain.entity.WaypointProgress
import com.you.plot.core.domain.repos.PlanRepo
import com.you.plot.core.domain.repos.RouteRepo
import com.you.plot.core.domain.repos.SessionRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.math.*

class StartSessionUseCase @Inject constructor(
    private val sessionRepo: SessionRepo,
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
                plannedArrivalMillis = event?.plannedTimeMillis ?: System.currentTimeMillis(),
                estimatedArrivalMillis = event?.plannedTimeMillis ?: System.currentTimeMillis(),
                distanceRemainingKm = route.totalDistanceKm,
            )
        }

        val nowMs = System.currentTimeMillis()
        // Estimated completion = start + (total distance / avg speed) hours
        val estimatedCompletion =
            nowMs + ((route.totalDistanceKm / plan.avgSpeedKmh) * 3_600_000L).toLong()

        val session = ActivitySession(
            planId = planId,
            routeId = plan.routeId,
            status = SessionStatus.IN_PROGRESS,
            startedAtMillis = nowMs,
            waypointProgress = waypointProgress,
            estimatedCompletionMillis = estimatedCompletion,
        )
        return sessionRepo.saveSession(session)
    }
}

class UpdateSessionLocationUseCase @Inject constructor(
    private val sessionRepo: SessionRepo,
) {
    suspend operator fun invoke(
        sessionId: Long,
        newLocation: LatLng,
        speedKmh: Double,
        elapsedSeconds: Long,
    ) {
        val session = sessionRepo.getSessionById(sessionId) ?: return
        val updated = session.recalculate(newLocation, speedKmh, elapsedSeconds)
        sessionRepo.updateSession(updated)
    }
}

class PauseSessionUseCase @Inject constructor(private val sessionRepo: SessionRepo) {
    suspend operator fun invoke(sessionId: Long) {
        val session = sessionRepo.getSessionById(sessionId) ?: return
        sessionRepo.updateSession(session.copy(status = SessionStatus.PAUSED))
    }
}

class ResumeSessionUseCase @Inject constructor(private val sessionRepo: SessionRepo) {
    suspend operator fun invoke(sessionId: Long) {
        val session = sessionRepo.getSessionById(sessionId) ?: return
        sessionRepo.updateSession(session.copy(status = SessionStatus.IN_PROGRESS))
    }
}

class CompleteSessionUseCase @Inject constructor(private val sessionRepo: SessionRepo) {
    suspend operator fun invoke(sessionId: Long) {
        val session = sessionRepo.getSessionById(sessionId) ?: return
        sessionRepo.updateSession(session.copy(status = SessionStatus.COMPLETED))
    }
}

class GetActiveSessionUseCase @Inject constructor(private val sessionRepo: SessionRepo) {
    suspend operator fun invoke(): ActivitySession? = sessionRepo.getActiveSession()
}

class GetSessionsByPlanUseCase @Inject constructor(private val sessionRepo: SessionRepo) {
    operator fun invoke(planId: Long): Flow<List<ActivitySession>> =
        sessionRepo.getSessionsByPlanId(planId)
}

// ─── recalculate: ETAs, distances, estimated completion ──────────────────────

fun ActivitySession.recalculate(
    newLocation: LatLng,
    speedKmh: Double,
    elapsedSeconds: Long,
): ActivitySession {
    val distanceDelta = currentLocation?.distanceTo(newLocation) ?: 0.0
    val newDistance = distanceCoveredKm + distanceDelta
    val nowMs = System.currentTimeMillis()

    val updatedProgress = waypointProgress.map { wp ->
        if (wp.isReached) return@map wp
        val waypointFraction = wp.waypoint.orderIndex.toDouble() /
            waypointProgress.size.coerceAtLeast(1)
        val totalDist = waypointProgress.lastOrNull()?.distanceRemainingKm
            ?.plus(newDistance) ?: newDistance
        val remaining = (waypointFraction * totalDist - newDistance).coerceAtLeast(0.0)
        val etaMs = if (speedKmh > 0)
            nowMs + ((remaining / speedKmh) * 3_600_000L).toLong()
        else wp.estimatedArrivalMillis

        wp.copy(
            distanceRemainingKm = remaining,
            estimatedArrivalMillis = etaMs,
            isReached = remaining <= 0.05,
        )
    }

    // Recalculate estimated completion from current position and speed
    val lastWp = updatedProgress.lastOrNull()
    val distToFinish = lastWp?.distanceRemainingKm ?: 0.0
    val newCompletion = if (speedKmh > 0)
        nowMs + ((distToFinish / speedKmh) * 3_600_000L).toLong()
    else estimatedCompletionMillis

    return copy(
        currentLocation = newLocation,
        currentSpeedKmh = speedKmh,
        distanceCoveredKm = newDistance,
        elapsedTimeSeconds = elapsedSeconds,
        waypointProgress = updatedProgress,
        estimatedCompletionMillis = newCompletion,
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
