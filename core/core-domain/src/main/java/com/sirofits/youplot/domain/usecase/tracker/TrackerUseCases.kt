package com.sirofits.youplot.domain.usecase.tracker

import com.sirofits.youplot.domain.entity.ActivitySession
import com.sirofits.youplot.domain.entity.LatLng
import com.sirofits.youplot.domain.entity.SessionStatus
import com.sirofits.youplot.domain.entity.WaypointProgress
import com.sirofits.youplot.domain.repository.PlanRepository
import com.sirofits.youplot.domain.repository.RouteRepository
import com.sirofits.youplot.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.math.*

class StartSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val planRepository: PlanRepository,
    private val routeRepository: RouteRepository,
) {
    suspend operator fun invoke(planId: Long): Long {
        val plan = requireNotNull(planRepository.getPlanById(planId)) { "Plan not found" }
        val route = requireNotNull(routeRepository.getRouteById(plan.routeId)) { "Route not found" }

        val waypointProgress = route.waypoints.mapIndexed { _, wp ->
            val event = plan.events.firstOrNull { it.waypointId == wp.id }
            WaypointProgress(
                waypoint = wp,
                plannedArrivalMillis = event?.plannedTimeMillis ?: System.currentTimeMillis(),
                estimatedArrivalMillis = event?.plannedTimeMillis ?: System.currentTimeMillis(),
                distanceRemainingKm = route.totalDistanceKm,
            )
        }

        val session = ActivitySession(
            planId = planId,
            routeId = plan.routeId,
            status = SessionStatus.IN_PROGRESS,
            startedAtMillis = System.currentTimeMillis(),
            waypointProgress = waypointProgress,
        )
        return sessionRepository.saveSession(session)
    }
}

class UpdateSessionLocationUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(
        sessionId: Long,
        newLocation: LatLng,
        speedKmh: Double,
        elapsedSeconds: Long,
    ) {
        val session = sessionRepository.getSessionById(sessionId) ?: return
        val updated = session.recalculate(newLocation, speedKmh, elapsedSeconds)
        sessionRepository.updateSession(updated)
    }
}

class PauseSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Long) {
        val session = sessionRepository.getSessionById(sessionId) ?: return
        sessionRepository.updateSession(session.copy(status = SessionStatus.PAUSED))
    }
}

class ResumeSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Long) {
        val session = sessionRepository.getSessionById(sessionId) ?: return
        sessionRepository.updateSession(session.copy(status = SessionStatus.IN_PROGRESS))
    }
}

class CompleteSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Long) {
        val session = sessionRepository.getSessionById(sessionId) ?: return
        sessionRepository.updateSession(session.copy(status = SessionStatus.COMPLETED))
    }
}

class GetActiveSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(): ActivitySession? = sessionRepository.getActiveSession()
}

class GetSessionsByPlanUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(planId: Long): Flow<List<ActivitySession>> =
        sessionRepository.getSessionsByPlanId(planId)
}

// ─── Extension: recalculate ETAs and distances ───────────────────────────────

private fun ActivitySession.recalculate(
    newLocation: LatLng,
    speedKmh: Double,
    elapsedSeconds: Long,
): ActivitySession {
    val distanceDelta = currentLocation?.distanceTo(newLocation) ?: 0.0
    val newDistance = distanceCoveredKm + distanceDelta

    val updatedProgress = waypointProgress.map { wp ->
        if (wp.isReached) return@map wp
        val remaining = (wp.waypoint.orderIndex.toDouble() / waypointProgress.size) *
                (waypointProgress.last().distanceRemainingKm) - newDistance
        val etaMs = if (speedKmh > 0)
            System.currentTimeMillis() + ((remaining / speedKmh) * 3_600_000).toLong()
        else wp.estimatedArrivalMillis

        wp.copy(
            distanceRemainingKm = remaining.coerceAtLeast(0.0),
            estimatedArrivalMillis = etaMs,
            isReached = remaining <= 0.05, // within 50m
        )
    }

    return copy(
        currentLocation = newLocation,
        currentSpeedKmh = speedKmh,
        distanceCoveredKm = newDistance,
        elapsedTimeSeconds = elapsedSeconds,
        waypointProgress = updatedProgress,
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
