package com.you.plot.core.domain.usecase.route

import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.usecase.FakeRouteRepo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RouteUseCasesTest {

    private fun sampleRoute(name: String = "Test", distanceKm: Double = 10.0) = Route(
        name = name,
        sportType = SportType.RUNNING,
        startPoint = LatLng(-1.0, 36.0),
        endPoint = LatLng(-1.1, 36.1),
        totalDistanceKm = distanceKm,
    )

    @Test
    fun `SaveRouteUseCase persists a valid route and returns the assigned id`() = runTest {
        val repo = FakeRouteRepo()
        val id = SaveRouteUseCase(repo).invoke(sampleRoute())
        assertTrue(id > 0)
        val stored = repo.getRouteById(id)
        assertNotNull(stored)
        assertEquals("Test", stored!!.name)
    }

    @Test
    fun `SaveRouteUseCase rejects blank names`() = runTest {
        val repo = FakeRouteRepo()
        try {
            SaveRouteUseCase(repo).invoke(sampleRoute(name = " "))
            fail("expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `SaveRouteUseCase rejects zero or negative distance`() = runTest {
        val repo = FakeRouteRepo()
        try {
            SaveRouteUseCase(repo).invoke(sampleRoute(distanceKm = 0.0))
            fail("expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `GetAllRoutesUseCase emits the current set of routes`() = runTest {
        val repo = FakeRouteRepo()
        SaveRouteUseCase(repo).invoke(sampleRoute(name = "A"))
        SaveRouteUseCase(repo).invoke(sampleRoute(name = "B"))
        val routes = GetAllRoutesUseCase(repo).invoke().first()
        assertEquals(setOf("A", "B"), routes.map { it.name }.toSet())
    }

    @Test
    fun `GetRouteByIdUseCase returns null when route is not found`() = runTest {
        val repo = FakeRouteRepo()
        assertNull(GetRouteByIdUseCase(repo).invoke(42L))
    }

    @Test
    fun `DeleteRouteUseCase removes the route from the repo`() = runTest {
        val repo = FakeRouteRepo()
        val id = SaveRouteUseCase(repo).invoke(sampleRoute())
        DeleteRouteUseCase(repo).invoke(id)
        assertNull(repo.getRouteById(id))
    }
}
