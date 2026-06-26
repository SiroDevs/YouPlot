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

package com.you.plot.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.you.plot.core.common.entity.SportType

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String,
    val sportType: SportType,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val waypointsJson: String,
    val elevationProfileJson: String,
    val totalDistanceKm: Double,
    val totalElevationGainMeters: Double,
    val totalElevationLossMeters: Double,
    val isRoundTrip: Boolean,
    val createdAt: Long,
)
