package com.you.plot.core.common.entity

data class ElevationPoint(val dist: Double, val elevation: Double)

data class LatLng(val latitude: Double, val longitude: Double)

data class RouteCandidate(
    val id: Int,
    val waypoints: List<LatLng>,
    val elevationPoints: List<ElevationPoint>,
    val totalDist: Double,
    val elevationGain: Double,
    val elevationLoss: Double,
    val colorArgb: Long,
)
