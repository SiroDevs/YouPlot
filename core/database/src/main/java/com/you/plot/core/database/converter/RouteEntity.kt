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
)

fun RouteEntity.toDomain() = Route(
    id = id, name = name, description = description, sportType = sportType,
    startPoint = LatLng(startLat, startLng), endPoint = LatLng(endLat, endLng),
    waypoints = waypoints.toWaypointList(), elevationProfile = elevationProfile.toElevationList(),
    totalDist = totalDist, elevationGain = elevationGain,
    elevationLoss = elevationLoss, isRoundTrip = isRoundTrip,
    createdAt = createdAt,
)
