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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.you.plot.core.common.utils.dateFmt
import com.you.plot.feature.plan.planner.utils.PlannerUiState
import com.you.plot.feature.plan.planner.view.components.PlanDaysStepper
import com.you.plot.feature.plan.planner.view.components.PlanSliderCard
import com.you.plot.feature.plan.planner.viewmodel.PlannerViewModel
import java.util.Date
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerStep1(state: PlannerUiState, vm: PlannerViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.startDateMillis)
    val timePickerState = rememberTimePickerState(initialHour = state.startHour, initialMinute = state.startMinute)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { vm.setStartDate(it) }
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
                            vm.setStartTime(timePickerState.hour, timePickerState.minute)
                            showTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    val totalDistance = state.selectedRoute?.totalDistanceKm
        ?: (state.avgDistancePerDayKm * state.numberOfDays)
    val etaHours = if (state.avgSpeedKmh > 0) totalDistance / state.avgSpeedKmh else 0.0
    val etaDays = state.numberOfDays
    val dailyHours = if (etaDays > 0) etaHours / etaDays else etaHours
    val etaText = buildString {
        append("%.1f km/day".format(state.avgDistancePerDayKm))
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
            value = state.planName, onValueChange = vm::setPlanName,
            label = { Text("Plan Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
        )

        OutlinedTextField(
            value = state.description, onValueChange = vm::setDescription,
            label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2,
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = dateFmt.format(Date(state.startDateMillis)),
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
                onDecrement = { if (state.numberOfDays > 1) vm.setNumberOfDays(state.numberOfDays - 1) },
                onIncrement = { vm.setNumberOfDays(state.numberOfDays + 1) },
                modifier = Modifier.weight(0.8f),
            )
        }

        PlanSliderCard(
            icon = { Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
            label = "Distance per Day",
            valueLabel = "%.1f km".format(state.avgDistancePerDayKm),
            value = state.avgDistancePerDayKm.toFloat(),
            onValueChange = { vm.setAvgDistancePerDay(it.toDouble()) },
            valueRange = 1f..100f,
            steps = 98,
            supportingText = if (state.selectedRoute != null) "Auto from route — drag to override" else null,
        )

        PlanSliderCard(
            icon = { Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
            label = "Average Speed",
            valueLabel = "%.1f km/h".format(state.avgSpeedKmh),
            value = state.avgSpeedKmh.toFloat(),
            onValueChange = { vm.setAvgSpeed(it.toDouble()) },
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
