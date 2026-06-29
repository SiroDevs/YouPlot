package com.you.plot.feature.route.plotter.view.components

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.utils.boundingBox
import com.you.plot.core.common.utils.boundingBoxOfAll
import com.you.plot.feature.route.plotter.utils.makeCircleMarker
import com.you.plot.feature.route.plotter.utils.makePinMarker
import com.you.plot.core.common.entity.RouteCandidate
import com.you.plot.core.common.utils.MapConstants
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.max
import androidx.core.graphics.drawable.toDrawable
import kotlinx.coroutines.Dispatchers

@SuppressLint("MissingPermission")
@Composable
fun PlotterMap(
    modifier: Modifier,
    startPoint: LatLng?,
    endPoint: LatLng?,
    waypoints: List<LatLng>,
    candidates: List<RouteCandidate>,
    selectedCandidateId: Int?,
    isRoundTrip: Boolean = false,
    startPointName: String = "",
    endPointName: String = "",
    waypointNames: List<String> = emptyList(),
    routePolyline: List<LatLng> = emptyList(),
    onMapTap: (LatLng) -> Unit,
    onWaypointMoved: ((index: Int, newLatLng: LatLng) -> Unit)? = null,
    onWaypointDelete: ((index: Int) -> Unit)? = null,
) {
    val context = LocalContext.current

    var menuWaypointIndex by remember { mutableIntStateOf(-1) }
    var menuAnchorPoint by remember { mutableStateOf<GeoPoint?>(null) }

    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(MapConstants.COUNTRY_ZOOM)
            controller.setCenter(MapConstants.KENYA_CENTER)
        }
    }

    val myLocationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply { enableMyLocation() }
    }

    LaunchedEffect(Unit) {
        if (!mapView.overlays.contains(myLocationOverlay)) mapView.overlays.add(myLocationOverlay)

        withContext(Dispatchers.IO) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val twoMin = 2 * 60 * 1000L
                val providers = listOf(
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER
                ).filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }

                val cached = providers.firstNotNullOfOrNull { p ->
                    lm.getLastKnownLocation(p)
                        ?.takeIf { System.currentTimeMillis() - it.time < twoMin }
                }
                withContext(Dispatchers.Main) {
                    if (cached != null) {
                        mapView.controller.setZoom(MapConstants.CITY_ZOOM)
                        mapView.controller.animateTo(GeoPoint(cached.latitude, cached.longitude))
                    } else {
                        myLocationOverlay.runOnFirstFix {
                            myLocationOverlay.myLocation?.let { loc ->
                                mapView.post {
                                    mapView.controller.setZoom(MapConstants.CITY_ZOOM)
                                    mapView.controller.animateTo(loc)
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) { /* stay on Kenya */
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { myLocationOverlay.disableMyLocation(); mapView.onDetach() }
    }

    LaunchedEffect(onMapTap) {
        mapView.overlays.removeAll { it is MapEventsOverlay }
        val tap = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                menuWaypointIndex = -1
                p?.let { onMapTap(LatLng(it.latitude, it.longitude)) }
                return true
            }

            override fun longPressHelper(p: GeoPoint?) = false
        })
        val locIdx = mapView.overlays.indexOf(myLocationOverlay)
        if (locIdx >= 0) mapView.overlays.add(locIdx, tap) else mapView.overlays.add(0, tap)
    }

    LaunchedEffect(startPoint, endPoint, waypoints, candidates, selectedCandidateId, isRoundTrip, routePolyline) {
        val tapOverlay = mapView.overlays.firstOrNull { it is MapEventsOverlay }
        val hasMyLoc = mapView.overlays.contains(myLocationOverlay)
        mapView.overlays.clear()
        tapOverlay?.let { mapView.overlays.add(it) }
        if (hasMyLoc) mapView.overlays.add(myLocationOverlay)

        if (routePolyline.size >= 2) {
            val primaryColor = android.graphics.Color.argb(220, 33, 150, 243) // blue
            mapView.overlays.add(
                Polyline(mapView).apply {
                    setPoints(routePolyline.map { GeoPoint(it.latitude, it.longitude) })
                    outlinePaint.color = primaryColor
                    outlinePaint.strokeWidth = 10f
                }
            )
        }

        candidates.sortedBy { if (it.id == selectedCandidateId) 1 else 0 }.forEach { c ->
            val selected = c.id == selectedCandidateId
            val raw = c.colorArgb
            mapView.overlays.add(Polyline(mapView).apply {
                setPoints(c.waypoints.map { GeoPoint(it.latitude, it.longitude) })
                outlinePaint.color = android.graphics.Color.argb(
                    if (selected) 220 else 90,
                    ((raw shr 16) and 0xFF).toInt(),
                    ((raw shr 8) and 0xFF).toInt(),
                    (raw and 0xFF).toInt(),
                )
                outlinePaint.strokeWidth = if (selected) 10f else 5f
            })
        }

        waypoints.forEachIndexed { i, pt ->
            mapView.overlays.add(Marker(mapView).apply {
                position = GeoPoint(pt.latitude, pt.longitude)
                title = "Waypoint ${i + 1}"
                icon = makeCircleMarker(
                    context, MapConstants.COLOR_WAYPOINT, "${i + 1}"
                ).toDrawable(context.resources)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                isDraggable = onWaypointMoved != null
                setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                    override fun onMarkerDragStart(marker: Marker) {}
                    override fun onMarkerDrag(marker: Marker) {}
                    override fun onMarkerDragEnd(marker: Marker) {
                        onWaypointMoved?.invoke(
                            i, LatLng(marker.position.latitude, marker.position.longitude)
                        )
                    }
                })
                setOnMarkerClickListener { _, _ ->
                    menuWaypointIndex = i
                    menuAnchorPoint = GeoPoint(pt.latitude, pt.longitude)
                    true
                }
            })
        }

        startPoint?.let { pt ->
            mapView.overlays.add(Marker(mapView).apply {
                position = GeoPoint(pt.latitude, pt.longitude)
                title = startPointName.ifBlank { "Start" }
                snippet = null   // no coordinates — name is the title
                icon = makePinMarker(context, MapConstants.COLOR_START, "S").toDrawable(context.resources)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            })
        }

        endPoint?.let { pt ->
            mapView.overlays.add(Marker(mapView).apply {
                position = GeoPoint(pt.latitude, pt.longitude)
                title = endPointName.ifBlank { if (isRoundTrip) "Turning Point" else "Destination" }
                snippet = null   // no coordinates
                icon = makePinMarker(
                    context,
                    if (isRoundTrip) MapConstants.COLOR_TURN else MapConstants.COLOR_END,
                    if (isRoundTrip) "T" else "D"
                ).toDrawable(context.resources)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            })
        }

        val selectedCandidate = candidates.firstOrNull { it.id == selectedCandidateId }

        when {
            selectedCandidate != null && selectedCandidate.waypoints.isNotEmpty() -> {
                val bbox = boundingBoxOfAll(selectedCandidate.waypoints)
                if (bbox != null) {
                    mapView.post {
                        mapView.zoomToBoundingBox(
                            BoundingBox(bbox.maxLat, bbox.maxLng, bbox.minLat, bbox.minLng),
                            true, 120,
                            MapConstants.WAYPOINT_ZOOM,
                            500L,
                        )
                    }
                }
            }

            startPoint != null && endPoint != null -> {
                val allPts = listOfNotNull(startPoint, endPoint) + waypoints
                val bbox = boundingBox(*allPts.toTypedArray())
                if (bbox != null) {
                    val padding = max(
                        120,
                        if (bbox.latSpan < 0.05 && bbox.lngSpan < 0.05) 200 else 120,
                    )
                    mapView.post {
                        mapView.zoomToBoundingBox(
                            BoundingBox(bbox.maxLat, bbox.maxLng, bbox.minLat, bbox.minLng),
                            true,
                            padding,
                            MapConstants.WAYPOINT_ZOOM,
                            500L,
                        )
                    }
                }
            }

            startPoint != null && waypoints.isNotEmpty() -> {
                val allPts = listOf(startPoint) + waypoints
                val bbox = boundingBox(*allPts.toTypedArray())
                if (bbox != null) {
                    mapView.post {
                        mapView.zoomToBoundingBox(
                            BoundingBox(bbox.maxLat, bbox.maxLng, bbox.minLat, bbox.minLng),
                            true, 160, MapConstants.WAYPOINT_ZOOM, 400L,
                        )
                    }
                }
            }

            startPoint != null -> {
                mapView.post {
                    mapView.controller.animateTo(
                        GeoPoint(startPoint.latitude, startPoint.longitude),
                        MapConstants.CITY_ZOOM,
                        500L,
                    )
                }
            }
        }
        mapView.invalidate()
    }

    AndroidView(factory = { mapView }, modifier = modifier)

    if (menuWaypointIndex >= 0) {
        WaypointPopup(
            waypointIndex = menuWaypointIndex,
            onDismiss = { menuWaypointIndex = -1 },
            onWaypointMoved = onWaypointMoved,
            onWaypointDelete = onWaypointDelete
        )
    }
}
