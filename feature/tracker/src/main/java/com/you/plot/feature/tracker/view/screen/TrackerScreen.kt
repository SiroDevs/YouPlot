package com.you.plot.feature.tracker.view.screen

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.feature.tracker.utils.vibrate
import com.you.plot.feature.tracker.view.components.FullScreenStopReminder
import com.you.plot.feature.tracker.view.components.TrackerActivePane
import com.you.plot.feature.tracker.view.components.TrackerPermissionDialogs
import com.you.plot.feature.tracker.view.components.TrackerReadyPane
import com.you.plot.feature.tracker.view.components.WaypointBottomSheet
import com.you.plot.feature.tracker.viewmodel.TrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val fineLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> viewModel.onLocationPermissionResult(granted) }

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> viewModel.onBackgroundLocationResult(granted) }

    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> viewModel.onActivityRecognitionResult(granted) }

    LaunchedEffect(Unit) {
        fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            viewModel.onBackgroundLocationResult(true)
            viewModel.onActivityRecognitionResult(true)
        }
        viewModel.onLocationServicesResult(context.isLocationOn())
    }

    LaunchedEffect(state.showFullScreenStopReminder) {
        if (state.showFullScreenStopReminder) vibrate(context)
    }

    if (state.showFullScreenStopReminder && state.pendingStopWaypoint != null) {
        FullScreenStopReminder(
            waypoint = state.pendingStopWaypoint!!,
            onStop = viewModel::onStopAcknowledged,
            onIgnore = viewModel::onStopIgnored,
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

    TrackerPermissionDialogs(
        showRationale = state.showPermissionRationale,
        locationServicesEnabled = state.locationServicesEnabled,
        locationPermissionGranted = state.locationPermissionGranted,
        onDismissRationale = viewModel::dismissPermissionRationale,
        fineLocationLauncher = fineLocationLauncher,
        context = context,
    )

    val sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 160.dp,
        topBar = { AppTopBar(title = "Activity", onNavIconClick = onBack) },
        sheetContent = {
            WaypointBottomSheet(
                waypoints = state.activity?.waypointProgress ?: emptyList(),
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

        val activity = state.activity
        if (activity == null) {
            TrackerReadyPane(
                allPermissionsGranted = state.allPermissionsGranted,
                onStart = viewModel::startActivity,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            TrackerActivePane(
                activity = activity,
                nextEta = state.nextUnreachedWaypoint?.estimatedArrival,
                doneEta = state.estimatedCompletion,
                onPause = viewModel::pauseActivity,
                onResume = viewModel::resumeActivity,
                onFinish = viewModel::completeActivity,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

private fun Context.isLocationOn(): Boolean {
    val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}
