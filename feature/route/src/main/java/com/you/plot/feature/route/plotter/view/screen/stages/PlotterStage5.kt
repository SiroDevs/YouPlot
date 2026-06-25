package com.you.plot.feature.route.plotter.view.screen.stages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.you.plot.feature.route.list.viewmodel.RoutePlotterUiState
import com.you.plot.feature.route.plotter.view.components.RouteTypeCard
import com.you.plot.feature.route.plotter.viewmodel.RoutePlotterViewModel

@Composable
fun PlotterStage5(state: RoutePlotterUiState, vm: RoutePlotterViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "How would you like to complete this route?",
            style = MaterialTheme.typography.titleMedium,
        )

        RouteTypeCard(
            title = "One-Way",
            description = "Start at the beginning, finish at the destination.",
            selected = !state.isRoundTrip,
            onClick = { vm.setRoundTrip(false) },
        )

        RouteTypeCard(
            title = "Round Trip",
            description = "Return journey follows the same route back to the start.",
            selected = state.isRoundTrip,
            onClick = { vm.setRoundTrip(true) },
        )

        state.selectedCandidate?.let { c ->
            val dist = if (state.isRoundTrip) c.totalDistanceKm * 2 else c.totalDistanceKm
            Text(
                "Total distance: ${"%.1f".format(dist)} km",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.weight(1f))

        Spacer(Modifier.padding(bottom = 56.dp))
    }
}
