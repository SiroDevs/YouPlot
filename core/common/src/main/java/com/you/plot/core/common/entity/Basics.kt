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

package com.you.plot.core.common.entity

data class ElevationPoint(val distanceKm: Double, val elevationMeters: Double)

data class LatLng(val latitude: Double, val longitude: Double)

data class RouteCandidate(
    val id: Int,
    val waypoints: List<LatLng>,
    val elevationProfile: List<ElevationPoint>,
    val totalDistanceKm: Double,
    val totalElevationGainMeters: Double,
    val totalElevationLossMeters: Double,
    val colorArgb: Long,
)
