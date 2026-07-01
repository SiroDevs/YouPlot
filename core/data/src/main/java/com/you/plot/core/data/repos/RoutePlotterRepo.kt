package com.you.plot.core.data.repos

import com.you.plot.core.common.entity.ElevationPoint
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.RouteCandidate
import com.you.plot.core.common.utils.MapConstants
import com.you.plot.core.common.utils.buildElevationStats
import com.you.plot.core.common.utils.buildFallbackCandidates
import com.you.plot.core.common.utils.countryName
import com.you.plot.core.common.utils.decodePolyline6
import com.you.plot.core.domain.entity.WaypointSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
class PlotterRepo @Inject constructor(
    @Named("osm_user_agent") private val osmUserAgent: String,
) {
    suspend fun searchLocations(query: String): List<WaypointSearchResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val urlString = "${MapConstants.PHOTON_BASE}/api/?q=$encoded&limit=10&lang=en"
                val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    setRequestProperty("User-Agent", osmUserAgent)
                    setRequestProperty("Accept-Language", "en")
                    connectTimeout = 8_000
                    readTimeout = 8_000
                }
                val json = conn.inputStream.bufferedReader().readText()
                conn.disconnect()
                val root = JSONObject(json)
                val features = root.getJSONArray("features")
                (0 until features.length()).map { i ->
                    val feat = features.getJSONObject(i)
                    val props = feat.getJSONObject("properties")
                    val coords = feat.getJSONObject("geometry").getJSONArray("coordinates")
                    val lon = coords.getDouble(0)
                    val lat = coords.getDouble(1)
                    // Build a nice display name from available properties
                    val name = props.optString("name")
                    val street = props.optString("street")
                    val city = props.optString("city")
                    val state = props.optString("state")
                    val country = props.optString("country")
                    val parts = listOfNotNull(
                        name.ifEmpty { null },
                        street.ifEmpty { null },
                        city.ifEmpty { null },
                        state.ifEmpty { null },
                        country.ifEmpty { null },
                    )
                    val displayName = parts.joinToString(", ").take(80)
                    WaypointSearchResult(
                        displayName = displayName.ifEmpty { "$lat, $lon" },
                        latLng = LatLng(lat, lon),
                    )
                }
            }.getOrElse { e ->
                e.printStackTrace()
                // Fallback to Nominatim if Photon fails
                runCatching {
                    val encoded = URLEncoder.encode(query, "UTF-8")
                    val urlString = "${MapConstants.NOMINATIM_BASE}search" +
                        "?q=$encoded&format=jsonv2&limit=10&addressdetails=1"
                    val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                        setRequestProperty("User-Agent", osmUserAgent)
                        setRequestProperty("Accept-Language", "en")
                        connectTimeout = 8_000
                        readTimeout = 8_000
                    }
                    val json = conn.inputStream.bufferedReader().readText()
                    conn.disconnect()
                    val arr = org.json.JSONArray(json)
                    (0 until arr.length()).map { i ->
                        val obj = arr.getJSONObject(i)
                        WaypointSearchResult(
                            displayName = obj.getString("display_name").take(80),
                            latLng = LatLng(obj.getDouble("lat"), obj.getDouble("lon")),
                        )
                    }
                }.getOrElse { emptyList() }
            }
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
                        elevation = 1400.0 + 80 * sin(idx.toDouble() / decodedPoints.size * PI * 2),
                    )
                }
                val (gain, loss) = buildElevationStats(elevProfile)

                RouteCandidate(
                    id = i,
                    waypoints = decodedPoints,
                    elevationProfile = elevProfile,
                    totalDist = distKm,
                    elevationGain = gain,
                    elevationLoss = loss,
                    colorArgb = MapConstants.CANDIDATE_COLORS[i % MapConstants.CANDIDATE_COLORS.size],
                )
            }
        }.getOrElse { e ->
            e.printStackTrace()
            buildFallbackCandidates(start, end, via)
        }
    }


    suspend fun reverseGeocode(latLng: LatLng): String? = withContext(Dispatchers.IO) {
        runCatching {
            val url = "${MapConstants.PHOTON_BASE}/reverse?lon=${latLng.longitude}&lat=${latLng.latitude}&limit=1"
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                setRequestProperty("User-Agent", osmUserAgent)
                connectTimeout = 6_000; readTimeout = 6_000
            }
            val json = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            val feat = JSONObject(json).getJSONArray("features").optJSONObject(0) ?: return@runCatching null
            val props = feat.getJSONObject("properties")
            val name = props.optString("name")
            val street = props.optString("street")
            val city = props.optString("city")
            listOfNotNull(name.ifEmpty { null }, street.ifEmpty { null }, city.ifEmpty { null })
                .joinToString(", ").ifEmpty { null }
        }.getOrNull()
    }

    suspend fun resolveAreaLabel(latLng: LatLng): String = withContext(Dispatchers.IO) {
        runCatching {
            val url = "${MapConstants.NOMINATIM_BASE}reverse?lat=${latLng.latitude}&lon=${latLng.longitude}" +
                "&format=jsonv2&zoom=14&addressdetails=1"
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                setRequestProperty("User-Agent", osmUserAgent)
                connectTimeout = 5_000; readTimeout = 5_000
            }
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            conn.disconnect()
            val addr = json.optJSONObject("address")
            listOfNotNull(
                addr?.optString("suburb")?.ifEmpty { null },
                addr?.optString("neighbourhood")?.ifEmpty { null },
                addr?.optString("village")?.ifEmpty { null },
                addr?.optString("town")?.ifEmpty { null },
                addr?.optString("city")?.ifEmpty { null },
                addr?.optString("county")?.ifEmpty { null },
                json.optString("display_name").split(",").firstOrNull()?.trim()?.ifEmpty { null },
            ).firstOrNull()
        }.getOrNull()
            ?: run {
                val ns = if (latLng.latitude >= 0) "Northern" else "Southern"
                val ew = if (latLng.longitude >= 0) "Eastern" else "Western"
                "$ns $ew Area"
            }
    }

    /**
     * Photon has no `countrycodes` parameter, so bias the query by appending the country
     * name to the search terms, then filter server responses that carry a mismatching
     * `countrycode` property. Falls back to Nominatim (via [searchLocations]) on failure —
     * Nominatim actually supports countrycodes and returns country-scoped hits.
     */
    suspend fun searchLocationsWithCountry(query: String, countryCode: String): List<WaypointSearchResult> =
        withContext(Dispatchers.IO) {
            if (countryCode.isBlank()) return@withContext searchLocations(query)
            runCatching {
                val biasedQuery = countryName(countryCode)?.let { "$query, $it" } ?: query
                val encoded = URLEncoder.encode(biasedQuery, "UTF-8")
                val urlString = "${MapConstants.PHOTON_BASE}/api/?q=$encoded&limit=10&lang=en"
                val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    setRequestProperty("User-Agent", osmUserAgent)
                    connectTimeout = 8_000; readTimeout = 8_000
                }
                val json = conn.inputStream.bufferedReader().readText()
                conn.disconnect()
                val features = JSONObject(json).getJSONArray("features")
                (0 until features.length()).mapNotNull { i ->
                    val feat = features.getJSONObject(i)
                    val props = feat.getJSONObject("properties")
                    val cc = props.optString("countrycode").lowercase()
                    if (cc.isNotBlank() && cc != countryCode.lowercase()) return@mapNotNull null
                    val coords = feat.getJSONObject("geometry").getJSONArray("coordinates")
                    val lon = coords.getDouble(0); val lat = coords.getDouble(1)
                    val parts = listOfNotNull(
                        props.optString("name").ifEmpty { null },
                        props.optString("street").ifEmpty { null },
                        props.optString("city").ifEmpty { null },
                        props.optString("state").ifEmpty { null },
                    )
                    WaypointSearchResult(
                        displayName = parts.joinToString(", ").take(80).ifEmpty { "$lat, $lon" },
                        latLng = LatLng(lat, lon),
                    )
                }
            }.getOrElse {
                runCatching { nominatimSearch(query, countryCode) }.getOrElse { emptyList() }
            }
        }

    private fun nominatimSearch(query: String, countryCode: String): List<WaypointSearchResult> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val urlString = "${MapConstants.NOMINATIM_BASE}search" +
            "?q=$encoded&format=jsonv2&limit=10&addressdetails=1&countrycodes=$countryCode"
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            setRequestProperty("User-Agent", osmUserAgent)
            setRequestProperty("Accept-Language", "en")
            connectTimeout = 8_000; readTimeout = 8_000
        }
        val json = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        val arr = org.json.JSONArray(json)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            WaypointSearchResult(
                displayName = obj.getString("display_name").take(80),
                latLng = LatLng(obj.getDouble("lat"), obj.getDouble("lon")),
            )
        }
    }
}
