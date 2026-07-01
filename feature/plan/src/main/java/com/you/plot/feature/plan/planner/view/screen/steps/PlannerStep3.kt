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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.common.utils.dateFmt
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.core.domain.entity.Event
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.ui.general.SummaryRow
import com.you.plot.feature.plan.planner.utils.PlannerUiState
import com.you.plot.feature.plan.planner.viewmodel.PlannerViewModel
import java.util.Date

@Composable
internal fun PlannerStep3(state: PlannerUiState, vm: PlannerViewModel) {
    PlannerStep3Content(state = state)
}

@Composable
private fun PlannerStep3Content(state: PlannerUiState) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Summary", style = MaterialTheme.typography.titleLarge)

        SummaryRow("Plan", state.planName)
        SummaryRow("Route", state.selectedRoute?.name ?: state.selectedTemplate?.name ?: "—")
        SummaryRow("Start", "${dateFmt.format(Date(state.startDate))} at %02d:%02d".format(state.startHour, state.startMinute))
        SummaryRow("Days", state.numberOfDays.toString())
        SummaryRow("Avg speed", "${"%.1f".format(state.avgSpeed)} km/h")
        SummaryRow("Avg distance / day", "${"%.1f".format(state.avgDailyDist)} km")
        val totalEvents = state.generatedEvents.size + state.customEvents.size
        SummaryRow("Total events", "$totalEvents across ${state.numberOfDays} day(s)")

        // Save Plan is handled by the FAB
        Spacer(Modifier.height(88.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun PlannerStep3Preview() {
    val sampleRoute = Route(
        id = 1L,
        name = "Coast Tour",
        sportType = SportType.CYCLING,
        startPoint = LatLng(-1.286, 36.817),
        endPoint = LatLng(-4.04, 39.67),
        totalDist = 480.0,
    )
    val generated = listOf(
        Event(id = -1L, planId = 0L, dayNumber = 1, name = "Start", plannedTime = 0L, orderIndex = 0),
        Event(id = -2L, planId = 0L, dayNumber = 1, name = "Mid", plannedTime = 1L, orderIndex = 1),
        Event(id = -3L, planId = 0L, dayNumber = 1, name = "End", plannedTime = 2L, orderIndex = 2),
    )
    AppTheme {
        PlannerStep3Content(
            state = PlannerUiState(
                selectedRoute = sampleRoute,
                planName = "Coast Tour Plan",
                numberOfDays = 5,
                avgSpeed = 18.0,
                generatedEvents = generated,
                startDate = 0L,
            ),
        )
    }
}
