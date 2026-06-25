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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.you.plot.core.domain.entity.SportType
import com.you.plot.feature.route.list.viewmodel.RoutePlotterUiState
import com.you.plot.feature.route.plotter.view.components.ElevationProfileGraph
import com.you.plot.feature.route.plotter.view.components.PlotterMap
import com.you.plot.feature.route.plotter.view.components.StatChip
import com.you.plot.feature.route.plotter.viewmodel.RoutePlotterViewModel

@Composable
fun PlotterStage6(state: RoutePlotterUiState, vm: RoutePlotterViewModel) {
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Review Route", style = MaterialTheme.typography.titleLarge)

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

        // Stats row
        state.selectedCandidate?.let { c ->
            val dist = if (state.isRoundTrip) c.totalDistanceKm * 2 else c.totalDistanceKm
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatChip(label = "Distance", value = "${"%.1f".format(dist)} km")
                StatChip(label = "↑ Gain", value = "${"%.0f".format(c.totalElevationGainMeters)} m")
                StatChip(label = "↓ Loss", value = "${"%.0f".format(c.totalElevationLossMeters)} m")
            }
        }

        // Elevation profile
        state.selectedCandidate?.let { c ->
            Text("Elevation Profile", style = MaterialTheme.typography.labelMedium)
            ElevationProfileGraph(
                profile = c.elevationProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
        }

        HorizontalDivider()

        // Route type summary
        Text(
            if (state.isRoundTrip) "Round Trip" else "One-Way",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        HorizontalDivider()

        // Name & description fields
        OutlinedTextField(
            value = state.name,
            onValueChange = vm::setName,
            label = { Text("Route Name *") },
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

        // Sport type
        Text("Sport", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SportType.entries.forEach { type ->
                FilterChip(
                    selected = state.sportType == type,
                    onClick = { vm.setSportType(type) },
                    label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                )
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
            Text(if (state.isSaving) "Saving…" else "Save Route")
        }
    }
}