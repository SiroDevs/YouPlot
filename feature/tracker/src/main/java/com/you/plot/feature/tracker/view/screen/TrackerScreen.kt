package com.you.plot.feature.tracker.view.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.SessionStatus
import com.you.plot.core.ui.components.action.AppTopBar
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

    if (state.showFullScreenStopReminder && state.pendingStopWaypoint != null) {
        FullScreenStopReminder(
            waypoint = state.pendingStopWaypoint!!,
            onStop = { viewModel.onStopAcknowledged() },
            onIgnore = { viewModel.onStopIgnored() },
        )
        return
    }

    state.error?.let {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(it) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
            },
        )
    }

    if (state.showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPermissionRationale() },
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
                    viewModel.dismissPermissionRationale()
                    fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) { Text("Grant") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPermissionRationale() }) { Text("Cancel") }
            },
        )
    }

    if (!state.locationServicesEnabled && state.locationPermissionGranted) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Location Services Off") },
            text = { Text("Please enable location services to start tracking.") },
            confirmButton = {
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) { Text("Open Settings") }
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
                        onClick = { viewModel.startSession() },
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

                TrackerMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    currentLocation = session.currentLocation,
                    waypoints = session.waypointProgress,
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
                                onClick = { viewModel.pauseSession() },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.Pause, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Pause")
                            }
                            Button(
                                onClick = { viewModel.completeSession() },
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
                                onClick = { viewModel.resumeSession() },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.PlayArrow, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Resume")
                            }
                            OutlinedButton(
                                onClick = { viewModel.completeSession() },
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
