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
import androidx.compose.ui.unit.dp
import com.you.plot.feature.route.plotter.utils.PlotterUiState
import com.you.plot.feature.route.plotter.view.components.ElevationProfileGraph
import com.you.plot.feature.route.plotter.view.components.RouteCandidateCard
import com.you.plot.feature.route.plotter.viewmodel.PlotterViewModel

@Composable
fun PlotterStage4(state: PlotterUiState, vm: PlotterViewModel) {
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
            item { Spacer(Modifier.height(4.dp)) }

            itemsIndexed(state.routeCandidates) { _, candidate ->
                RouteCandidateCard(
                    candidate = candidate,
                    isSelected = candidate.id == state.selectedCandidateId,
                    onClick = { vm.selectCandidate(candidate.id) },
                )
            }

            state.selectedCandidate?.let { c ->
                item {
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

            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}
