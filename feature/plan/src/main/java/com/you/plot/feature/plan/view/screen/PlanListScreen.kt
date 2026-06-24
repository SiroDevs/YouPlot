package com.you.plot.feature.plan.view.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.feature.plan.viewmodel.PlanListViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanListScreen(
    viewModel: PlanListViewModel,
    onCreatePlan: () -> Unit,
    onPlanClick: (Long) -> Unit,
    onStartTracking: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Plans") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePlan) {
                Icon(Icons.Default.Add, contentDescription = "Create plan")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.plans.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No plans yet", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Create a plan from a route", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(state.plans, key = { it.id }) { plan ->
                    PlanItem(
                        plan = plan,
                        onClick = { onPlanClick(plan.id) },
                        onStartTracking = { onStartTracking(plan.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanItem(plan: ActivityPlan, onClick: () -> Unit, onStartTracking: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onClick,
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(plan.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${plan.numberOfDays} day(s) · %.1f km/day".format(plan.avgDistancePerDayKm),
                    style = MaterialTheme.typography.bodySmall,
                )
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(Date(plan.startDateMillis))
                Text("Starts $dateStr", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onStartTracking) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start tracking")
            }
        }
    }
}
