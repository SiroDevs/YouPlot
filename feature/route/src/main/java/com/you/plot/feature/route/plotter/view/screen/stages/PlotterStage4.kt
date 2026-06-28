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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.IceSkating
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.ui.components.dialog.PickerDialog
import com.you.plot.feature.route.list.viewmodel.RoutePlotterUiState
import com.you.plot.feature.route.plotter.view.components.ElevationProfileGraph
import com.you.plot.feature.route.plotter.view.components.PlotterMap
import com.you.plot.feature.route.plotter.view.components.StatChip
import com.you.plot.feature.route.plotter.viewmodel.RoutePlotterViewModel

@Composable
fun PlotterStage5(state: RoutePlotterUiState, vm: RoutePlotterViewModel) {
    var showSportDialog by remember { mutableStateOf(false) }

    if (showSportDialog) {
        PickerDialog(
            title = "Sport Type",
            options = SportType.entries.map { it to it.displayName },
            selected = state.sportType,
            onDismiss = { showSportDialog = false },
            onConfirm = { vm.setSportType(it); showSportDialog = false },
        )
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Map preview ──────────────────────────────────────────────────────
        PlotterMap(
            modifier = Modifier.fillMaxWidth().height(220.dp),
            startPoint = state.startPoint,
            endPoint = state.endPoint,
            waypoints = state.activeWaypoints,
            candidates = state.selectedCandidate?.let { listOf(it) } ?: emptyList(),
            selectedCandidateId = state.selectedCandidateId,
            isRoundTrip = state.isRoundTrip,
            startPointName = state.startPointName,
            endPointName = state.endPointName,
            onMapTap = {},
        )

        // ── Stats row ────────────────────────────────────────────────────────
        state.selectedCandidate?.let { c ->
            val dist = if (state.isRoundTrip) c.totalDistanceKm * 2 else c.totalDistanceKm
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatCard("Distance", "%.1f km".format(dist), Modifier.weight(1f))
                StatCard("↑ Gain", "%.0f m".format(c.totalElevationGainMeters), Modifier.weight(1f))
                StatCard("↓ Loss", "%.0f m".format(c.totalElevationLossMeters), Modifier.weight(1f))
            }
        }

        // ── Elevation profile ────────────────────────────────────────────────
        state.selectedCandidate?.let { c ->
            if (c.elevationProfile.isNotEmpty()) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text("Elevation Profile", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    ElevationProfileGraph(
                        profile = c.elevationProfile,
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(12.dp))

        // ── Route name & description ─────────────────────────────────────────
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val autoName = buildAutoName(state)
            OutlinedTextField(
                value = state.name,
                onValueChange = vm::setName,
                label = { Text("Route Name") },
                placeholder = { Text(autoName, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                supportingText = { Text("Leave blank to save as: $autoName") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = vm::setDescription,
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(12.dp))

        // ── Sport type + Route type ──────────────────────────────────────────
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Sport", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SportType.entries.forEach { sport ->
                    FilterChip(
                        selected = state.sportType == sport,
                        onClick = { vm.setSportType(sport) },
                        label = { Text(sport.displayName, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(sport.icon, null, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("Route Type", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !state.isRoundTrip,
                    onClick = { vm.setRoundTrip(false) },
                    label = { Text("One-Way") },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = state.isRoundTrip,
                    onClick = { vm.setRoundTrip(true) },
                    label = { Text("Round Trip") },
                    leadingIcon = { Icon(Icons.Default.Loop, null, modifier = Modifier.size(14.dp)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

private fun buildAutoName(state: RoutePlotterUiState): String =
    when {
        state.startPointName.isNotBlank() && state.endPointName.isNotBlank() ->
            "${state.startPointName} → ${state.endPointName}"
        state.startPointName.isNotBlank() -> "${state.startPointName} Route"
        else -> "New Route"
    }

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        }
    }
}

val SportType.displayName get() = when (this) {
    SportType.RUNNING -> "Run"
    SportType.CYCLING -> "Cycle"
    SportType.HIKING  -> "Hike"
    SportType.WALKING -> "Walk"
}

val SportType.icon: ImageVector get() = when (this) {
    SportType.RUNNING -> Icons.Default.DirectionsRun
    SportType.CYCLING -> Icons.Default.DirectionsBike
    SportType.HIKING  -> Icons.Default.TrendingUp
    SportType.WALKING -> Icons.Default.DirectionsWalk
}
