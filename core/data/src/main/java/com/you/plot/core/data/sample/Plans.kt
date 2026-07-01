package com.you.plot.core.data.sample

import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.Route

val sampleRoutes = listOf(
    Route(
        id = 1L,
        name = "Morning Loop",
        sportType = SportType.RUNNING,
        startPoint = LatLng(-1.286, 36.817),
        endPoint = LatLng(-1.300, 36.830),
        totalDist = 8.4,
    ),
    Route(
        id = 2L,
        name = "Karura Forest Trail",
        sportType = SportType.HIKING,
        startPoint = LatLng(-1.243, 36.835),
        endPoint = LatLng(-1.250, 36.850),
        totalDist = 12.7,
    ),
    Route(
        id = 3L,
        name = "Lakeside Ride",
        sportType = SportType.CYCLING,
        startPoint = LatLng(-1.310, 36.700),
        endPoint = LatLng(-1.320, 36.720),
        totalDist = 24.0,
    ),
)

val samplePlans = listOf(
    ActivityPlan(
        id = 10L,
        routeId = 1L,
        name = "Weekend Run",
        description = "Easy morning loop",
        startDate = System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
        numberOfDays = 1,
        avgSpeed = 10.0,
        avgDailyDist = 8.4,
    ),
    ActivityPlan(
        id = 11L,
        routeId = 2L,
        name = "Forest Hike",
        description = "Half-day trail outing",
        startDate = System.currentTimeMillis() + 3L * 24 * 60 * 60 * 1000L,
        numberOfDays = 1,
        avgSpeed = 4.5,
        avgDailyDist = 12.7,
    ),
)
