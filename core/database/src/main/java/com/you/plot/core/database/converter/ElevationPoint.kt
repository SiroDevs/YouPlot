package com.you.plot.core.database.converter

import com.you.plot.core.common.entity.ElevationPoint
import com.you.plot.core.common.entity.LatLng
import org.json.JSONArray
import org.json.JSONObject

@JvmName("elevationListToJson")
fun List<ElevationPoint>.toJson(): String {
    val arr = JSONArray()
    forEach { ep -> arr.put(JSONObject().apply { put("d", ep.dist); put("e", ep.elevation) }) }
    return arr.toString()
}

fun String.toElevationList(): List<ElevationPoint> {
    val arr = JSONArray(this)
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        ElevationPoint(obj.getDouble("d"), obj.getDouble("e"))
    }
}

@JvmName("latLngListToJson")
fun List<LatLng>.polylineJson(): String {
    val arr = JSONArray()
    forEach { p -> arr.put(JSONObject().apply { put("lat", p.latitude); put("lng", p.longitude) }) }
    return arr.toString()
}

fun String.toLatLngList(): List<LatLng> {
    if (isBlank()) return emptyList()
    val arr = JSONArray(this)
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        LatLng(obj.getDouble("lat"), obj.getDouble("lng"))
    }
}
