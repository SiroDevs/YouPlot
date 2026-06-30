package com.you.plot.feature.route.plotter.view.screen.stages

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.ui.components.dialog.PickerDialog
import com.you.plot.feature.route.plotter.utils.PlotterUiState
import com.you.plot.feature.route.plotter.view.components.ElevationProfileGraph
import com.you.plot.feature.route.plotter.view.components.PlotterMap
import com.you.plot.feature.route.plotter.view.components.StatChip
import com.you.plot.feature.route.plotter.viewmodel.PlotterViewModel

@Composable
fun PlotterStage5(state: PlotterUiState, vm: PlotterViewModel) {
    val scrollState = rememberScrollState()
    var showSportTypeDialog by remember { mutableStateOf(false) }

    if (showSportTypeDialog) {
        PickerDialog(
            title = "Change Sport Type",
            options = SportType.entries.map {
                it to it.name.lowercase().replaceFirstChar { c -> c.uppercase() }
            },
            selected = state.sportType,
            onDismiss = { showSportTypeDialog = false },
            onConfirm = { selectedType ->
                vm.setSportType(selectedType)
                showSportTypeDialog = false
            },
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PlotterMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp)),
            startPoint = state.startPoint,
            endPoint = state.endPoint,
            waypoints = state.activeWaypoints,
            candidates = state.selectedCandidate?.let { listOf(it) } ?: emptyList(),
            selectedCandidateId = state.selectedCandidateId,
            onMapTap = {},
        )

        state.selectedCandidate?.let { c ->
            val dist = if (state.isRoundTrip) c.totalDistanceKm * 2 else c.totalDistanceKm
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatChip(label = "Distance", value = "${"%.1f".format(dist)} km")
                StatChip(
                    label = "↑ Elev. Gain",
                    value = "${"%.0f".format(c.totalElevationGainMeters)} m"
                )
                StatChip(
                    label = "↓ Elev. Loss",
                    value = "${"%.0f".format(c.totalElevationLossMeters)} m"
                )
            }
        }

        state.selectedCandidate?.let { c ->
            Text("Elevation Profile", style = MaterialTheme.typography.labelMedium)
            ElevationProfileGraph(
                profile = c.elevationProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
            )
        }

        val autoName = if (state.startPointName.isNotBlank() && state.endPointName.isNotBlank()) {
            "${state.startPointName} → ${state.endPointName}"
        } else if (state.startPointName.isNotBlank()) {
            "${state.startPointName} Route"
        } else {
            "New Route"
        }

        OutlinedTextField(
            value = state.name,
            onValueChange = vm::setName,
            label = { Text("Route Name") },
            placeholder = { Text(autoName, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
            supportingText = { Text("Leave blank to use: $autoName") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = vm::setDescription,
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Route Type: ${if (state.isRoundTrip) "Round Trip" else "One-Way"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            VerticalDivider()

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Sport Type:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { showSportTypeDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(state.sportType.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = vm::advanceStage,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(if (state.isSaving) "Saving ..." else "Save Route")
        }
    }
}
