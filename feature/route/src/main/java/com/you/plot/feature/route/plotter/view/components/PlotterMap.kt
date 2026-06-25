package com.you.plot.feature.route.plotter.view.components

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.you.plot.core.domain.entity.LatLng
import com.you.plot.feature.route.list.viewmodel.RouteCandidate
import com.you.plot.feature.route.plotter.view.screen.fmt
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

private val KENYA_CENTER = GeoPoint(-0.0236, 37.9062)
private const val COUNTRY_ZOOM = 7.0
private const val CITY_ZOOM = 12.0

@SuppressLint("MissingPermission")
@Composable
fun PlotterMap(
    modifier: Modifier,
    startPoint: LatLng?,
    endPoint: LatLng?,
    waypoints: List<LatLng>,
    candidates: List<RouteCandidate>,
    selectedCandidateId: Int?,
    onMapTap: (LatLng) -> Unit,
) {
    val context = LocalContext.current

    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(COUNTRY_ZOOM)
            controller.setCenter(KENYA_CENTER)
        }
    }

    // One-shot: pan to user's country / location on first composition.
    LaunchedEffect(Unit) {
        try {
            val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE)
                as android.location.LocationManager

            // Try GPS first, then fall back to network provider.
            val providers = listOf(
                android.location.LocationManager.GPS_PROVIDER,
                android.location.LocationManager.NETWORK_PROVIDER,
            )
            val location = providers
                .firstOrNull { lm.isProviderEnabled(it) }
                ?.let { lm.getLastKnownLocation(it) }

            if (location != null) {
                mapView.controller.setZoom(CITY_ZOOM)
                mapView.controller.animateTo(GeoPoint(location.latitude, location.longitude))
            }
            // else: already centred on Kenya in the `remember` block above
        } catch (_: SecurityException) {
            // Permission not granted — stay on Kenya fallback
        } catch (_: Exception) {
            // Any other failure (provider not available, etc.) — stay on Kenya fallback
        }
    }

    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }

    // Re-attach the tap listener whenever the callback lambda changes (stage navigation).
    LaunchedEffect(onMapTap) {
        mapView.overlays.removeAll { it is MapEventsOverlay }
        val eventsOverlay = MapEventsOverlay(
            object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                    p?.let { onMapTap(LatLng(it.latitude, it.longitude)) }
                    return true
                }

                override fun longPressHelper(p: GeoPoint?) = false
            }
        )
        mapView.overlays.add(0, eventsOverlay)
    }

    // Redraw markers / polylines whenever map data changes.
    LaunchedEffect(startPoint, endPoint, waypoints, candidates, selectedCandidateId) {
        val eventsOverlay = mapView.overlays.firstOrNull { it is MapEventsOverlay }
        mapView.overlays.clear()
        eventsOverlay?.let { mapView.overlays.add(it) }

        // Draw route candidates (selected on top).
        candidates.sortedBy { if (it.id == selectedCandidateId) 1 else 0 }.forEach { candidate ->
            val line = Polyline(mapView).apply {
                setPoints(candidate.waypoints.map { GeoPoint(it.latitude, it.longitude) })
                val alpha = if (candidate.id == selectedCandidateId) 220 else 100
                val raw = candidate.colorArgb
                outlinePaint.color = android.graphics.Color.argb(
                    alpha,
                    ((raw shr 16) and 0xFF).toInt(),
                    ((raw shr 8) and 0xFF).toInt(),
                    (raw and 0xFF).toInt(),
                )
                outlinePaint.strokeWidth = if (candidate.id == selectedCandidateId) 8f else 5f
            }
            mapView.overlays.add(line)
        }

        // Waypoint markers.
        waypoints.forEachIndexed { i, pt ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(pt.latitude, pt.longitude)
                title = "Waypoint ${i + 1}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mapView.overlays.add(marker)
        }

        // Start marker — also pans the camera if set.
        startPoint?.let { pt ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(pt.latitude, pt.longitude)
                title = "Start"
                snippet = "${pt.latitude.fmt()}, ${pt.longitude.fmt()}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mapView.overlays.add(marker)
            mapView.controller.setZoom(CITY_ZOOM)
            mapView.controller.animateTo(GeoPoint(pt.latitude, pt.longitude))
        }

        endPoint?.let { pt ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(pt.latitude, pt.longitude)
                title = "Destination"
                snippet = "${pt.latitude.fmt()}, ${pt.longitude.fmt()}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}
