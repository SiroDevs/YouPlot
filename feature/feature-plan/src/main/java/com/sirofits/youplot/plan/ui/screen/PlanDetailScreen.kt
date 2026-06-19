package com.sirofits.youplot.plan.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import com.sirofits.youplot.domain.entity.PlanEvent
import com.sirofits.youplot.plan.viewmodel.PlanDetailViewModel
import com.sirofits.youplot.ui.components.YouPlotTopBar

@Composable
fun PlanDetailScreen(
    onBack: () -> Unit,
    onStartTracking: (Long) -> Unit,
    viewModel: PlanDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            YouPlotTopBar(
                title = state.plan?.name ?: "Plan",
                onBack = onBack,
                actions = {
                    state.plan?.let { plan ->
                        IconButton(onClick = { onStartTracking(plan.id) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start tracking")
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.plan == null -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Plan not found", style = MaterialTheme.typography.bodyLarge)
                }
            }
            else -> {
                PlanDetailContent(
                    plan = state.plan!!,
                    selectedDay = state.selectedDay,
                    onDaySelected = viewModel::selectDay,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun PlanDetailContent(
    plan: ActivityPlan,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        // Plan header stats
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                HeaderStat("Start", TimeUtils.formatDate(plan.startDateMillis))
                HeaderStat("Days", "${plan.numberOfDays}")
                HeaderStat("Speed", "${plan.avgSpeedKmh.toInt()} km/h")
                HeaderStat("Events", "${plan.events.size}")
            }
        }

        // Day tabs (only if multi-day)
        if (plan.numberOfDays > 1) {
            ScrollableTabRow(selectedTabIndex = selectedDay - 1) {
                (1..plan.numberOfDays).forEach { day ->
                    Tab(
                        selected = selectedDay == day,
                        onClick = { onDaySelected(day) },
                        text = { Text("Day $day") },
                    )
                }
            }
        }

        val dayEvents = plan.events
            .filter { it.dayNumber == selectedDay }
            .sortedBy { it.plannedTimeMillis }

        val totalDayDistance = dayEvents.lastOrNull()?.distanceCoveredKm ?: 0.0
        val remainingDistance = (plan.events.lastOrNull()?.distanceCoveredKm ?: 0.0) - totalDayDistance

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                Text(
                    "Day $selectedDay Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }

            items(dayEvents, key = { it.id }) { event ->
                TimelineEventRow(event = event, isLast = event == dayEvents.last())
            }

            if (dayEvents.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    DaySummaryCard(
                        distanceForDay = totalDayDistance,
                        remainingDistance = remainingDistance,
                        numberOfDays = plan.numberOfDays,
                        currentDay = selectedDay,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineEventRow(event: PlanEvent, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Time
        Text(
            TimeUtils.formatTime(event.plannedTimeMillis),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .width(44.dp)
                .padding(top = 4.dp),
        )

        // Timeline line + dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(20.dp),
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (event.waypointId != null)
                    MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp),
            ) {}
            if (!isLast) {
                Spacer(
                    Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .padding(top = 2.dp)
                )
                // Can't draw a line easily in Compose without Canvas, use a thin Box
                Box(
                    Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .padding(vertical = 2.dp)
                ) {
                    Surface(
                        Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    ) {}
                }
            }
        }

        // Content
        Column(
            Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 24.dp),
        ) {
            Text(event.name, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (event.durationMinutes > 0) {
                    Text(
                        "Stop ${TimeUtils.formatDuration(event.durationMinutes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                if (event.distanceCoveredKm > 0) {
                    Text(
                        DistanceUtils.formatKm(event.distanceCoveredKm),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DaySummaryCard(
    distanceForDay: Double,
    remainingDistance: Double,
    numberOfDays: Int,
    currentDay: Int,
) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            HeaderStat("Day distance", DistanceUtils.formatKm(distanceForDay))
            if (currentDay < numberOfDays) {
                HeaderStat("Remaining", DistanceUtils.formatKm(remainingDistance))
                HeaderStat("Days left", "${numberOfDays - currentDay}")
            }
        }
    }
}

@Composable
private fun HeaderStat(label: String, value: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleSmall)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
