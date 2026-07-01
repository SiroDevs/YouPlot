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
