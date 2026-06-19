package com.sirofits.youplot.tracker.ui.screen

import android.content.Context
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.sirofits.youplot.common.util.DistanceUtils
import com.sirofits.youplot.common.util.TimeUtils
import com.sirofits.youplot.domain.entity.ActivitySession
import com.sirofits.youplot.domain.entity.LatLng
import com.sirofits.youplot.domain.entity.SessionStatus
import com.sirofits.youplot.domain.entity.WaypointProgress
import com.sirofits.youplot.tracker.viewmodel.TrackerViewModel
import com.sirofits.youplot.ui.components.EmptyState
import com.sirofits.youplot.ui.components.StatChip
import com.sirofits.youplot.ui.components.YouPlotTopBar
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    onBack: () -> Unit,
    viewModel: TrackerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Permission handling
    val locationPermission = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) { granted -> viewModel.onPermissionResult(granted) }

    LaunchedEffect(Unit) {
        if (!locationPermission.status.isGranted) {
            locationPermission.launchPermissionRequest()
        } else {
            viewModel.onPermissionResult(true)
        }
    }

    // Waypoint stop dialog
    state.pendingStopWaypoint?.let { wp ->
        WaypointStopDialog(
            waypointProgress = wp,
            onStop = viewModel::onStopAcknowledged,
            onIgnore = viewModel::onStopIgnored,
        )
    }

    val sheetState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = sheetState,
        topBar = {
            YouPlotTopBar(
                title = when (state.session?.status) {
                    SessionStatus.IN_PROGRESS -> "● Tracking"
                    SessionStatus.PAUSED -> "⏸ Paused"
                    SessionStatus.COMPLETED -> "✓ Completed"
                    else -> "Activity Tracker"
                },
                onBack = onBack,
            )
        },
        sheetContent = {
            WaypointProgressSheet(
                waypointProgress = state.session?.waypointProgress ?: emptyList(),
            )
        },
        sheetPeekHeight = 180.dp,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Live map
            LiveMapView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                currentLocation = state.session?.currentLocation,
                waypointProgress = state.session?.waypointProgress ?: emptyList(),
            )

            // Stats strip
            state.session?.let { session ->
                LiveStatsRow(
                    session = session,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                )

                // Control buttons
                SessionControlRow(
                    status = session.status,
                    onStart = {
                        viewModel.startSession()
                        context.startService(com.sirofits.youplot.tracker.service.TrackingService.startIntent(context))
                    },
                    onPause = {
                        viewModel.pauseSession()
                        context.startService(com.sirofits.youplot.tracker.service.TrackingService.pauseIntent(context))
                    },
                    onResume = {
                        viewModel.resumeSession()
                        context.startService(com.sirofits.youplot.tracker.service.TrackingService.startIntent(context))
                    },
                    onComplete = {
                        viewModel.completeSession()
                        context.startService(com.sirofits.youplot.tracker.service.TrackingService.stopIntent(context))
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
                )
            } ?: run {
                // Session not started yet
                Box(
                    Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        onClick = {
                            viewModel.startSession()
                            context.startService(com.sirofits.youplot.tracker.service.TrackingService.startIntent(context))
                        },
                        modifier = Modifier.height(56.dp).fillMaxWidth(),
                    ) {
                        Text("Start Activity", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveStatsRow(session: ActivitySession, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatChip(
            label = "Distance",
            value = DistanceUtils.formatKm(session.distanceCoveredKm),
            modifier = Modifier.weight(1f),
        )
        StatChip(
            label = "Elapsed",
            value = TimeUtils.formatElapsedTime(session.elapsedTimeSeconds),
            modifier = Modifier.weight(1f),
        )
        StatChip(
            label = "Speed",
            value = DistanceUtils.formatSpeed(session.currentSpeedKmh),
            modifier = Modifier.weight(1f),
        )
        session.estimatedCompletionMillis?.let {
            StatChip(
                label = "ETA finish",
                value = TimeUtils.formatTime(it),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SessionControlRow(
    status: SessionStatus,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        when (status) {
            SessionStatus.NOT_STARTED -> {
                Button(onClick = onStart, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Start")
                }
            }
            SessionStatus.IN_PROGRESS -> {
                OutlinedButton(onClick = onPause, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Pause")
                }
                Button(onClick = onComplete, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Finish")
                }
            }
            SessionStatus.PAUSED -> {
                Button(onClick = onResume, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Resume")
                }
                OutlinedButton(onClick = onComplete, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Finish")
                }
            }
            SessionStatus.COMPLETED -> {
                Surface(
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Activity complete ✓",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun WaypointProgressSheet(waypointProgress: List<WaypointProgress>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            "Waypoints",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 12.dp),
        )

        if (waypointProgress.isEmpty()) {
            EmptyState(
                emoji = "📍",
                title = "No waypoints",
                subtitle = "Start your activity to see progress",
                modifier = Modifier.height(120.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(waypointProgress) { wp ->
                    WaypointProgressRow(wp)
                }
            }
        }
    }
}

@Composable
private fun WaypointProgressRow(wp: WaypointProgress) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Status indicator
        Surface(
            shape = RoundedCornerShape(50),
            color = when {
                wp.isReached -> MaterialTheme.colorScheme.primary
                wp.wasSkipped -> MaterialTheme.colorScheme.outlineVariant
                else -> MaterialTheme.colorScheme.secondaryContainer
            },
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    when {
                        wp.isReached -> "✓"
                        wp.wasSkipped -> "—"
                        else -> "${wp.waypoint.orderIndex}"
                    },
                    fontSize = 12.sp,
                    color = when {
                        wp.isReached -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSecondaryContainer
                    },
                )
            }
        }

        Column(Modifier.weight(1f)) {
            Text(wp.waypoint.name, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Planned: ${TimeUtils.formatTime(wp.plannedArrivalMillis)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!wp.isReached) {
                    Text(
                        "ETA: ${TimeUtils.formatTime(wp.estimatedArrivalMillis)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }

        if (!wp.isReached) {
            Text(
                DistanceUtils.formatKm(wp.distanceRemainingKm),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WaypointStopDialog(
    waypointProgress: WaypointProgress,
    onStop: () -> Unit,
    onIgnore: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onIgnore,
        icon = { Text("📍", fontSize = 32.sp) },
        title = { Text("Waypoint reached: ${waypointProgress.waypoint.name}") },
        text = {
            Text(
                "You planned a stop here. Are you stopping?",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(onClick = onStop) {
                Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Yes, stopping")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onIgnore) {
                Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Keep going")
            }
        },
    )
}

@Composable
private fun LiveMapView(
    modifier: Modifier,
    currentLocation: LatLng?,
    waypointProgress: List<WaypointProgress>,
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
                controller.setZoom(15.0)
                controller.setCenter(
                    currentLocation
                        ?.let { GeoPoint(it.latitude, it.longitude) }
                        ?: GeoPoint(-1.286389, 36.817223)
                )
            }
        },
        update = { mapView ->
            mapView.overlays.removeIf { it is Marker || it is Polyline }

            currentLocation?.let { loc ->
                Marker(mapView).apply {
                    position = GeoPoint(loc.latitude, loc.longitude)
                    title = "You"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    mapView.overlays.add(this)
                }
                mapView.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
            }

            // Add waypoint markers
            waypointProgress.forEach { wp ->
                Marker(mapView).apply {
                    position = GeoPoint(wp.waypoint.position.latitude, wp.waypoint.position.longitude)
                    title = wp.waypoint.name
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(this)
                }
            }

            // Draw route polyline from waypoints
            if (waypointProgress.size >= 2) {
                Polyline(mapView).apply {
                    setPoints(waypointProgress.map {
                        GeoPoint(it.waypoint.position.latitude, it.waypoint.position.longitude)
                    })
                    mapView.overlays.add(this)
                }
            }

            mapView.invalidate()
        },
    )
}
