package com.you.plot.feature.plan.planner.view.screen.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.ui.components.general.DayTimeline
import com.you.plot.feature.plan.planner.utils.PlannerUiState
import com.you.plot.feature.plan.planner.view.components.AddEventDialog
import com.you.plot.feature.plan.planner.view.components.DaySummaryCard
import com.you.plot.feature.plan.planner.view.components.EventRow
import com.you.plot.feature.plan.planner.viewmodel.PlannerViewModel

@Composable
fun PlannerStep2(state: PlannerUiState, vm: PlannerViewModel) {
    var showAddEventDialog by remember { mutableStateOf(false) }

    if (showAddEventDialog) {
        AddEventDialog(
            day = state.selectedDay,
            onDismiss = { showAddEventDialog = false },
            onConfirm = { name, hour, minute, duration ->
                vm.addCustomEvent(name, hour, minute, duration, state.selectedDay)
                showAddEventDialog = false
            },
        )
    }

    Column(Modifier.fillMaxSize()) {

        // Day tab row
        LazyRow(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(state.numberOfDays) { i ->
                val day = i + 1
                FilterChip(
                    selected = state.selectedDay == day,
                    onClick = { vm.selectDay(day) },
                    label = { Text("Day $day") },
                )
            }
        }

        // Horizontal timeline
        DayTimeline(
            events = state.eventsForSelectedDay,
            modifier = Modifier.fillMaxWidth().height(90.dp).padding(horizontal = 12.dp),
        )

        HorizontalDivider(Modifier.padding(vertical = 4.dp))

        // Event list for the day
        LazyColumn(
            Modifier.weight(1f).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            items(state.eventsForSelectedDay, key = { it.id }) { event ->
                val isCustom = state.customEvents.any { it.id == event.id }
                EventRow(
                    event = event,
                    isCustom = isCustom,
                    onRemove = {
                        if (isCustom) vm.removeCustomEvent(event.id)
                        else vm.removeGeneratedEvent(event.id)
                    },
                )
            }

            item { Spacer(Modifier.height(4.dp)) }

            // End-of-day summary
            item {
                DaySummaryCard(
                    dayTotalKm = state.dayTotalDistanceKm,
                    remainingKm = state.remainingDistanceKm,
                    adjustedDailyKm = state.adjustedRemainingDailyDistanceKm,
                    daysLeft = state.numberOfDays - state.selectedDay,
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        // Floating add event button area
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { showAddEventDialog = true },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Event")
            }
            Button(
                onClick = vm::nextStep,
                modifier = Modifier.weight(1f),
            ) { Text("Review →") }
        }
    }
}
