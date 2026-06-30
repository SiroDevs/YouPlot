package com.you.plot.feature.route.plotter.view.screen.stages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.feature.route.plotter.utils.PlotterUiState
import com.you.plot.feature.route.plotter.view.components.RouteTypeCard
import com.you.plot.feature.route.plotter.viewmodel.PlotterViewModel


@Composable
fun PlotterStage3(state: PlotterUiState, vm: PlotterViewModel) {
    PlotterStage3Content(
        state = state,
        onRoundTripChange = vm::setRoundTrip,
        onToggleSuggestedWaypoints = vm::toggleSuggestedWaypoints,
        onRemoveManualWaypoint = vm::removeManualWaypoint,
    )
}

@Composable
private fun PlotterStage3Content(
    state: PlotterUiState,
    onRoundTripChange: (Boolean) -> Unit,
    onToggleSuggestedWaypoints: (Boolean) -> Unit,
    onRemoveManualWaypoint: (Int) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0f)),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Route Type",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RouteTypeCard(
                    title = "One-Way",
                    description = "Start → Finish",
                    selected = !state.isRoundTrip,
                    onClick = { onRoundTripChange(false) },
                    modifier = Modifier.weight(1f),
                )
                RouteTypeCard(
                    title = "Round Trip",
                    description = "Return to Start",
                    selected = state.isRoundTrip,
                    onClick = { onRoundTripChange(true) },
                    modifier = Modifier.weight(1f),
                )
            }

            state.selectedCandidate?.let { c ->
                val dist = if (state.isRoundTrip) c.totalDistanceKm * 2 else c.totalDistanceKm
                Text(
                    "Est. distance: ${"%.1f".format(dist)} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        HorizontalDivider()

        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Use suggested waypoints", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = state.useSuggestedWaypoints,
                    onCheckedChange = onToggleSuggestedWaypoints,
                )
            }

            if (!state.useSuggestedWaypoints) {
                Text(
                    "Tap the map to add waypoints manually",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (state.suggestedWaypoints.isNotEmpty()) {
                Text(
                    "${state.suggestedWaypoints.size} waypoints auto-suggested along route",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        if (!state.useSuggestedWaypoints && state.manualWaypoints.isNotEmpty()) {
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                    .padding(horizontal = 16.dp),
            ) {
                itemsIndexed(state.manualWaypoints, key = { index, _ -> "waypoint_$index" }) { index, pt ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "Waypoint ${index + 1}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { onRemoveManualWaypoint(index) }) {
                            Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(72.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun PlotterStage3ManualWaypointsPreview() {
    AppTheme {
        PlotterStage3Content(
            state = PlotterUiState(
                isRoundTrip = false,
                useSuggestedWaypoints = false,
                manualWaypoints = listOf(
                    LatLng(-1.286, 36.817),
                    LatLng(-1.290, 36.820),
                    LatLng(-1.295, 36.825),
                ),
            ),
            onRoundTripChange = {},
            onToggleSuggestedWaypoints = {},
            onRemoveManualWaypoint = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlotterStage3SuggestedWaypointsPreview() {
    AppTheme {
        PlotterStage3Content(
            state = PlotterUiState(
                isRoundTrip = true,
                useSuggestedWaypoints = true,
                suggestedWaypoints = listOf(
                    LatLng(-1.286, 36.817),
                    LatLng(-1.290, 36.820),
                    LatLng(-1.295, 36.825),
                    LatLng(-1.300, 36.830),
                ),
            ),
            onRoundTripChange = {},
            onToggleSuggestedWaypoints = {},
            onRemoveManualWaypoint = {},
        )
    }
}
