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

package com.you.plot.feature.route.plotter.utils

import android.location.LocationManager
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.PlotterStage
import com.you.plot.core.common.utils.MapConstants
import com.you.plot.core.common.utils.bearingLabel
import com.you.plot.core.common.utils.destinationPoint
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
