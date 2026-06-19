package com.sirofits.youplot.route.ui.screen

import android.preference.PreferenceManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirofits.youplot.domain.entity.LatLng
import com.sirofits.youplot.domain.entity.SportType
import com.sirofits.youplot.route.viewmodel.RoutePlotterViewModel
import com.sirofits.youplot.ui.components.*
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlotterScreen(
    onBack: () -> Unit,
    onRouteSaved: (Long) -> Unit,
    viewModel: RoutePlotterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedRouteId) {
        state.savedRouteId?.let { onRouteSaved(it) }
    }

    state.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar — handled by scaffold
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            YouPlotTopBar(
                title = "Plot Route",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = viewModel::saveRoute,
                        enabled = !state.isSaving,
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Save route")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            // Map takes majority of screen
            OsmMapView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                startPoint = state.startPoint,
                endPoint = state.endPoint,
                waypoints = state.waypoints,
                onMapTap = { latLng ->
                    when {
                        state.startPoint == null -> viewModel.setStartPoint(latLng)
                        state.endPoint == null -> viewModel.setEndPoint(latLng)
                        else -> viewModel.addWaypoint(latLng)
                    }
                },
            )

            // Controls panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::setName,
                    label = { Text("Route name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::setDescription,
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                )

                Text("Sport type", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SportType.entries.forEach { sport ->
                        SportChip(
                            sportType = sport,
                            selected = state.sportType == sport,
                            onClick = { viewModel.setSportType(sport) },
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.isRoundTrip,
                        onCheckedChange = viewModel::setRoundTrip,
                    )
                    Text("Round trip (return via same route)")
                }

                // Waypoint summary
                PlottingStatusRow(
                    hasStart = state.startPoint != null,
                    hasEnd = state.endPoint != null,
                    waypointCount = state.waypoints.size,
                )
            }
        }
    }
}

@Composable
private fun PlottingStatusRow(hasStart: Boolean, hasEnd: Boolean, waypointCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AssistChip(
            onClick = {},
            label = { Text("Start ${if (hasStart) "✓" else "·"}") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (hasStart) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
            )
        )
        AssistChip(
            onClick = {},
            label = { Text("Finish ${if (hasEnd) "✓" else "·"}") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (hasEnd) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
            )
        )
        if (waypointCount > 0) {
            AssistChip(onClick = {}, label = { Text("$waypointCount waypoints") })
        }
    }
}

@Composable
private fun OsmMapView(
    modifier: Modifier,
    startPoint: LatLng?,
    endPoint: LatLng?,
    waypoints: List<LatLng>,
    onMapTap: (LatLng) -> Unit,
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = {
            Configuration.getInstance()
                .load(context, PreferenceManager.getDefaultSharedPreferences(context))
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(14.0)
                // Default center: Nairobi
                controller.setCenter(GeoPoint(-1.286389, 36.817223))

                overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: org.osmdroid.util.GeoPoint): Boolean {
                        onMapTap(LatLng(p.latitude, p.longitude))
                        return true
                    }
                    override fun longPressHelper(p: org.osmdroid.util.GeoPoint) = false
                }))
            }
        },
        update = { mapView ->
            mapView.overlays.removeIf { it is Marker || it is Polyline }

            fun addMarker(latLng: LatLng, title: String, color: Int) {
                Marker(mapView).apply {
                    position = GeoPoint(latLng.latitude, latLng.longitude)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    this.title = title
                    mapView.overlays.add(this)
                }
            }

            startPoint?.let { addMarker(it, "Start", 0) }
            endPoint?.let { addMarker(it, "Finish", 0) }
            waypoints.forEachIndexed { i, wp -> addMarker(wp, "WP ${i + 1}", 0) }

            // Draw polyline if we have at least 2 points
            val allPoints = listOfNotNull(startPoint) + waypoints + listOfNotNull(endPoint)
            if (allPoints.size >= 2) {
                Polyline(mapView).apply {
                    setPoints(allPoints.map { GeoPoint(it.latitude, it.longitude) })
                    mapView.overlays.add(this)
                }
            }

            mapView.invalidate()
        },
    )
}
