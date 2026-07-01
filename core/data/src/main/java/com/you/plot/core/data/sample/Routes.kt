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

package com.you.plot.core.data.sample

import com.you.plot.core.common.entity.ElevationPoint
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.entity.Waypoint

fun sampleRoute(): Route {
    val createdAt = 1_700_000_000_000L
    val profile = listOf(
        ElevationPoint(0.0, 1700.0),
        ElevationPoint(1.0, 1720.0),
        ElevationPoint(2.0, 1755.0),
        ElevationPoint(3.0, 1742.0),
        ElevationPoint(4.0, 1768.0),
        ElevationPoint(5.0, 1750.0),
        ElevationPoint(6.0, 1820.0),
        ElevationPoint(7.0, 1855.0),
        ElevationPoint(8.0, 1942.0),
        ElevationPoint(9.0, 1900.0),
        ElevationPoint(10.0, 1950.0),
    )
    val waypoints = listOf(
        Waypoint(
            id = 1, routeId = 1L, name = "Nairobi CBD",
            position = LatLng(-1.286, 36.817), orderIndex = 0,
            elevation = 1700.0, distFromStart = 0.0,
        ),
        Waypoint(
            id = 2, routeId = 1L, name = "Uhuru Park",
            position = LatLng(-1.291, 36.819), orderIndex = 1,
            elevation = 1720.0, distFromStart = 2.1,
            isStopPlanned = true,
        ),
        Waypoint(
            id = 3, routeId = 1L, name = "Lavington",
            position = LatLng(-1.300, 36.830), orderIndex = 2,
            elevation = 1750.0, distFromStart = 8.4,
        ),
    )
    return Route(
        id = 1L,
        name = "Morning Loop",
        description = "Easy paced run through the city.",
        sportType = SportType.RUNNING,
        startPoint = LatLng(-1.286, 36.817),
        endPoint = LatLng(-1.300, 36.830),
        waypoints = waypoints,
        elevationPoints = profile,
        totalDist = 8.4,
        elevationGain = 120.0,
        elevationLoss = 95.0,
        isRoundTrip = false,
        createdAt = createdAt,
    )
}
