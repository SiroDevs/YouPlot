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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.feature.route.list.viewmodel.RoutePlotterUiState
import com.you.plot.feature.route.plotter.view.screen.fmt
import com.you.plot.feature.route.plotter.viewmodel.RoutePlotterViewModel

@Composable
fun PlotterStage3(state: RoutePlotterUiState, vm: RoutePlotterViewModel) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Use suggested waypoints", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = state.useSuggestedWaypoints,
                onCheckedChange = vm::toggleSuggestedWaypoints,
            )
        }

        if (!state.useSuggestedWaypoints) {
            Text(
                "Tap the map to add waypoints manually",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        } else if (state.suggestedWaypoints.isNotEmpty()) {
            Text(
                "${state.suggestedWaypoints.size} waypoints auto-suggested along route",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // The map fills the space between hints and the waypoint list / button.
        Spacer(Modifier.weight(1f))

        // Manual waypoint list (only when user added some)
        if (!state.useSuggestedWaypoints && state.manualWaypoints.isNotEmpty()) {
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 16.dp),
            ) {
                itemsIndexed(state.manualWaypoints) { index, pt ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "Waypoint ${index + 1}: ${pt.latitude.fmt()}, ${pt.longitude.fmt()}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { vm.removeManualWaypoint(index) }) {
                            Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(72.dp))
    }
}
