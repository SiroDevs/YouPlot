package com.you.plot.feature.route.plotter.utils

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import com.you.plot.core.common.entity.ElevationPoint
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.PlotterStage
import com.you.plot.core.common.entity.RouteCandidate
import com.you.plot.core.common.utils.MapConstants
import com.you.plot.core.common.utils.bearingLabel
import com.you.plot.core.common.utils.destinationPoint
import com.you.plot.core.domain.entity.Waypoint
import com.you.plot.core.domain.entity.WaypointSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

object PlotterUtils {
    fun buildDistanceSuggestions(start: LatLng, targetDist: Double): List<WaypointSearchResult> {
        return listOf(0.0, 45.0, 90.0, 135.0).map { bearing ->
            WaypointSearchResult(
                displayName = bearingLabel(bearing) + " (≈ ${targetDist.toInt()} km)",
                latLng = destinationPoint(start, bearing, targetDist),
            )
        }
    }

    fun generateFallbackAreaLabel(latLng: LatLng): String {
        val ns = if (latLng.latitude >= 0) "Northern" else "Southern"
        val ew = if (latLng.longitude >= 0) "Eastern" else "Western"
        return "$ns $ew Area"
    }

    fun getEnabledLocationProviders(lm: LocationManager): List<String> {
        return listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
    }

    fun createLocationListener(
        lm: LocationManager,
        cont: kotlinx.coroutines.CancellableContinuation<android.location.Location?>
    ): android.location.LocationListener {
        return object : android.location.LocationListener {
            override fun onLocationChanged(loc: android.location.Location) {
                lm.removeUpdates(this)
                if (cont.isActive) cont.resume(loc)
            }

            @Deprecated("Deprecated in API 29")
            override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) {
            }
        }
    }
}

fun PlotterStage.previousStage(): PlotterStage? {
    return when (this) {
        PlotterStage.STAGE_1 -> null
        PlotterStage.STAGE_2 -> PlotterStage.STAGE_1
        PlotterStage.STAGE_3 -> PlotterStage.STAGE_2
        PlotterStage.STAGE_4 -> PlotterStage.STAGE_3
        PlotterStage.STAGE_5 -> PlotterStage.STAGE_4
    }
}

suspend fun buildRouteWaypoints(
    start: LatLng,
    end: LatLng,
    intermediates: List<LatLng>,
    isRoundTrip: Boolean,
    totalDist: Double,
    startName: String,
    endName: String,
    countryCode: String,
    resolveName: suspend (LatLng) -> String?,
): List<Waypoint> {
    val allPoints = buildList {
        add(start); addAll(intermediates); add(end)
        if (isRoundTrip) add(start)
    }
    return allPoints.mapIndexed { index, latLng ->
        val humanName = when (index) {
            0 -> startName.ifBlank { "Start" }
            allPoints.lastIndex -> {
                if (isRoundTrip) startName.ifBlank { "Start" }
                else endName.ifBlank { "Finish" }
            }
            else -> resolveName(latLng) ?: "Waypoint $index"
        }
        val cumDist = if (allPoints.size > 1)
            totalDist * index.toDouble() / (allPoints.size - 1)
        else 0.0
        Waypoint(
            routeId = 0L,
            name = humanName,
            position = latLng,
            orderIndex = index,
            distFromStart = cumDist,
            isStopPlanned = index != 0 && index != allPoints.lastIndex,
            countryCode = countryCode,
        )
    }
}

fun deriveAutoRouteName(startName: String, endName: String): String = when {
    startName.isNotBlank() && endName.isNotBlank() -> "$startName → $endName"
    startName.isNotBlank() -> "$startName Route"
    else -> "New Route"
}

fun Context.hasLocationPermission(): Boolean {
    val fine = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val coarse = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

/**
 * Distance, gain, loss, elevation profile, and polyline adjusted for the chosen
 * trip mode. For a real out-and-back the return leg re-covers every metre of the
 * outbound leg, so distance doubles and the round-trip gain equals one-way gain
 * plus one-way loss (which also equals loss, since start and end sit at the same
 * altitude). Callers use this for both stage-5 display and for the values saved
 * on the Route entity so the two never disagree.
 */
data class RouteStats(
    val totalDist: Double,
    val elevationGain: Double,
    val elevationLoss: Double,
    val elevationPoints: List<ElevationPoint>,
    val polyline: List<LatLng>,
)

fun RouteCandidate.statsFor(isRoundTrip: Boolean): RouteStats {
    if (!isRoundTrip) return RouteStats(
        totalDist = totalDist,
        elevationGain = elevationGain,
        elevationLoss = elevationLoss,
        elevationPoints = elevationPoints,
        polyline = waypoints,
    )
    val roundGainLoss = elevationGain + elevationLoss
    val forwardEnd = elevationPoints.lastOrNull()?.distanceKm ?: 0.0
    val mirroredProfile = elevationPoints.asReversed().drop(1).map { pt ->
        pt.copy(distanceKm = 2 * forwardEnd - pt.distanceKm)
    }
    return RouteStats(
        totalDist = totalDist * 2,
        elevationGain = roundGainLoss,
        elevationLoss = roundGainLoss,
        elevationPoints = elevationPoints + mirroredProfile,
        polyline = waypoints + waypoints.asReversed().drop(1),
    )
}
