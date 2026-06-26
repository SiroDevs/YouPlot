package com.you.plot.core.data.repos

import com.you.plot.core.common.entity.ElevationPoint
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.RouteCandidate
import com.you.plot.core.common.utils.MapConstants
import com.you.plot.core.common.utils.buildElevationStats
import com.you.plot.core.common.utils.buildFallbackCandidates
import com.you.plot.core.common.utils.decodePolyline6
import com.you.plot.core.domain.entity.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.sin

@Singleton
class RoutePlotterRepo @Inject constructor(
    @Named("osm_user_agent") private val osmUserAgent: String,
) {
    suspend fun searchLocations(query: String): List<SearchResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val urlString = "${MapConstants.OSRM_BASE}search" +
                    "?q=$encoded&format=jsonv2&limit=10&addressdetails=1&extratags=1&namedetails=1"
                val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    setRequestProperty("User-Agent", osmUserAgent)
                    setRequestProperty("Accept-Language", "en")
                    connectTimeout = 8_000
                    readTimeout = 8_000
                }
                val json = conn.inputStream.bufferedReader().readText()
                conn.disconnect()
                val arr = JSONArray(json)
                (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    SearchResult(
                        displayName = obj.getString("display_name").take(80),
                        latLng = LatLng(obj.getDouble("lat"), obj.getDouble("lon")),
                    )
                }
            }.getOrElse { e -> e.printStackTrace(); emptyList() }
        }

    suspend fun fetchRouteCandidates(
        start: LatLng,
        end: LatLng,
        via: List<LatLng>,
    ): List<RouteCandidate> = withContext(Dispatchers.IO) {
        runCatching {
            val allPoints = listOf(start) + via + listOf(end)
            val coordStr = allPoints.joinToString(";") { "${it.longitude},${it.latitude}" }
            val url = "${MapConstants.OSRM_ROUTER}/$coordStr" +
                "?overview=full&geometries=polyline6&alternatives=true&steps=false"

            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                setRequestProperty("User-Agent", osmUserAgent)
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            conn.disconnect()

            if (json.getString("code") != "Ok") return@runCatching emptyList()

            val routes = json.getJSONArray("routes")
            (0 until routes.length()).map { i ->
                val route = routes.getJSONObject(i)
                val decodedPoints = decodePolyline6(route.getString("geometry"))
                val distKm = route.getDouble("distance") / 1000.0

                val elevProfile = decodedPoints.mapIndexed { idx, _ ->
                    ElevationPoint(
                        distanceKm = distKm * idx.toDouble() / decodedPoints.size,
                        elevationMeters = 1400.0 + 80 * sin(idx.toDouble() / decodedPoints.size * PI * 2),
                    )
                }
                val (gain, loss) = buildElevationStats(elevProfile)

                RouteCandidate(
                    id = i,
                    waypoints = decodedPoints,
                    elevationProfile = elevProfile,
                    totalDistanceKm = distKm,
                    totalElevationGainMeters = gain,
                    totalElevationLossMeters = loss,
                    colorArgb = MapConstants.CANDIDATE_COLORS[i % MapConstants.CANDIDATE_COLORS.size],
                )
            }
        }.getOrElse { e ->
            e.printStackTrace()
            buildFallbackCandidates(start, end, via)
        }
    }
}
