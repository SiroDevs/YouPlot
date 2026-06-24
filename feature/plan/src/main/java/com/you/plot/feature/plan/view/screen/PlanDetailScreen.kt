package com.you.plot.feature.plan.view.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.feature.plan.viewmodel.PlanDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    viewModel: PlanDetailViewModel,
    onBack: () -> Unit,
    onStartTracking: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.plan?.name ?: "Plan") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    state.plan?.let { plan ->
                        IconButton(onClick = { onStartTracking(plan.id) }) {
                            Icon(Icons.Default.PlayArrow, "Start tracking")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val plan = state.plan ?: return@Scaffold
            LazyColumn(Modifier.padding(padding).padding(horizontal = 16.dp)) {
                item {
                    Text("${plan.numberOfDays} day(s) · %.1f km/day · %.0f km/h".format(plan.avgDistancePerDayKm, plan.avgSpeedKmh),
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    if (plan.description.isNotBlank()) {
                        Text(plan.description, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                    }
                    // Day tabs
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..plan.numberOfDays).forEach { day ->
                            FilterChip(
                                selected = state.selectedDay == day,
                                onClick = { viewModel.selectDay(day) },
                                label = { Text("Day $day") }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                val dayEvents = plan.events.filter { it.dayNumber == state.selectedDay }
                items(dayEvents, key = { it.id }) { event ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(event.name, style = MaterialTheme.typography.titleSmall)
                            Text("%.1f km".format(event.distanceCoveredKm), style = MaterialTheme.typography.bodySmall)
                            if (event.durationMinutes > 0)
                                Text("Stop: ${event.durationMinutes} min", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
