package com.you.plot.core.common.utils

import com.you.plot.core.common.entity.ElevationPoint
import com.you.plot.core.common.entity.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteUtilsTest {

    @Test
    fun `haversineKm returns zero for identical points`() {
        val p = LatLng(-1.286389, 36.817223)
        assertEquals(0.0, haversineKm(p, p), 1e-9)
    }

    @Test
    fun `haversineKm matches known city distance within tolerance`() {
        // Nairobi → Mombasa is roughly 440 km
        val nairobi = LatLng(-1.286389, 36.817223)
        val mombasa = LatLng(-4.043477, 39.668206)
        val d = haversineKm(nairobi, mombasa)
        assertTrue("expected ~440 km, got $d", d in 430.0..450.0)
    }

    @Test
    fun `destinationPoint moves north by approximately the requested distance`() {
        val start = LatLng(0.0, 0.0)
        val target = destinationPoint(start, bearingDeg = 0.0, distanceKm = 100.0)
        val measured = haversineKm(start, target)
        assertEquals(100.0, measured, 0.1)
        assertTrue("expected northward latitude", target.latitude > 0)
    }

    @Test
    fun `bearingLabel maps cardinal degrees to compass names`() {
        assertEquals("North", bearingLabel(0.0))
        assertEquals("North-East", bearingLabel(45.0))
        assertEquals("East", bearingLabel(90.0))
        assertEquals("South-East", bearingLabel(135.0))
        assertEquals("South", bearingLabel(180.0))
        assertEquals("South-West", bearingLabel(225.0))
        assertEquals("West", bearingLabel(270.0))
        assertEquals("North-West", bearingLabel(315.0))
        assertEquals("North", bearingLabel(360.0))
    }

    @Test
    fun `decodePolyline6 returns empty list for empty input`() {
        assertEquals(emptyList<LatLng>(), decodePolyline6(""))
    }

    @Test
    fun `decodePolyline6 produces same number of points as the encoding suggests`() {
        // "??_p~iF~ps|U" is a precision-5 polyline of two zero-deltas;
        // at precision 6 it decodes to two arbitrary points — we only care
        // that the decoder is total and returns plausible coordinates.
        val points = decodePolyline6("??")
        assertEquals(1, points.size)
        assertTrue(points[0].latitude in -90.0..90.0)
        assertTrue(points[0].longitude in -180.0..180.0)
    }

    @Test
    fun `buildElevationStats sums gains and losses separately`() {
        val profile = listOf(
            ElevationPoint(0.0, 1000.0),
            ElevationPoint(1.0, 1100.0),  // +100
            ElevationPoint(2.0, 1050.0),  // -50
            ElevationPoint(3.0, 1200.0),  // +150
        )
        val (gain, loss) = buildElevationStats(profile)
        assertEquals(250.0, gain, 1e-6)
        assertEquals(50.0, loss, 1e-6)
    }

    @Test
    fun `buildElevationStats returns zero for empty or single-point profile`() {
        assertEquals(0.0 to 0.0, buildElevationStats(emptyList()))
        assertEquals(0.0 to 0.0, buildElevationStats(listOf(ElevationPoint(0.0, 100.0))))
    }

    @Test
    fun `buildWaypointSuggestions interpolates count midpoints between start and end`() {
        val start = LatLng(0.0, 0.0)
        val end = LatLng(10.0, 20.0)
        val pts = buildWaypointSuggestions(start, end, count = 3)
        assertEquals(3, pts.size)
        // Each successive point should be strictly between start and end and ordered
        pts.zipWithNext().forEach { (a, b) -> assertTrue(a.latitude < b.latitude) }
        assertTrue(pts.first().latitude > start.latitude)
        assertTrue(pts.last().latitude < end.latitude)
    }

    @Test
    fun `buildFallbackCandidates returns two distinct candidates with unique ids`() {
        val start = LatLng(-1.0, 36.0)
        val end = LatLng(-1.1, 36.1)
        val candidates = buildFallbackCandidates(start, end, via = emptyList())
        assertEquals(2, candidates.size)
        assertEquals(0, candidates[0].id)
        assertEquals(1, candidates[1].id)
        assertTrue(candidates[0].totalDistanceKm > 0)
        // The detoured candidate should be at least as long as the direct one
        assertTrue(candidates[1].totalDistanceKm >= candidates[0].totalDistanceKm)
    }

    @Test
    fun `boundingBox returns null when all points are null`() {
        assertNull(boundingBox(null, null))
    }

    @Test
    fun `boundingBox computes correct min and max from mixed nulls`() {
        val a = LatLng(-1.0, 36.0)
        val b = LatLng(-2.0, 37.0)
        val bbox = boundingBox(a, null, b)
        assertNotNull(bbox)
        assertEquals(-2.0, bbox!!.minLat, 1e-9)
        assertEquals(-1.0, bbox.maxLat, 1e-9)
        assertEquals(36.0, bbox.minLng, 1e-9)
        assertEquals(37.0, bbox.maxLng, 1e-9)
    }

    @Test
    fun `boundingBoxOfAll merges multiple point lists`() {
        val a = listOf(LatLng(-1.0, 36.0), LatLng(-1.5, 36.5))
        val b = listOf(LatLng(-2.0, 37.0))
        val bbox = boundingBoxOfAll(a, b)
        assertNotNull(bbox)
        assertEquals(-2.0, bbox!!.minLat, 1e-9)
        assertEquals(-1.0, bbox.maxLat, 1e-9)
        assertEquals(36.0, bbox.minLng, 1e-9)
        assertEquals(37.0, bbox.maxLng, 1e-9)
    }

    @Test
    fun `generateDistanceSuggestions produces four directions all at the requested distance`() {
        val start = LatLng(0.0, 0.0)
        val targetKm = 25.0
        val suggestions = generateDistanceSuggestions(start, targetKm)
        assertEquals(4, suggestions.size)
        suggestions.forEach { (_, latLng) ->
            assertEquals(targetKm, haversineKm(start, latLng), 0.5)
        }
    }
}
