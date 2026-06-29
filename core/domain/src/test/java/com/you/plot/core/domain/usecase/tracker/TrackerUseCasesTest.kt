package com.you.plot.core.domain.usecase.tracker

import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.SessionStatus
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.ActivitySession
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.entity.Waypoint
import com.you.plot.core.domain.usecase.FakePlanRepo
import com.you.plot.core.domain.usecase.FakeRouteRepo
import com.you.plot.core.domain.usecase.FakeSessionRepo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class TrackerUseCasesTest {

    private fun seed(): Triple<FakeRouteRepo, FakePlanRepo, FakeSessionRepo> {
        val routeRepo = FakeRouteRepo()
        val planRepo = FakePlanRepo()
        val sessionRepo = FakeSessionRepo()
        return Triple(routeRepo, planRepo, sessionRepo)
    }

    private fun route(id: Long = 0L) = Route(
        id = id,
        name = "R",
        sportType = SportType.RUNNING,
        startPoint = LatLng(0.0, 0.0),
        endPoint = LatLng(0.5, 0.5),
        totalDistanceKm = 80.0,
        waypoints = listOf(
            Waypoint(routeId = id, name = "W1", position = LatLng(0.1, 0.1), orderIndex = 1),
            Waypoint(routeId = id, name = "W2", position = LatLng(0.3, 0.3), orderIndex = 2),
        ),
    )

    private fun plan(routeId: Long) = ActivityPlan(
        routeId = routeId,
        name = "P",
        startDateMillis = 0L,
        numberOfDays = 1,
        avgSpeedKmh = 20.0,
        avgDistancePerDayKm = 80.0,
    )

    @Test
    fun `StartSessionUseCase fails when plan does not exist`() = runTest {
        val (routeRepo, planRepo, sessionRepo) = seed()
        try {
            StartSessionUseCase(sessionRepo, planRepo, routeRepo).invoke(999L)
            fail("expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `StartSessionUseCase fails when route does not exist`() = runTest {
        val (routeRepo, planRepo, sessionRepo) = seed()
        val planId = planRepo.savePlan(plan(routeId = 42L))
        try {
            StartSessionUseCase(sessionRepo, planRepo, routeRepo).invoke(planId)
            fail("expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `StartSessionUseCase persists an in-progress session with waypoint progress`() = runTest {
        val (routeRepo, planRepo, sessionRepo) = seed()
        val routeId = routeRepo.saveRoute(route())
        val planId = planRepo.savePlan(plan(routeId))
        val sessionId = StartSessionUseCase(sessionRepo, planRepo, routeRepo).invoke(planId)
        val saved = sessionRepo.getSessionById(sessionId)
        assertNotNull(saved)
        assertEquals(SessionStatus.IN_PROGRESS, saved!!.status)
        assertEquals(2, saved.waypointProgress.size)
        assertTrue("estimated completion must be after start", saved.estimatedCompletionMillis!! > saved.startedAtMillis!!)
    }

    @Test
    fun `PauseSessionUseCase transitions a live session to paused`() = runTest {
        val (routeRepo, planRepo, sessionRepo) = seed()
        val routeId = routeRepo.saveRoute(route())
        val planId = planRepo.savePlan(plan(routeId))
        val sessionId = StartSessionUseCase(sessionRepo, planRepo, routeRepo).invoke(planId)
        PauseSessionUseCase(sessionRepo).invoke(sessionId)
        assertEquals(SessionStatus.PAUSED, sessionRepo.getSessionById(sessionId)?.status)
    }

    @Test
    fun `ResumeSessionUseCase returns a paused session to in-progress`() = runTest {
        val (routeRepo, planRepo, sessionRepo) = seed()
        val routeId = routeRepo.saveRoute(route())
        val planId = planRepo.savePlan(plan(routeId))
        val sessionId = StartSessionUseCase(sessionRepo, planRepo, routeRepo).invoke(planId)
        PauseSessionUseCase(sessionRepo).invoke(sessionId)
        ResumeSessionUseCase(sessionRepo).invoke(sessionId)
        assertEquals(SessionStatus.IN_PROGRESS, sessionRepo.getSessionById(sessionId)?.status)
    }

    @Test
    fun `CompleteSessionUseCase marks the session as completed`() = runTest {
        val (routeRepo, planRepo, sessionRepo) = seed()
        val routeId = routeRepo.saveRoute(route())
        val planId = planRepo.savePlan(plan(routeId))
        val sessionId = StartSessionUseCase(sessionRepo, planRepo, routeRepo).invoke(planId)
        CompleteSessionUseCase(sessionRepo).invoke(sessionId)
        assertEquals(SessionStatus.COMPLETED, sessionRepo.getSessionById(sessionId)?.status)
    }

    @Test
    fun `GetActiveSessionUseCase returns only the in-progress session`() = runTest {
        val (routeRepo, planRepo, sessionRepo) = seed()
        val routeId = routeRepo.saveRoute(route())
        val planId = planRepo.savePlan(plan(routeId))
        val sessionId = StartSessionUseCase(sessionRepo, planRepo, routeRepo).invoke(planId)
        assertEquals(sessionId, GetActiveSessionUseCase(sessionRepo).invoke()?.id)
        CompleteSessionUseCase(sessionRepo).invoke(sessionId)
        assertNull(GetActiveSessionUseCase(sessionRepo).invoke())
    }

    @Test
    fun `UpdateSessionLocationUseCase silently no-ops when the session is missing`() = runTest {
        val (_, _, sessionRepo) = seed()
        // Should not throw
        UpdateSessionLocationUseCase(sessionRepo).invoke(
            sessionId = 999L,
            newLocation = LatLng(0.0, 0.0),
            speedKmh = 10.0,
            elapsedSeconds = 1L,
        )
    }

    @Test
    fun `UpdateSessionLocationUseCase advances distance covered and current location`() = runTest {
        val (routeRepo, planRepo, sessionRepo) = seed()
        val routeId = routeRepo.saveRoute(route())
        val planId = planRepo.savePlan(plan(routeId))
        val sessionId = StartSessionUseCase(sessionRepo, planRepo, routeRepo).invoke(planId)
        // Place an initial location so the next update has a delta to add
        sessionRepo.updateSession(
            sessionRepo.getSessionById(sessionId)!!.copy(currentLocation = LatLng(0.0, 0.0)),
        )
        UpdateSessionLocationUseCase(sessionRepo).invoke(
            sessionId = sessionId,
            newLocation = LatLng(0.05, 0.05),
            speedKmh = 8.0,
            elapsedSeconds = 60L,
        )
        val updated = sessionRepo.getSessionById(sessionId)!!
        assertTrue("distance should accumulate", updated.distanceCoveredKm > 0)
        assertEquals(LatLng(0.05, 0.05), updated.currentLocation)
        assertEquals(8.0, updated.currentSpeedKmh, 1e-9)
        assertEquals(60L, updated.elapsedTimeSeconds)
    }

    @Test
    fun `LatLng distanceTo returns zero for the same point`() {
        val p = LatLng(-1.0, 36.0)
        assertEquals(0.0, p.distanceTo(p), 1e-9)
    }

    @Test
    fun `recalculate clamps remaining distances to non-negative values`() {
        val session = ActivitySession(
            planId = 1L,
            routeId = 1L,
            waypointProgress = listOf(
                com.you.plot.core.domain.entity.WaypointProgress(
                    waypoint = Waypoint(
                        routeId = 1L, name = "W", position = LatLng(0.0, 0.0), orderIndex = 1,
                    ),
                    plannedArrivalMillis = 0L,
                    estimatedArrivalMillis = 0L,
                    distanceRemainingKm = 100.0,
                ),
            ),
        )
        val updated = session.recalculate(LatLng(0.0, 0.0), speedKmh = 10.0, elapsedSeconds = 1L)
        assertTrue(updated.waypointProgress.all { it.distanceRemainingKm >= 0.0 })
    }
}
