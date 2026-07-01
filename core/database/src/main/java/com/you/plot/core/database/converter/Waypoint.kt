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
            put("orderIndex", wp.orderIndex); put("elevation", wp.elevation)
            put("distFromStart", wp.distFromStart)
            put("isStopPlanned", wp.isStopPlanned)
            put("countryCode", wp.countryCode)
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
            elevation = obj.getDouble("elevation"),
            distFromStart = obj.optDouble("distFromStart", 0.0),
            isStopPlanned = obj.getBoolean("isStopPlanned"),
            countryCode = obj.optString("countryCode", ""),
        )
    }
}
