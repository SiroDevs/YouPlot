package com.you.plot.core.common.entity

data class ElevationPoint(val distanceKm: Double, val elevation: Double)

data class LatLng(val latitude: Double, val longitude: Double)

data class RouteCandidate(
    val id: Int,
    val waypoints: List<LatLng>,
    val elevationProfile: List<ElevationPoint>,
    val totalDist: Double,
    val elevationGain: Double,
    val elevationLoss: Double,
    val colorArgb: Long,
)
