package com.you.plot.feature.plan.planner.view.screen.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.common.utils.dateFmt
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.core.domain.entity.Route
import com.you.plot.feature.plan.planner.utils.PlannerUiState
import com.you.plot.feature.plan.planner.view.components.PlanDaysStepper
import com.you.plot.feature.plan.planner.view.components.PlanSliderCard
import com.you.plot.feature.plan.planner.viewmodel.PlannerViewModel
import java.util.Date
import kotlin.math.roundToInt

@Composable
internal fun PlannerStep1(state: PlannerUiState, vm: PlannerViewModel) {
    PlannerStep1Content(
        state = state,
        onPlanNameChange = vm::setPlanName,
        onDescriptionChange = vm::setDescription,
        onStartDateChange = vm::setStartDate,
        onStartTimeChange = vm::setStartTime,
        onNumberOfDaysChange = vm::setNumberOfDays,
        onAvgDistancePerDayChange = vm::setAvgDistancePerDay,
        onAvgSpeedChange = vm::setAvgSpeed,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlannerStep1Content(
    state: PlannerUiState,
    onPlanNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onStartTimeChange: (Int, Int) -> Unit,
    onNumberOfDaysChange: (Int) -> Unit,
    onAvgDistancePerDayChange: (Double) -> Unit,
    onAvgSpeedChange: (Double) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.startDate)
    val timePickerState = rememberTimePickerState(initialHour = state.startHour, initialMinute = state.startMinute)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onStartDateChange(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Start Time", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    TimePicker(state = timePickerState)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            onStartTimeChange(timePickerState.hour, timePickerState.minute)
                            showTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    val totalDistance = state.selectedRoute?.totalDist
        ?: (state.avgDailyDist * state.numberOfDays)
    val etaHours = if (state.avgSpeed > 0) totalDistance / state.avgSpeed else 0.0
    val etaDays = state.numberOfDays
    val dailyHours = if (etaDays > 0) etaHours / etaDays else etaHours
    val etaText = buildString {
        append("%.1f km/day".format(state.avgDailyDist))
        append(" · ")
        val h = dailyHours.toInt()
        val m = ((dailyHours - h) * 60).roundToInt()
        if (h > 0) append("${h}h ")
        append("${m}m/day")
        append(" · ${etaDays}d total")
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        OutlinedTextField(
            value = state.planName, onValueChange = onPlanNameChange,
            label = { Text("Plan Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
        )

        OutlinedTextField(
            value = state.description, onValueChange = onDescriptionChange,
            label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2,
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = dateFmt.format(Date(state.startDate)),
                onValueChange = {},
                label = { Text("Start Date") },
                readOnly = true,
                modifier = Modifier
                    .weight(1f)
                    .clickable { showDatePicker = true },
                enabled = false,
            )

            OutlinedTextField(
                value = "%02d:%02d".format(state.startHour, state.startMinute),
                onValueChange = {},
                label = { Text("Time") },
                readOnly = true,
                modifier = Modifier
                    .weight(0.6f)
                    .clickable { showTimePicker = true },
                enabled = false,
            )

            PlanDaysStepper(
                days = state.numberOfDays,
                onDecrement = { if (state.numberOfDays > 1) onNumberOfDaysChange(state.numberOfDays - 1) },
                onIncrement = { onNumberOfDaysChange(state.numberOfDays + 1) },
                modifier = Modifier.weight(0.8f),
            )
        }

        PlanSliderCard(
            icon = { Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
            label = "Distance per Day",
            valueLabel = "%.1f km".format(state.avgDailyDist),
            value = state.avgDailyDist.toFloat(),
            onValueChange = { onAvgDistancePerDayChange(it.toDouble()) },
            valueRange = 1f..100f,
            steps = 98,
            supportingText = if (state.selectedRoute != null) "Auto from route — drag to override" else null,
        )

        PlanSliderCard(
            icon = { Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
            label = "Average Speed",
            valueLabel = "%.1f km/h".format(state.avgSpeed),
            value = state.avgSpeed.toFloat(),
            onValueChange = { onAvgSpeedChange(it.toDouble()) },
            valueRange = 1f..40f,
            steps = 38,
        )

        // ETA summary card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Estimated Plan",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
                Text(
                    etaText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    "Total: %.1f km over $etaDays day${if (etaDays > 1) "s" else ""}".format(totalDistance),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
        }

        Spacer(Modifier.height(88.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun PlannerStep1Preview() {
    val sampleRoute = Route(
        id = 1L,
        name = "Coast Tour",
        sportType = SportType.CYCLING,
        startPoint = LatLng(-1.286, 36.817),
        endPoint = LatLng(-4.04, 39.67),
        totalDist = 480.0,
    )
    AppTheme {
        PlannerStep1Content(
            state = PlannerUiState(
                selectedRoute = sampleRoute,
                planName = "Coast Tour Plan",
                description = "Five days along the Indian Ocean coast.",
                numberOfDays = 5,
                avgSpeed = 18.0,
            ),
            onPlanNameChange = {},
            onDescriptionChange = {},
            onStartDateChange = {},
            onStartTimeChange = { _, _ -> },
            onNumberOfDaysChange = {},
            onAvgDistancePerDayChange = {},
            onAvgSpeedChange = {},
        )
    }
}
