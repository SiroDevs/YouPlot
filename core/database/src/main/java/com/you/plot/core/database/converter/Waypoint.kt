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
import com.you.plot.core.domain.entity.Waypoint
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.forEach

@JvmName("waypointListToJson")
fun List<Waypoint>.toJson(): String {
    val arr = JSONArray()
    forEach { wp ->
        arr.put(JSONObject().apply {
            put("id", wp.id); put("routeId", wp.routeId); put("name", wp.name)
            put("lat", wp.position.latitude); put("lng", wp.position.longitude)
            put("orderIndex", wp.orderIndex); put("elevation", wp.elevationMeters)
            put("isStopPlanned", wp.isStopPlanned)
        })
    }
    return arr.toString()
}

fun String.toWaypointList(): List<Waypoint> {
    val arr = JSONArray(this)
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        Waypoint(
            id = obj.getLong("id"), routeId = obj.getLong("routeId"),
            name = obj.getString("name"),
            position = LatLng(obj.getDouble("lat"), obj.getDouble("lng")),
            orderIndex = obj.getInt("orderIndex"),
            elevationMeters = obj.getDouble("elevation"),
            isStopPlanned = obj.getBoolean("isStopPlanned"),
        )
    }
}
