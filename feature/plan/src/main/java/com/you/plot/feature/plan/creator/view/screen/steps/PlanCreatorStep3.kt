package com.you.plot.feature.plan.creator.view.screen.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.feature.plan.creator.utils.PlanCreatorUiState
import com.you.plot.feature.plan.creator.view.components.SummaryRow
import com.you.plot.feature.plan.creator.view.screen.dateFmt
import com.you.plot.feature.plan.creator.viewmodel.PlanCreatorViewModel
import java.util.Date

@Composable
fun PlanCreatorStep3(state: PlanCreatorUiState, vm: PlanCreatorViewModel) {
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

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = vm::savePlan,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(if (state.isSaving) "Saving…" else "Save Plan")
        }
    }
}
