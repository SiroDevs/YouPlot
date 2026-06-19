package com.sirofits.youplot.plan.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirofits.youplot.common.util.DistanceUtils
import com.sirofits.youplot.common.util.TimeUtils
import com.sirofits.youplot.domain.entity.ActivityPlan
import com.sirofits.youplot.plan.viewmodel.PlanListViewModel
import com.sirofits.youplot.ui.components.*

@Composable
fun PlanListScreen(
    onCreatePlan: () -> Unit,
    onPlanClick: (Long) -> Unit,
    onStartTracking: (Long) -> Unit,
    viewModel: PlanListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { YouPlotTopBar(title = "Activity Plans") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreatePlan,
                icon = { Icon(Icons.Default.Add, "New plan") },
                text = { Text("New Plan") },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Column(
                Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) { repeat(3) { ShimmerCard() } }
        } else if (state.plans.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    emoji = "📋",
                    title = "No plans yet",
                    subtitle = "Create a plan from one of your saved routes",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.plans, key = { it.id }) { plan ->
                    PlanCard(
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
private fun PlanCard(
    plan: ActivityPlan,
    onClick: () -> Unit,
    onStartTracking: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(plan.name, style = MaterialTheme.typography.titleMedium)
                    if (plan.description.isNotBlank()) {
                        Text(
                            plan.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                FilledTonalIconButton(onClick = onStartTracking) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start tracking")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PlanStat(label = "Start", value = TimeUtils.formatDate(plan.startDateMillis))
                PlanStat(label = "Days", value = "${plan.numberOfDays}")
                PlanStat(label = "Avg speed", value = "${plan.avgSpeedKmh.toInt()} km/h")
                PlanStat(label = "Events", value = "${plan.events.size}")
            }
        }
    }
}

@Composable
private fun PlanStat(label: String, value: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleSmall)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
