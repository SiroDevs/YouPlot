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

import com.you.plot.core.domain.entity.ElevationPoint
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.forEach

@JvmName("elevationListToJson")
fun List<ElevationPoint>.toJson(): String {
    val arr = JSONArray()
    forEach { ep -> arr.put(JSONObject().apply { put("d", ep.distanceKm); put("e", ep.elevationMeters) }) }
    return arr.toString()
}

fun String.toElevationList(): List<ElevationPoint> {
    val arr = JSONArray(this)
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        ElevationPoint(obj.getDouble("d"), obj.getDouble("e"))
    }
}
