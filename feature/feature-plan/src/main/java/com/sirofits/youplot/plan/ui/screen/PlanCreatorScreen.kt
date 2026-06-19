package com.sirofits.youplot.plan.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirofits.youplot.common.util.TimeUtils
import com.sirofits.youplot.domain.entity.ActivityPlan
import com.sirofits.youplot.domain.entity.PlanEvent
import com.sirofits.youplot.domain.entity.Route
import com.sirofits.youplot.plan.viewmodel.PlanCreatorViewModel
import com.sirofits.youplot.ui.components.*

@Composable
fun PlanCreatorScreen(
    onBack: () -> Unit,
    onPlanSaved: (Long) -> Unit,
    viewModel: PlanCreatorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(state.savedPlanId) {
        state.savedPlanId?.let { onPlanSaved(it) }
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHost.showSnackbar(it); viewModel.clearError() }
    }

    val stepTitles = listOf("Choose Route", "Setup", "Schedule", "Review")

    Scaffold(
        topBar = {
            YouPlotTopBar(
                title = stepTitles.getOrElse(state.currentStep) { "Plan" },
                onBack = if (state.currentStep == 0) onBack else viewModel::prevStep,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar = {
            PlanCreatorBottomBar(
                currentStep = state.currentStep,
                totalSteps = stepTitles.size,
                isSaving = state.isSaving,
                onNext = viewModel::nextStep,
                onSave = viewModel::savePlan,
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Step indicator
            StepIndicator(current = state.currentStep, total = stepTitles.size)

            when (state.currentStep) {
                0 -> RoutePickerStep(
                    routes = state.routes,
                    selectedRoute = state.selectedRoute,
                    onRouteSelected = viewModel::selectRoute,
                )
                1 -> PlanSetupStep(
                    state = state,
                    onNameChange = viewModel::setPlanName,
                    onDescChange = viewModel::setDescription,
                    onDaysChange = viewModel::setNumberOfDays,
                    onSpeedChange = viewModel::setAvgSpeed,
                )
                2 -> EventsStep(
                    generatedEvents = state.generatedEvents,
                    customEvents = state.customEvents,
                    numberOfDays = state.numberOfDays,
                    onRemoveCustomEvent = viewModel::removeCustomEvent,
                )
                3 -> ReviewStep(
                    state = com.sirofits.youplot.plan.viewmodel.PlanCreatorUiState(
                        selectedRoute = state.selectedRoute,
                        planName = state.planName,
                        description = state.description,
                        startDateMillis = state.startDateMillis,
                        numberOfDays = state.numberOfDays,
                        avgSpeedKmh = state.avgSpeedKmh,
                        generatedEvents = state.generatedEvents,
                        customEvents = state.customEvents,
                    )
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(current: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(total) { step ->
            val isActive = step == current
            val isDone = step < current
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(2.dp),
                    color = when {
                        isDone -> MaterialTheme.colorScheme.primary
                        isActive -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    },
                ) {}
            }
        }
    }
}

@Composable
private fun RoutePickerStep(
    routes: List<Route>,
    selectedRoute: Route?,
    onRouteSelected: (Route) -> Unit,
) {
    if (routes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                emoji = "🗺️",
                title = "No routes yet",
                subtitle = "Plot a route first before creating a plan",
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(routes, key = { it.id }) { route ->
            val isSelected = route.id == selectedRoute?.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRouteSelected(route) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface,
                ),
                border = if (isSelected) ButtonDefaults.outlinedButtonBorder else null,
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(route.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${route.sportType.emoji()} · %.1f km".format(route.totalDistanceKm),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanSetupStep(
    state: com.sirofits.youplot.plan.viewmodel.PlanCreatorUiState,
    onNameChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onDaysChange: (Int) -> Unit,
    onSpeedChange: (Double) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = state.planName,
            onValueChange = onNameChange,
            label = { Text("Plan name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.description,
            onValueChange = onDescChange,
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
        )

        HorizontalDivider()

        Text("Duration", style = MaterialTheme.typography.titleSmall)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilledTonalIconButton(
                onClick = { onDaysChange(state.numberOfDays - 1) },
                enabled = state.numberOfDays > 1,
            ) { Text("−") }
            Text(
                "${state.numberOfDays} ${if (state.numberOfDays == 1) "day" else "days"}",
                style = MaterialTheme.typography.headlineMedium,
            )
            FilledTonalIconButton(onClick = { onDaysChange(state.numberOfDays + 1) }) { Text("+") }
        }

        HorizontalDivider()

        Text("Avg speed (km/h)", style = MaterialTheme.typography.titleSmall)
        Slider(
            value = state.avgSpeedKmh.toFloat(),
            onValueChange = { onSpeedChange(it.toDouble()) },
            valueRange = 3f..40f,
            steps = 37,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "%.0f km/h · ~%.1f km/day".format(
                state.avgSpeedKmh,
                state.selectedRoute?.totalDistanceKm?.div(state.numberOfDays) ?: 0.0
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        state.selectedRoute?.let { route ->
            HorizontalDivider()
            SummaryCard(
                days = state.numberOfDays,
                totalKm = route.totalDistanceKm,
                speedKmh = state.avgSpeedKmh,
            )
        }
    }
}

@Composable
private fun SummaryCard(days: Int, totalKm: Double, speedKmh: Double) {
    val perDay = totalKm / days
    val hoursPerDay = perDay / speedKmh

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Plan summary", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatChip("Per day", "%.0f km".format(perDay), Modifier.weight(1f))
                StatChip("Daily hours", "%.1f h".format(hoursPerDay), Modifier.weight(1f))
                StatChip("Total", "%.0f km".format(totalKm), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun EventsStep(
    generatedEvents: List<PlanEvent>,
    customEvents: List<PlanEvent>,
    numberOfDays: Int,
    onRemoveCustomEvent: (Int) -> Unit,
) {
    val allEvents = (generatedEvents + customEvents)
        .sortedWith(compareBy({ it.dayNumber }, { it.plannedTimeMillis }))

    if (allEvents.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Day tab selector
    var selectedDay by remember { mutableStateOf(1) }

    Column(Modifier.fillMaxSize()) {
        if (numberOfDays > 1) {
            ScrollableTabRow(
                selectedTabIndex = selectedDay - 1,
                modifier = Modifier.fillMaxWidth(),
            ) {
                (1..numberOfDays).forEach { day ->
                    Tab(
                        selected = selectedDay == day,
                        onClick = { selectedDay = day },
                        text = { Text("Day $day") },
                    )
                }
            }
        }

        val dayEvents = allEvents.filter { it.dayNumber == selectedDay }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(dayEvents) { _, event ->
                EventRow(event = event)
            }
        }
    }
}

@Composable
private fun EventRow(event: PlanEvent) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Time column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(52.dp),
        ) {
            Text(
                TimeUtils.formatTime(event.plannedTimeMillis),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // Timeline dot + line
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = RoundedCornerShape(50),
                color = if (event.waypointId != null)
                    MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(10.dp),
            ) {}
        }

        // Event content
        Column(Modifier.weight(1f)) {
            Text(event.name, style = MaterialTheme.typography.bodyMedium)
            if (event.durationMinutes > 0) {
                Text(
                    "Stop: ${TimeUtils.formatDuration(event.durationMinutes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (event.distanceCoveredKm > 0) {
                Text(
                    "@ %.1f km".format(event.distanceCoveredKm),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ReviewStep(state: com.sirofits.youplot.plan.viewmodel.PlanCreatorUiState) {
    val allEvents = (state.generatedEvents + state.customEvents)
        .sortedWith(compareBy({ it.dayNumber }, { it.plannedTimeMillis }))

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(state.planName, style = MaterialTheme.typography.headlineMedium)
                    if (state.description.isNotBlank()) {
                        Text(state.description, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    Text("Route: ${state.selectedRoute?.name ?: "—"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Start: ${TimeUtils.formatDateTime(state.startDateMillis)}", style = MaterialTheme.typography.bodyMedium)
                    Text("${state.numberOfDays} ${if (state.numberOfDays == 1) "day" else "days"} · ${state.avgSpeedKmh.toInt()} km/h avg",
                        style = MaterialTheme.typography.bodyMedium)
                    Text("${allEvents.size} scheduled events", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (allEvents.isNotEmpty()) {
            item { Text("Events", style = MaterialTheme.typography.titleSmall) }
            items(allEvents.take(10)) { event ->
                EventRow(event = event)
            }
            if (allEvents.size > 10) {
                item {
                    Text(
                        "+ ${allEvents.size - 10} more events",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanCreatorBottomBar(
    currentStep: Int,
    totalSteps: Int,
    isSaving: Boolean,
    onNext: () -> Unit,
    onSave: () -> Unit,
) {
    val isLastStep = currentStep == totalSteps - 1
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            if (isLastStep) {
                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier.height(48.dp),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Save Plan")
                }
            } else {
                Button(
                    onClick = onNext,
                    modifier = Modifier.height(48.dp),
                ) {
                    Text("Next")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
