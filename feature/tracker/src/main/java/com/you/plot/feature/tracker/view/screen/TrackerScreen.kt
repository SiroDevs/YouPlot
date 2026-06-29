package com.you.plot.feature.tracker.view.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.SessionStatus
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.core.domain.entity.ActivitySession
import com.you.plot.core.domain.entity.Waypoint
import com.you.plot.core.domain.entity.WaypointProgress
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.feature.tracker.utils.TrackerUiState
import com.you.plot.feature.tracker.utils.vibrate
import com.you.plot.feature.tracker.view.components.FullScreenStopReminder
import com.you.plot.feature.tracker.view.components.StatCard
import com.you.plot.feature.tracker.view.components.TrackerMap
import com.you.plot.feature.tracker.view.components.WaypointBottomSheet
import com.you.plot.feature.tracker.viewmodel.TrackerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val fineLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onLocationPermissionResult(granted)
    }

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onBackgroundLocationResult(granted)
    }

    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onActivityRecognitionResult(granted)
    }

    LaunchedEffect(Unit) {
        fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            viewModel.onBackgroundLocationResult(true)
            viewModel.onActivityRecognitionResult(true)
        }
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        viewModel.onLocationServicesResult(enabled)
    }

    LaunchedEffect(state.showFullScreenStopReminder) {
        if (state.showFullScreenStopReminder) vibrate(context)
    }

    TrackerScreenContent(
        state = state,
        onBack = onBack,
        onStopAcknowledged = { viewModel.onStopAcknowledged() },
        onStopIgnored = { viewModel.onStopIgnored() },
        onClearError = { viewModel.clearError() },
        onDismissPermissionRationale = { viewModel.dismissPermissionRationale() },
        onRequestLocationPermission = {
            fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        },
        onOpenLocationSettings = {
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        },
        onStartSession = { viewModel.startSession() },
        onPauseSession = { viewModel.pauseSession() },
        onResumeSession = { viewModel.resumeSession() },
        onCompleteSession = { viewModel.completeSession() },
        mapContent = { mapModifier, session ->
            TrackerMap(
                modifier = mapModifier,
                currentLocation = session.currentLocation,
                waypoints = session.waypointProgress,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackerScreenContent(
    state: TrackerUiState,
    onBack: () -> Unit,
    onStopAcknowledged: () -> Unit,
    onStopIgnored: () -> Unit,
    onClearError: () -> Unit,
    onDismissPermissionRationale: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onStartSession: () -> Unit,
    onPauseSession: () -> Unit,
    onResumeSession: () -> Unit,
    onCompleteSession: () -> Unit,
    mapContent: @Composable (Modifier, ActivitySession) -> Unit,
) {
    if (state.showFullScreenStopReminder && state.pendingStopWaypoint != null) {
        FullScreenStopReminder(
            waypoint = state.pendingStopWaypoint!!,
            onStop = onStopAcknowledged,
            onIgnore = onStopIgnored,
        )
        return
    }

    state.error?.let {
        AlertDialog(
            onDismissRequest = onClearError,
            title = { Text("Error") },
            text = { Text(it) },
            confirmButton = {
                TextButton(onClick = onClearError) { Text("OK") }
            },
        )
    }

    if (state.showPermissionRationale) {
        AlertDialog(
            onDismissRequest = onDismissPermissionRationale,
            icon = { Icon(Icons.Default.Warning, null) },
            title = { Text("Permissions Required") },
            text = {
                Text(
                    "YouPlot needs Location (precise + background) and Activity Recognition " +
                        "permissions to track your activity. Please grant them and ensure " +
                        "location services are on."
                )
            },
            confirmButton = {
                Button(onClick = {
                    onDismissPermissionRationale()
                    onRequestLocationPermission()
                }) { Text("Grant") }
            },
            dismissButton = {
                TextButton(onClick = onDismissPermissionRationale) { Text("Cancel") }
            },
        )
    }

    if (!state.locationServicesEnabled && state.locationPermissionGranted) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Location Services Off") },
            text = { Text("Please enable location services to start tracking.") },
            confirmButton = {
                Button(onClick = onOpenLocationSettings) { Text("Open Settings") }
            },
        )
    }

    val sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 160.dp,
        topBar = {
            AppTopBar(
                title = "Activity",
                onNavIconClick = onBack,
            )
        },
        sheetContent = {
            WaypointBottomSheet(
                waypoints = state.session?.waypointProgress ?: emptyList(),
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@BottomSheetScaffold
        }

        val session = state.session

        if (session == null) {
            Box(Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("Ready to start?", style = MaterialTheme.typography.headlineMedium)
                    if (!state.allPermissionsGranted) {
                        Text(
                            "Waiting for permissions…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(
                        onClick = onStartSession,
                        modifier = Modifier.fillMaxWidth(0.65f),
                        enabled = state.allPermissionsGranted,
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start Activity")
                    }
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                mapContent(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    session,
                )

                val elapsed = session.elapsedTimeSeconds
                val h = elapsed / 3600;
                val m = (elapsed % 3600) / 60;
                val s = elapsed % 60
                val nextEta = state.nextUnreachedWaypoint?.estimatedArrivalMillis
                val doneEta = state.estimatedCompletionMillis

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        "Distance",
                        "%.2f km".format(session.distanceCoveredKm),
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "Speed",
                        "%.1f km/h".format(session.currentSpeedKmh),
                        Modifier.weight(1f)
                    )
                    StatCard("Elapsed", "%02d:%02d:%02d".format(h, m, s), Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        "Next WP ETA",
                        nextEta?.let { timeFmt.format(Date(it)) } ?: "—",
                        Modifier.weight(1f),
                    )
                    StatCard(
                        "Done ETA",
                        doneEta?.let { timeFmt.format(Date(it)) } ?: "—",
                        Modifier.weight(1f),
                    )
                    StatCard(
                        "Status",
                        session.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        Modifier.weight(1f),
                    )
                }

                // Controls
                when (session.status) {
                    SessionStatus.IN_PROGRESS -> {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onPauseSession,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.Pause, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Pause")
                            }
                            Button(
                                onClick = onCompleteSession,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                ),
                            ) {
                                Icon(Icons.Default.Stop, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Finish")
                            }
                        }
                    }

                    SessionStatus.PAUSED -> {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onResumeSession,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.PlayArrow, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Resume")
                            }
                            OutlinedButton(
                                onClick = onCompleteSession,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Finish")
                            }
                        }
                    }

                    SessionStatus.COMPLETED -> {
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "Activity complete! 🎉",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                    }

                    else -> {}
                }

                Spacer(Modifier.height(170.dp))
            }
        }
    }
}

private fun previewSampleWaypoint(
    id: Long,
    routeId: Long,
    name: String,
    orderIndex: Int,
    distanceFromStartKm: Double,
    isStop: Boolean = false,
) = Waypoint(
    id = id,
    routeId = routeId,
    name = name,
    position = LatLng(-1.2921 + orderIndex * 0.01, 36.8219 + orderIndex * 0.01),
    orderIndex = orderIndex,
    elevationMeters = 1700.0 + orderIndex * 20,
    distanceFromStartKm = distanceFromStartKm,
    isStopPlanned = isStop,
)

private fun previewSampleSession(): ActivitySession {
    val routeId = 1L
    val now = System.currentTimeMillis()
    val waypoints = listOf(
        WaypointProgress(
            waypoint = previewSampleWaypoint(1, routeId, "Trailhead", 0, 0.0),
            plannedArrivalMillis = now - 60 * 60 * 1000,
            estimatedArrivalMillis = now - 60 * 60 * 1000,
            distanceRemainingKm = 0.0,
            isReached = true,
        ),
        WaypointProgress(
            waypoint = previewSampleWaypoint(2, routeId, "Viewpoint", 1, 2.4, isStop = true),
            plannedArrivalMillis = now + 15 * 60 * 1000,
            estimatedArrivalMillis = now + 18 * 60 * 1000,
            distanceRemainingKm = 1.1,
            isReached = false,
        ),
        WaypointProgress(
            waypoint = previewSampleWaypoint(3, routeId, "Summit", 2, 5.0),
            plannedArrivalMillis = now + 60 * 60 * 1000,
            estimatedArrivalMillis = now + 65 * 60 * 1000,
            distanceRemainingKm = 3.7,
            isReached = false,
        ),
    )
    return ActivitySession(
        id = 1L,
        planId = 1L,
        routeId = routeId,
        status = SessionStatus.IN_PROGRESS,
        startedAtMillis = now - 60 * 60 * 1000,
        elapsedTimeSeconds = 60 * 35 + 12,
        currentLocation = LatLng(-1.2855, 36.8190),
        currentSpeedKmh = 4.7,
        distanceCoveredKm = 1.3,
        waypointProgress = waypoints,
        estimatedCompletionMillis = now + 60 * 60 * 1000,
    )
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun PreviewMapPlaceholder(modifier: Modifier, session: ActivitySession) {
    Box(
        modifier = modifier.background(Color(0xFFDDE3EA)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "Map preview",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF455A64),
        )
    }
}

@Suppress("unused")
@Preview(showBackground = true)
@Composable
private fun TrackerScreenContentReadyPreview() {
    AppTheme {
        TrackerScreenContent(
            state = TrackerUiState(
                session = null,
                isLoading = false,
                locationPermissionGranted = true,
                backgroundLocationGranted = true,
                activityRecognitionGranted = true,
                locationServicesEnabled = true,
            ),
            onBack = {},
            onStopAcknowledged = {},
            onStopIgnored = {},
            onClearError = {},
            onDismissPermissionRationale = {},
            onRequestLocationPermission = {},
            onOpenLocationSettings = {},
            onStartSession = {},
            onPauseSession = {},
            onResumeSession = {},
            onCompleteSession = {},
            mapContent = { modifier, session -> PreviewMapPlaceholder(modifier, session) },
        )
    }
}

@Suppress("unused")
@Preview(showBackground = true)
@Composable
private fun TrackerScreenContentInProgressPreview() {
    AppTheme {
        TrackerScreenContent(
            state = TrackerUiState(
                session = previewSampleSession(),
                isLoading = false,
                locationPermissionGranted = true,
                backgroundLocationGranted = true,
                activityRecognitionGranted = true,
                locationServicesEnabled = true,
            ),
            onBack = {},
            onStopAcknowledged = {},
            onStopIgnored = {},
            onClearError = {},
            onDismissPermissionRationale = {},
            onRequestLocationPermission = {},
            onOpenLocationSettings = {},
            onStartSession = {},
            onPauseSession = {},
            onResumeSession = {},
            onCompleteSession = {},
            mapContent = { modifier, session -> PreviewMapPlaceholder(modifier, session) },
        )
    }
}
