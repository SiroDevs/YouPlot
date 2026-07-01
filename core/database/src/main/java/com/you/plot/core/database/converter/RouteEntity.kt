package com.you.plot.core.database.converter

import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.database.model.RouteEntity
import com.you.plot.core.domain.entity.Route

fun Route.toEntity() = RouteEntity(
    id = id, name = name, description = description, sportType = sportType,
    startLat = startPoint.latitude, startLng = startPoint.longitude,
    endLat = endPoint.latitude, endLng = endPoint.longitude,
    waypoints = waypoints.toJson(), elevationProfile = elevationProfile.toJson(),
    totalDist = totalDist, elevationGain = elevationGain,
    elevationLoss = elevationLoss, isRoundTrip = isRoundTrip,
    createdAt = createdAt,
    polyline = polyline.polylineJson(),
    isFavorite = isFavorite, deletedAt = deletedAt,
)

fun RouteEntity.toDomain() = Route(
    id = id, name = name, description = description, sportType = sportType,
    startPoint = LatLng(startLat, startLng), endPoint = LatLng(endLat, endLng),
    waypoints = waypoints.toWaypointList(), elevationProfile = elevationProfile.toElevationList(),
    totalDist = totalDist, elevationGain = elevationGain,
    elevationLoss = elevationLoss, isRoundTrip = isRoundTrip,
    createdAt = createdAt,
    polyline = polyline.toLatLngList(),
    isFavorite = isFavorite, deletedAt = deletedAt,
)
