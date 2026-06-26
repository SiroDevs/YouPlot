package com.you.plot.feature.route.plotter.view.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.location.LocationManager
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import com.you.plot.core.domain.entity.LatLng
import com.you.plot.feature.route.list.viewmodel.RouteCandidate
import com.you.plot.feature.route.plotter.view.screen.fmt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private val KENYA_CENTER = GeoPoint(-0.0236, 37.9062)
private const val COUNTRY_ZOOM = 7.5
private const val CITY_ZOOM = 13.0

private const val COLOR_START = 0xFF43A047.toInt()
private const val COLOR_END = 0xFFE53935.toInt()
private const val COLOR_WAYPOINT = 0xFF1E88E5.toInt()
private const val COLOR_TURN = 0xFFFF8F00.toInt()
private const val COLOR_MARKER_TEXT = Color.WHITE

private const val PIN_W = 72;
private const val PIN_H = 90
private const val CIRCLE_R = 28f

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
            controller.setZoom(COUNTRY_ZOOM)
            controller.setCenter(KENYA_CENTER)
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
                val providers =
                    listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                        .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }

                val cached = providers.firstNotNullOfOrNull { p ->
                    lm.getLastKnownLocation(p)
                        ?.takeIf { System.currentTimeMillis() - it.time < twoMin }
                }
                withContext(Dispatchers.Main) {
                    if (cached != null) {
                        mapView.controller.setZoom(CITY_ZOOM)
                        mapView.controller.animateTo(GeoPoint(cached.latitude, cached.longitude))
                    } else {
                        myLocationOverlay.runOnFirstFix {
                            myLocationOverlay.myLocation?.let { loc ->
                                mapView.post {
                                    mapView.controller.setZoom(CITY_ZOOM)
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
                menuWaypointIndex = -1          // dismiss any open menu
                p?.let { onMapTap(LatLng(it.latitude, it.longitude)) }
                return true
            }

            override fun longPressHelper(p: GeoPoint?) = false
        })
        val locIdx = mapView.overlays.indexOf(myLocationOverlay)
        if (locIdx >= 0) mapView.overlays.add(locIdx, tap) else mapView.overlays.add(0, tap)
    }

    LaunchedEffect(startPoint, endPoint, waypoints, candidates, selectedCandidateId, isRoundTrip) {
        val tapOverlay = mapView.overlays.firstOrNull { it is MapEventsOverlay }
        val hasMyLoc = mapView.overlays.contains(myLocationOverlay)
        mapView.overlays.clear()
        tapOverlay?.let { mapView.overlays.add(it) }
        if (hasMyLoc) mapView.overlays.add(myLocationOverlay)

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
                icon = android.graphics.drawable.BitmapDrawable(
                    context.resources,
                    makeCircleMarker(context, COLOR_WAYPOINT, "${i + 1}"),
                )
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                isDraggable = onWaypointMoved != null

                // Drag end → update VM
                setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                    override fun onMarkerDragStart(marker: Marker) {}
                    override fun onMarkerDrag(marker: Marker) {}
                    override fun onMarkerDragEnd(marker: Marker) {
                        onWaypointMoved?.invoke(
                            i,
                            LatLng(marker.position.latitude, marker.position.longitude)
                        )
                    }
                })

                // Tap → open context menu
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
                title = "Start"
                snippet = "${pt.latitude.fmt()}, ${pt.longitude.fmt()}"
                icon = android.graphics.drawable.BitmapDrawable(
                    context.resources, makePinMarker(context, COLOR_START, "S"),
                )
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            })
            mapView.controller.setZoom(CITY_ZOOM)
            mapView.controller.animateTo(GeoPoint(pt.latitude, pt.longitude))
        }

        endPoint?.let { pt ->
            val isReturn = isRoundTrip
            mapView.overlays.add(Marker(mapView).apply {
                position = GeoPoint(pt.latitude, pt.longitude)
                title = if (isReturn) "Turning Point" else "Destination"
                snippet = "${pt.latitude.fmt()}, ${pt.longitude.fmt()}"
                icon = android.graphics.drawable.BitmapDrawable(
                    context.resources,
                    makePinMarker(
                        context,
                        if (isReturn) COLOR_TURN else COLOR_END,
                        if (isReturn) "T" else "D"
                    ),
                )
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            })
        }

        mapView.invalidate()
    }

    AndroidView(factory = { mapView }, modifier = modifier)

    if (menuWaypointIndex >= 0) {
        Popup(onDismissRequest = { menuWaypointIndex = -1 }) {
            Column(
                Modifier
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(4.dp)
                    .width(200.dp),
            ) {
                Text(
                    "Waypoint ${menuWaypointIndex + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                // Move hint row
                if (onWaypointMoved != null) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Default.OpenWith,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(4.dp)
                        )
                        Text(
                            "Long-press marker to drag",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                // Delete button
                if (onWaypointDelete != null) {
                    val idx = menuWaypointIndex
                    TextButton(
                        onClick = { onWaypointDelete(idx); menuWaypointIndex = -1 },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            Icons.Default.Delete, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Delete waypoint", color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

private fun makePinMarker(context: Context, color: Int, label: String): Bitmap {
    val bmp = Bitmap.createBitmap(PIN_W, PIN_H, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)

    val fillPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
    val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 3f
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = COLOR_MARKER_TEXT; textSize = 26f; typeface =
        Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
    }

    val cx = PIN_W / 2f
    val cy = CIRCLE_R + 4f   // centre of the circle head

    // Circle head
    canvas.drawCircle(cx, cy, CIRCLE_R, fillPaint)
    canvas.drawCircle(cx, cy, CIRCLE_R, rimPaint)

    // Pointed tail (teardrop)
    val path = Path().apply {
        moveTo(cx - CIRCLE_R * 0.6f, cy + CIRCLE_R * 0.6f)
        lineTo(cx, PIN_H.toFloat() - 4f)
        lineTo(cx + CIRCLE_R * 0.6f, cy + CIRCLE_R * 0.6f)
        close()
    }
    canvas.drawPath(path, fillPaint)
    canvas.drawPath(path, rimPaint)

    // Label
    val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2
    canvas.drawText(label, cx, textY, textPaint)
    return bmp
}

private fun makeCircleMarker(context: Context, color: Int, label: String): Bitmap {
    val size = (CIRCLE_R * 2 + 8).toInt()
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val cx = size / 2f;
    val cy = size / 2f;
    val r = CIRCLE_R

    val fillPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
    val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 3f
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = COLOR_MARKER_TEXT; textSize = 22f; typeface =
        Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
    }

    canvas.drawCircle(cx, cy, r, fillPaint)
    canvas.drawCircle(cx, cy, r, rimPaint)
    val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2
    canvas.drawText(label, cx, textY, textPaint)
    return bmp
}