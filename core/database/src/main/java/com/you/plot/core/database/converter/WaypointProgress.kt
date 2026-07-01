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
import com.you.plot.core.domain.entity.WaypointProgress
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.forEach

@JvmName("waypointProgressListToJson")
fun List<WaypointProgress>.toJson(): String {
    val arr = JSONArray()
    forEach { wp ->
        arr.put(JSONObject().apply {
            put("waypointJson", wp.waypoint.run {
                JSONObject().apply {
                    put("id", id); put("routeId", routeId); put("name", name)
                    put("lat", position.latitude); put("lng", position.longitude)
                    put("orderIndex", orderIndex); put("elevation", elevation)
                    put("isStopPlanned", isStopPlanned)
                }
            })
            put("planned", wp.plannedArrival); put("estimated", wp.estimatedArrival)
            put("remaining", wp.distRemaining); put("reached", wp.isReached)
            put("skipped", wp.wasSkipped)
        })
    }
    return arr.toString()
}

fun String.toWaypointProgressList(): List<WaypointProgress> {
    val arr = JSONArray(this)
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        val wObj = obj.getJSONObject("waypointJson")
        WaypointProgress(
            waypoint = Waypoint(
                id = wObj.getLong("id"), routeId = wObj.getLong("routeId"),
                name = wObj.getString("name"),
                position = LatLng(wObj.getDouble("lat"), wObj.getDouble("lng")),
                orderIndex = wObj.getInt("orderIndex"),
                elevation = wObj.getDouble("elevation"),
                isStopPlanned = wObj.getBoolean("isStopPlanned"),
            ),
            plannedArrival = obj.getLong("planned"),
            estimatedArrival = obj.getLong("estimated"),
            distRemaining = obj.getDouble("remaining"),
            isReached = obj.getBoolean("reached"),
            wasSkipped = obj.getBoolean("skipped"),
        )
    }
}
