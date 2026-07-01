package com.you.plot.feature.route.plotter.view.screen.stages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.ElevationPoint
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.RouteCandidate
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.feature.route.plotter.utils.PlotterUiState
import com.you.plot.core.ui.components.maps.ElevationProfileGraph
import com.you.plot.feature.route.plotter.view.components.RouteCandidateCard
import com.you.plot.feature.route.plotter.viewmodel.PlotterViewModel


@Composable
fun PlotterStage4(state: PlotterUiState, vm: PlotterViewModel) {
    PlotterStage4Content(
        state = state,
        onSelectCandidate = vm::selectCandidate,
    )
}

@Composable
private fun PlotterStage4Content(
    state: PlotterUiState,
    onSelectCandidate: (Int) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.weight(1f))

        LazyColumn(
            Modifier
                .fillMaxWidth()
                .height(if (state.selectedCandidate != null) 320.dp else 220.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f))
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "top_spacer") { Spacer(Modifier.height(4.dp)) }

            itemsIndexed(state.routeCandidates, key = { _, c -> "candidate_${c.id}" }) { _, candidate ->
                RouteCandidateCard(
                    candidate = candidate,
                    isSelected = candidate.id == state.selectedCandidateId,
                    onClick = { onSelectCandidate(candidate.id) },
                )
            }

            state.selectedCandidate?.let { c ->
                item(key = "elevation_profile") {
                    Text(
                        "Elevation Profile – Route ${c.id + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                    ElevationProfileGraph(
                        profile = c.elevationProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                    )
                }
            }

            item(key = "bottom_spacer") { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlotterStage4Preview() {
    val sampleProfile = listOf(
        ElevationPoint(0.0, 1700.0),
        ElevationPoint(1.0, 1720.0),
        ElevationPoint(2.0, 1750.0),
        ElevationPoint(3.0, 1735.0),
        ElevationPoint(4.0, 1760.0),
        ElevationPoint(5.0, 1745.0),
    )
    val candidates = listOf(
        RouteCandidate(
            id = 0,
            waypoints = listOf(LatLng(-1.286, 36.817), LatLng(-1.300, 36.830)),
            elevationProfile = sampleProfile,
            totalDist = 5.4,
            elevationGain = 80.0,
            elevationLoss = 55.0,
            colorArgb = 0xFF1976D2L,
        ),
        RouteCandidate(
            id = 1,
            waypoints = listOf(LatLng(-1.286, 36.817), LatLng(-1.305, 36.835)),
            elevationProfile = sampleProfile,
            totalDist = 6.2,
            elevationGain = 110.0,
            elevationLoss = 70.0,
            colorArgb = 0xFFD32F2FL,
        ),
    )
    AppTheme {
        PlotterStage4Content(
            state = PlotterUiState(
                routeCandidates = candidates,
                selectedCandidateId = 0,
            ),
            onSelectCandidate = {},
        )
    }
}
