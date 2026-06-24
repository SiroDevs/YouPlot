package com.you.plot.feature.tracker.view.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.domain.entity.SessionStatus
import com.you.plot.feature.tracker.viewmodel.TrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val session = state.session

    // Waypoint stop dialog
    state.pendingStopWaypoint?.let { wp ->
        AlertDialog(
            onDismissRequest = { viewModel.onStopIgnored() },
            title = { Text("Planned Stop: ${wp.waypoint.name}") },
            text = { Text("You've reached a planned stop. Take a break?") },
            confirmButton = {
                TextButton(onClick = { viewModel.onStopAcknowledged() }) { Text("Acknowledge") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onStopIgnored() }) { Text("Skip") }
            }
        )
    }

    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tracker") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (session == null) {
                // Not started yet
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ready to start?", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.startSession() }, modifier = Modifier.fillMaxWidth(0.6f)) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Start Activity")
                        }
                    }
                }
            } else {
                // Stats cards
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(label = "Distance", value = "%.2f km".format(session.distanceCoveredKm), modifier = Modifier.weight(1f))
                    StatCard(label = "Speed", value = "%.1f km/h".format(session.currentSpeedKmh), modifier = Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val elapsed = session.elapsedTimeSeconds
                    val h = elapsed / 3600; val m = (elapsed % 3600) / 60; val s = elapsed % 60
                    StatCard(label = "Elapsed", value = "%02d:%02d:%02d".format(h, m, s), modifier = Modifier.weight(1f))
                    StatCard(
                        label = "Status",
                        value = session.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Waypoint progress list
                Text("Waypoints", style = MaterialTheme.typography.titleMedium)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                    items(session.waypointProgress, key = { it.waypoint.id }) { progress ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    progress.isReached -> MaterialTheme.colorScheme.primaryContainer
                                    progress.wasSkipped -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Row(
                                Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = if (progress.isReached) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                                    contentDescription = null,
                                )
                                Column {
                                    Text(progress.waypoint.name, style = MaterialTheme.typography.titleSmall)
                                    Text("%.2f km remaining".format(progress.distanceRemainingKm),
                                        style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // Control buttons
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (session.status) {
                        SessionStatus.IN_PROGRESS -> {
                            OutlinedButton(onClick = { viewModel.pauseSession() }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Pause, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Pause")
                            }
                            Button(onClick = { viewModel.completeSession() }, modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                Icon(Icons.Default.Stop, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Finish")
                            }
                        }
                        SessionStatus.PAUSED -> {
                            Button(onClick = { viewModel.resumeSession() }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.PlayArrow, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Resume")
                            }
                            OutlinedButton(onClick = { viewModel.completeSession() }, modifier = Modifier.weight(1f)) {
                                Text("Finish")
                            }
                        }
                        SessionStatus.COMPLETED -> {
                            Card(Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("Activity complete!", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
