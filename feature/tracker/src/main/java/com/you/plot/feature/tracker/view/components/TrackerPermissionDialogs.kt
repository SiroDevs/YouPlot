package com.you.plot.feature.tracker.view.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * The two AlertDialogs the tracker shows around Android permissions — the
 * rationale before we request, and the "please turn location on" reminder when
 * services are off.
 */
@Composable
fun TrackerPermissionDialogs(
    showRationale: Boolean,
    locationServicesEnabled: Boolean,
    locationPermissionGranted: Boolean,
    onDismissRationale: () -> Unit,
    fineLocationLauncher: ActivityResultLauncher<String>,
    context: Context,
) {
    if (showRationale) {
        AlertDialog(
            onDismissRequest = onDismissRationale,
            icon = { Icon(Icons.Default.Warning, null) },
            title = { Text("Permissions Required") },
            text = {
                Text(
                    "YouPlot needs Location (precise + background) and Activity Recognition " +
                        "permissions to track your activity. Please grant them and ensure " +
                        "location services are on.",
                )
            },
            confirmButton = {
                Button(onClick = {
                    onDismissRationale()
                    fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) { Text("Grant") }
            },
            dismissButton = {
                TextButton(onClick = onDismissRationale) { Text("Cancel") }
            },
        )
    }

    if (!locationServicesEnabled && locationPermissionGranted) {
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
}
