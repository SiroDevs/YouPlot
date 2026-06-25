package com.you.plot.feature.plan.creator.view.screen.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.you.plot.core.common.utils.dateFmt
import com.you.plot.feature.plan.creator.utils.PlanCreatorUiState
import com.you.plot.feature.plan.creator.viewmodel.PlanCreatorViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanCreatorStep1(state: PlanCreatorUiState, vm: PlanCreatorViewModel) {
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

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {

        OutlinedTextField(value = state.planName, onValueChange = vm::setPlanName,
            label = { Text("Plan Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        OutlinedTextField(value = state.description, onValueChange = vm::setDescription,
            label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

        // Date picker trigger
        OutlinedTextField(
            value = dateFmt.format(Date(state.startDateMillis)),
            onValueChange = {},
            label = { Text("Start Date") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
            enabled = false,
        )

        // Time picker trigger
        OutlinedTextField(
            value = "%02d:%02d".format(state.startHour, state.startMinute),
            onValueChange = {},
            label = { Text("Start Time") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true },
            enabled = false,
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.numberOfDays.toString(),
                onValueChange = { it.toIntOrNull()?.let { v -> vm.setNumberOfDays(v) } },
                label = { Text("Days") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            OutlinedTextField(
                value = state.avgSpeedKmh.toString(),
                onValueChange = { it.toDoubleOrNull()?.let { v -> vm.setAvgSpeed(v) } },
                label = { Text("Avg Speed (km/h)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        }

        // Editable avg distance per day
        OutlinedTextField(
            value = "%.1f".format(state.avgDistancePerDayKm),
            onValueChange = { it.toDoubleOrNull()?.let { v -> vm.setAvgDistancePerDay(v) } },
            label = { Text("Avg Distance / Day (km)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("Auto-calculated from route — tap to override") },
        )

        Spacer(Modifier.height(8.dp))
        Button(onClick = vm::nextStep, modifier = Modifier.fillMaxWidth()) {
            Text("Generate Schedule →")
        }
    }
}
