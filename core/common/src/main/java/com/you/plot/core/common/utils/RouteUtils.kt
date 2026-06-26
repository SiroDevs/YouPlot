/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.you.plot.core.common.utils

import com.you.plot.core.common.entity.ElevationPoint
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.RouteCandidate
import kotlin.collections.plus
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


fun haversineKm(a: LatLng, b: LatLng): Double {
    val r = 6371.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLng = Math.toRadians(b.longitude - a.longitude)
    val h = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLng / 2).pow(2)
    return r * 2 * atan2(sqrt(h), sqrt(1 - h))
}

fun destinationPoint(start: LatLng, bearingDeg: Double, distanceKm: Double): LatLng {
    val r = 6371.0
    val d = distanceKm / r
    val brng = Math.toRadians(bearingDeg)
    val lat1 = Math.toRadians(start.latitude)
    val lon1 = Math.toRadians(start.longitude)
    val lat2 = asin(sin(lat1) * cos(d) + cos(lat1) * sin(d) * cos(brng))
    val lon2 = lon1 + atan2(sin(brng) * sin(d) * cos(lat1), cos(d) - sin(lat1) * sin(lat2))
    return LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2))
}

fun bearingLabel(b: Double) = when {
    b < 22.5 -> "North"
    b < 67.5 -> "North-East"
    b < 112.5 -> "East"
    b < 157.5 -> "South-East"
    b < 202.5 -> "South"
    b < 247.5 -> "South-West"
    b < 292.5 -> "West"
    b < 337.5 -> "North-West"
    else -> "North"
}

fun Double.fmt() = "%.5f".format(this)

fun decodePolyline6(encoded: String): List<LatLng> {
    val result = mutableListOf<LatLng>()
    var index = 0;
    var lat = 0;
    var lng = 0
    while (index < encoded.length) {
        var shift = 0;
        var b: Int;
        var result5 = 0
        do {
            b = encoded[index++].code - 63
            result5 = result5 or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        lat += if (result5 and 1 != 0) (result5 shr 1).inv() else result5 shr 1
        shift = 0; result5 = 0
        do {
            b = encoded[index++].code - 63
            result5 = result5 or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        lng += if (result5 and 1 != 0) (result5 shr 1).inv() else result5 shr 1
        result.add(LatLng(lat / 1e6, lng / 1e6))
    }
    return result
}

fun buildElevationStats(elevProfile: List<ElevationPoint>): Pair<Double, Double> {
    var gain = 0.0;
    var loss = 0.0
    elevProfile.zipWithNext().forEach { (a, b) ->
        val diff = b.elevationMeters - a.elevationMeters
        if (diff > 0) gain += diff else loss += abs(diff)
    }
    return gain to loss
}

fun buildFallbackCandidates(start: LatLng, end: LatLng, via: List<LatLng>): List<RouteCandidate> {
    val base = listOf(start) + via + listOf(end)
    return listOf(
        buildFallbackCandidate(0, base),
        buildFallbackCandidate(
            1,
            listOf(start) +
                listOf(
                    LatLng(
                        (start.latitude + end.latitude) / 2 + 0.008,
                        (start.longitude + end.longitude) / 2
                    )
                ) +
                via +
                listOf(end),
        ),
    )
}

fun buildFallbackCandidate(id: Int, points: List<LatLng>): RouteCandidate {
    val segs = points.zipWithNext().map { (a, b) -> haversineKm(a, b) }
    val dist = segs.sum()
    val elev = points.mapIndexed { i, _ ->
        ElevationPoint(segs.take(i).sum(), 1400.0 + 80 * sin(i.toDouble() / points.size * PI * 2))
    }
    val (gain, loss) = buildElevationStats(elev)
    return RouteCandidate(
        id,
        points,
        elev,
        dist,
        gain,
        loss,
        MapConstants.CANDIDATE_COLORS[id % MapConstants.CANDIDATE_COLORS.size]
    )
}

fun buildWaypointSuggestions(start: LatLng, end: LatLng, count: Int = 3): List<LatLng> =
    (1..count).map { i ->
        val t = i.toDouble() / (count + 1).toDouble()
        LatLng(
            start.latitude + t * (end.latitude - start.latitude),
            start.longitude + t * (end.longitude - start.longitude),
        )
    }

fun generateDistanceSuggestions(
    start: LatLng,
    targetKm: Double,
): List<Pair<String, LatLng>> =
    listOf(0.0, 45.0, 90.0, 135.0).map { b ->
        bearingLabel(b) + " (≈ ${targetKm.toInt()} km)" to destinationPoint(start, b, targetKm)
    }

data class LatLngBounds(
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double,
) {
    val centerLat get() = (minLat + maxLat) / 2.0
    val centerLng get() = (minLng + maxLng) / 2.0
    val latSpan get() = maxLat - minLat
    val lngSpan get() = maxLng - minLng
}

fun boundingBox(vararg points: LatLng?): LatLngBounds? {
    val valid = points.filterNotNull()
    if (valid.isEmpty()) return null
    return LatLngBounds(
        minLat = valid.minOf { it.latitude },
        maxLat = valid.maxOf { it.latitude },
        minLng = valid.minOf { it.longitude },
        maxLng = valid.maxOf { it.longitude },
    )
}

fun boundingBoxOfAll(vararg groups: List<LatLng>): LatLngBounds? =
    boundingBox(*groups.flatMap { it }.toTypedArray())
