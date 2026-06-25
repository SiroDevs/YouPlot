package com.you.plot.feature.tracker.view.components

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.you.plot.core.domain.entity.LatLng
import com.you.plot.core.domain.entity.WaypointProgress
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.collections.forEach

@Composable
fun TrackerMap(
    modifier: Modifier,
    currentLocation: LatLng?,
    waypoints: List<WaypointProgress>,
) {
    val context = LocalContext.current
    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
        }
    }

    DisposableEffect(Unit) { onDispose { mapView.onDetach() } }

    LaunchedEffect(currentLocation, waypoints) {
        mapView.overlays.clear()

        if (waypoints.size >= 2) {
            val line = Polyline(mapView).apply {
                setPoints(waypoints.map {
                    GeoPoint(
                        it.waypoint.position.latitude,
                        it.waypoint.position.longitude
                    )
                })
                outlinePaint.color = Color.argb(180, 33, 150, 243)
                outlinePaint.strokeWidth = 7f
            }
            mapView.overlays.add(line)
        }

        waypoints.forEach { wp ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(wp.waypoint.position.latitude, wp.waypoint.position.longitude)
                title = wp.waypoint.name
                snippet =
                    if (wp.isReached) "Reached" else "%.2f km away".format(wp.distanceRemainingKm)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mapView.overlays.add(marker)
        }

        currentLocation?.let { loc ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(loc.latitude, loc.longitude)
                title = "You"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            mapView.overlays.add(marker)
            mapView.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
        }

        mapView.invalidate()
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}
