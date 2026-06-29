package com.you.plot.core.domain.usecase.plan

import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.entity.Waypoint
import com.you.plot.core.domain.usecase.FakePlanRepo
import com.you.plot.core.domain.usecase.FakeRouteRepo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PlanUseCasesTest {

    private fun sampleRoute(id: Long = 0L, distanceKm: Double = 100.0) = Route(
        id = id,
        name = "Coast Road",
        sportType = SportType.CYCLING,
        startPoint = LatLng(-1.0, 36.0),
        endPoint = LatLng(-4.0, 39.0),
        totalDistanceKm = distanceKm,
        waypoints = listOf(
            Waypoint(routeId = id, name = "Start", position = LatLng(-1.0, 36.0), orderIndex = 0),
            Waypoint(routeId = id, name = "Mid", position = LatLng(-2.5, 37.5), orderIndex = 1, isStopPlanned = true),
            Waypoint(routeId = id, name = "End", position = LatLng(-4.0, 39.0), orderIndex = 2),
        ),
    )

    private fun samplePlan(routeId: Long, days: Int = 2, distancePerDay: Double = 50.0) = ActivityPlan(
        routeId = routeId,
        name = "Coast Tour",
        startDateMillis = 0L,
        numberOfDays = days,
        avgSpeedKmh = 20.0,
        avgDistancePerDayKm = distancePerDay,
    )

    @Test
    fun `SavePlanUseCase rejects blank plan names`() = runTest {
        val routeRepo = FakeRouteRepo()
        val planRepo = FakePlanRepo()
        val routeId = routeRepo.saveRoute(sampleRoute())
        try {
            SavePlanUseCase(planRepo, routeRepo).invoke(
                samplePlan(routeId).copy(name = "  "),
            )
            fail("expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `SavePlanUseCase rejects plans with a missing route`() = runTest {
        val planRepo = FakePlanRepo()
        val routeRepo = FakeRouteRepo()
        try {
            SavePlanUseCase(planRepo, routeRepo).invoke(samplePlan(routeId = 99L))
            fail("expected IllegalArgumentException for missing route")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `SavePlanUseCase persists and returns the assigned id`() = runTest {
        val routeRepo = FakeRouteRepo()
        val routeId = routeRepo.saveRoute(sampleRoute())
        val planRepo = FakePlanRepo()
        val id = SavePlanUseCase(planRepo, routeRepo).invoke(samplePlan(routeId))
        assertTrue(id > 0)
        assertEquals("Coast Tour", planRepo.getPlanById(id)?.name)
    }

    @Test
    fun `GetAllPlansUseCase reflects saved plans`() = runTest {
        val routeRepo = FakeRouteRepo()
        val routeId = routeRepo.saveRoute(sampleRoute())
        val planRepo = FakePlanRepo()
        SavePlanUseCase(planRepo, routeRepo).invoke(samplePlan(routeId))
        val plans = GetAllPlansUseCase(planRepo).invoke().first()
        assertEquals(1, plans.size)
    }

    @Test
    fun `GetPlansByRouteUseCase filters plans by route id`() = runTest {
        val routeRepo = FakeRouteRepo()
        val routeAId = routeRepo.saveRoute(sampleRoute().copy(name = "A"))
        val routeBId = routeRepo.saveRoute(sampleRoute().copy(name = "B"))
        val planRepo = FakePlanRepo()
        SavePlanUseCase(planRepo, routeRepo).invoke(samplePlan(routeAId).copy(name = "PA"))
        SavePlanUseCase(planRepo, routeRepo).invoke(samplePlan(routeBId).copy(name = "PB"))
        val onlyA = GetPlansByRouteUseCase(planRepo).invoke(routeAId).first()
        assertEquals(listOf("PA"), onlyA.map { it.name })
    }

    @Test
    fun `GetPlanByIdUseCase returns null for unknown id`() = runTest {
        assertNull(GetPlanByIdUseCase(FakePlanRepo()).invoke(123L))
    }

    @Test
    fun `DeletePlanUseCase removes the plan`() = runTest {
        val routeRepo = FakeRouteRepo()
        val routeId = routeRepo.saveRoute(sampleRoute())
        val planRepo = FakePlanRepo()
        val id = SavePlanUseCase(planRepo, routeRepo).invoke(samplePlan(routeId))
        DeletePlanUseCase(planRepo).invoke(id)
        assertNull(planRepo.getPlanById(id))
    }

    @Test
    fun `GeneratePlanEventsUseCase returns empty list when route is missing`() = runTest {
        val routeRepo = FakeRouteRepo()
        val plan = samplePlan(routeId = 999L)
        val events = GeneratePlanEventsUseCase(routeRepo).invoke(plan)
        assertTrue(events.isEmpty())
    }

    @Test
    fun `GeneratePlanEventsUseCase produces a Begin and End event per day`() = runTest {
        val routeRepo = FakeRouteRepo()
        val routeId = routeRepo.saveRoute(sampleRoute(distanceKm = 100.0))
        val plan = samplePlan(routeId, days = 2, distancePerDay = 50.0)
        val events = GeneratePlanEventsUseCase(routeRepo).invoke(plan)
        // Each day has a Begin and End event; waypoints may add more
        val beginEvents = events.filter { it.name == "Begin activity" }
        val endEvents = events.filter { it.name.startsWith("End of activity") }
        assertEquals(2, beginEvents.size)
        assertEquals(2, endEvents.size)
    }

    @Test
    fun `GeneratePlanEventsUseCase assigns unique synthetic ids`() = runTest {
        val routeRepo = FakeRouteRepo()
        val routeId = routeRepo.saveRoute(sampleRoute(distanceKm = 60.0))
        val plan = samplePlan(routeId, days = 3, distancePerDay = 20.0)
        val events = GeneratePlanEventsUseCase(routeRepo).invoke(plan)
        val ids = events.map { it.id }
        // Regression: previously every generated event had id=0L which crashed LazyColumn.
        assertEquals("ids must be unique", ids.size, ids.toSet().size)
        // Synthetic ids are negative so they can't collide with DB-assigned positive ids.
        assertTrue("synthetic ids should be negative", ids.all { it < 0 })
    }

    @Test
    fun `GeneratePlanEventsUseCase keeps cumulative distance non-decreasing within a day`() = runTest {
        val routeRepo = FakeRouteRepo()
        val routeId = routeRepo.saveRoute(sampleRoute(distanceKm = 100.0))
        val plan = samplePlan(routeId, days = 2, distancePerDay = 50.0)
        val events = GeneratePlanEventsUseCase(routeRepo).invoke(plan)
        events.groupBy { it.dayNumber }.forEach { (_, dayEvents) ->
            val sorted = dayEvents.sortedBy { it.orderIndex }
            sorted.zipWithNext().forEach { (a, b) ->
                assertFalse(
                    "distance should not decrease day=${a.dayNumber}",
                    b.distanceCoveredKm < a.distanceCoveredKm,
                )
            }
        }
    }

    @Test
    fun `GeneratePlanEventsUseCase tolerates zero-day or zero-speed plans without crashing`() = runTest {
        val routeRepo = FakeRouteRepo()
        val routeId = routeRepo.saveRoute(sampleRoute(distanceKm = 50.0))
        val plan = samplePlan(routeId).copy(numberOfDays = 0, avgSpeedKmh = 0.0)
        val events = GeneratePlanEventsUseCase(routeRepo).invoke(plan)
        // numberOfDays coerced to >= 1 inside the use case
        assertNotNull(events)
        assertTrue(events.isNotEmpty())
    }
}
