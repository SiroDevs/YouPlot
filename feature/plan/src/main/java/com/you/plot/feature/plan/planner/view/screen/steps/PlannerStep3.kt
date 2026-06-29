package com.you.plot.feature.plan.planner.view.screen.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.utils.dateFmt
import com.you.plot.core.ui.components.general.SummaryRow
import com.you.plot.feature.plan.planner.utils.PlannerUiState
import com.you.plot.feature.plan.planner.viewmodel.PlannerViewModel
import java.util.Date

@Composable
fun PlannerStep3(state: PlannerUiState, vm: PlannerViewModel) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Summary", style = MaterialTheme.typography.titleLarge)

        SummaryRow("Plan", state.planName)
        SummaryRow("Route", state.selectedRoute?.name ?: state.selectedTemplate?.name ?: "—")
        SummaryRow("Start", "${dateFmt.format(Date(state.startDateMillis))} at %02d:%02d".format(state.startHour, state.startMinute))
        SummaryRow("Days", state.numberOfDays.toString())
        SummaryRow("Avg speed", "${"%.1f".format(state.avgSpeedKmh)} km/h")
        SummaryRow("Avg distance / day", "${"%.1f".format(state.avgDistancePerDayKm)} km")
        val totalEvents = state.generatedEvents.size + state.customEvents.size
        SummaryRow("Total events", "$totalEvents across ${state.numberOfDays} day(s)")

        // Save Plan is handled by the FAB
        Spacer(Modifier.height(88.dp))
    }
}