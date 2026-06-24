package com.you.plot.feature.plan.view.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.feature.plan.viewmodel.PlanCreatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanCreatorScreen(
    viewModel: PlanCreatorViewModel,
    onBack: () -> Unit,
    onPlanSaved: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.savedPlanId) {
        state.savedPlanId?.let { onPlanSaved(it) }
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
                title = { Text("Create Plan — Step ${state.currentStep + 1}") },
                navigationIcon = {
                    IconButton(onClick = { if (state.currentStep > 0) viewModel.prevStep() else onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (state.currentStep) {
                0 -> {
                    Text("Select a Route", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.routes, key = { it.id }) { route ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { viewModel.selectRoute(route) },
                                colors = if (state.selectedRoute?.id == route.id)
                                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                else CardDefaults.cardColors()
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(route.name, style = MaterialTheme.typography.titleMedium)
                                    Text("%.1f km".format(route.totalDistanceKm), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                1 -> {
                    Text("Setup Plan", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = state.planName, onValueChange = { viewModel.setPlanName(it) },
                        label = { Text("Plan name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = state.description, onValueChange = { viewModel.setDescription(it) },
                        label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = state.numberOfDays.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> viewModel.setNumberOfDays(v) } },
                        label = { Text("Number of days") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = state.avgSpeedKmh.toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.setAvgSpeed(v) } },
                        label = { Text("Avg speed (km/h)") }, modifier = Modifier.fillMaxWidth())
                }
                2 -> {
                    Text("Review Events", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("${state.generatedEvents.size} events generated across ${state.numberOfDays} days",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(state.generatedEvents.take(10)) { event ->
                            Text("Day ${event.dayNumber}: ${event.name} (${String.format("%.1f", event.distanceCoveredKm)} km)",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                3 -> {
                    Text("Save Plan", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    Text("Route: ${state.selectedRoute?.name}", style = MaterialTheme.typography.bodyLarge)
                    Text("Plan: ${state.planName}", style = MaterialTheme.typography.bodyLarge)
                    Text("Days: ${state.numberOfDays}", style = MaterialTheme.typography.bodyLarge)
                    Text("Speed: ${state.avgSpeedKmh} km/h", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { viewModel.savePlan() }, modifier = Modifier.fillMaxWidth(), enabled = !state.isSaving) {
                        if (state.isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Save Plan")
                    }
                }
            }
            if (state.currentStep < 3) {
                Spacer(Modifier.weight(1f))
                Button(onClick = { viewModel.nextStep() }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.currentStep == 2) "Review" else "Next")
                }
            }
        }
    }
}
