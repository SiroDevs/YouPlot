package com.you.plot.feature.route.view.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.domain.entity.LatLng
import com.you.plot.core.domain.entity.SportType
import com.you.plot.feature.route.viewmodel.RoutePlotterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlotterScreen(
    viewModel: RoutePlotterViewModel,
    onBack: () -> Unit,
    onRouteSaved: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.savedRouteId) {
        state.savedRouteId?.let { onRouteSaved(it) }
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
                title = { Text("Plot Route") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.setName(it) },
                label = { Text("Route name") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )

            // Sport type chips
            Text("Sport", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SportType.entries.forEach { type ->
                    FilterChip(
                        selected = state.sportType == type,
                        onClick = { viewModel.setSportType(type) },
                        label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.isRoundTrip, onCheckedChange = { viewModel.setRoundTrip(it) })
                Text("Round trip", Modifier.padding(start = 8.dp))
            }

            // Map placeholder: in production, embed OSMDroid/MapView here
            Card(
                Modifier.fillMaxWidth().weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Map View", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Tap to set Start → Waypoints → Finish", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(16.dp))
                        // Demo: simulate setting points
                        Button(onClick = {
                            viewModel.setStartPoint(LatLng(-1.2921, 36.8219))  // Nairobi
                            viewModel.setEndPoint(LatLng(-1.3000, 36.8300))
                        }) { Text("Set demo points (Nairobi)") }
                    }
                }
            }

            Button(
                onClick = { viewModel.saveRoute() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving,
            ) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Save Route")
            }
        }
    }
}
