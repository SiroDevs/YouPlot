package com.you.plot.feature.plan.planner.view.screen.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.core.domain.entity.Event
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.ui.components.general.DayTimeline
import com.you.plot.feature.plan.planner.utils.PlannerUiState
import com.you.plot.feature.plan.planner.view.components.AddEventDialog
import com.you.plot.feature.plan.planner.view.components.DaySummaryCard
import com.you.plot.feature.plan.planner.view.components.EventRow
import com.you.plot.feature.plan.planner.viewmodel.PlannerViewModel

@Composable
internal fun PlannerStep2(state: PlannerUiState, vm: PlannerViewModel) {
    PlannerStep2Content(
        state = state,
        onDaySelected = vm::selectDay,
        onAddCustomEvent = { name, hour, minute, duration ->
            vm.addCustomEvent(name, hour, minute, duration, state.selectedDay)
        },
        onRemoveCustomEvent = vm::removeCustomEvent,
        onRemoveGeneratedEvent = vm::removeGeneratedEvent,
    )
}

@Composable
private fun PlannerStep2Content(
    state: PlannerUiState,
    onDaySelected: (Int) -> Unit,
    onAddCustomEvent: (name: String, hour: Int, minute: Int, duration: Int) -> Unit,
    onRemoveCustomEvent: (Long) -> Unit,
    onRemoveGeneratedEvent: (Long) -> Unit,
) {
    var showAddEventDialog by remember { mutableStateOf(false) }

    if (showAddEventDialog) {
        AddEventDialog(
            day = state.selectedDay,
            onDismiss = { showAddEventDialog = false },
            onConfirm = { name, hour, minute, duration ->
                onAddCustomEvent(name, hour, minute, duration)
                showAddEventDialog = false
            },
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddEventDialog = true },
                expanded = true,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Event") },
            )
        },
        floatingActionButtonPosition = FabPosition.Start
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(state.numberOfDays, key = { i -> "day_tab_$i" }) { i ->
                    val day = i + 1
                    FilterChip(
                        selected = state.selectedDay == day,
                        onClick = { onDaySelected(day) },
                        label = { Text("Day $day") },
                    )
                }
            }

            DayTimeline(
                events = state.eventsForSelectedDay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .padding(horizontal = 12.dp),
            )

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            LazyColumn(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item(key = "top_spacer") { Spacer(Modifier.height(4.dp)) }

                items(
                    state.eventsForSelectedDay,
                    key = { "${it.id}-${it.orderIndex}-${it.plannedTime}" },
                ) { event ->
                    val isCustom = state.customEvents.any { it.id == event.id }
                    EventRow(
                        event = event,
                        isCustom = isCustom,
                        onRemove = {
                            if (isCustom) onRemoveCustomEvent(event.id)
                            else onRemoveGeneratedEvent(event.id)
                        },
                    )
                }

                item(key = "bottom_spacer") { Spacer(Modifier.height(4.dp)) }

                item(key = "day_summary") {
                    DaySummaryCard(
                        dayTotalDist = state.dayTotalDist,
                        remainingDist = state.remainingDist,
                        adjustedDailyDist = state.adjustedRemainingDailyDist,
                        daysLeft = state.numberOfDays - state.selectedDay,
                    )
                }

                item(key = "fab_spacer") { Spacer(Modifier.height(80.dp)) }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlannerStep2Preview() {
    val sampleRoute = Route(
        id = 1L,
        name = "Coast Tour",
        sportType = SportType.CYCLING,
        startPoint = LatLng(-1.286, 36.817),
        endPoint = LatLng(-4.04, 39.67),
        totalDist = 480.0,
    )
    
    val dayStart = 0L
    val generated = listOf(
        Event(
            id = -1L, planId = 0L, dayNumber = 1, name = "Start",
            plannedTime = dayStart + 6L * 3_600_000L,
            duration = 0, distCovered = 0.0, orderIndex = 0,
        ),
        Event(
            id = -2L, planId = 0L, dayNumber = 1, name = "Checkpoint",
            plannedTime = dayStart + 9L * 3_600_000L,
            duration = 15, distCovered = 45.0, orderIndex = 1,
        ),
        Event(
            id = -3L, planId = 0L, dayNumber = 1, name = "End of Day",
            plannedTime = dayStart + 14L * 3_600_000L,
            duration = 0, distCovered = 96.0, orderIndex = 2,
        ),
    )
    AppTheme {
        PlannerStep2Content(
            state = PlannerUiState(
                selectedRoute = sampleRoute,
                numberOfDays = 5,
                avgSpeed = 18.0,
                selectedDay = 1,
                generatedEvents = generated,
                startDate = 0L,
            ),
            onDaySelected = {},
            onAddCustomEvent = { _, _, _, _ -> },
            onRemoveCustomEvent = {},
            onRemoveGeneratedEvent = {},
        )
    }
}
