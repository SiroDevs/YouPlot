package com.you.plot.feature.route.plotter.view.screen.stages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.feature.route.detail.view.components.RouteInfoPanel
import com.you.plot.feature.route.plotter.utils.PlotterUiState
import com.you.plot.feature.route.plotter.view.components.PlotterMap
import com.you.plot.feature.route.plotter.viewmodel.PlotterViewModel

@Composable
fun PlotterStage5(state: PlotterUiState, vm: PlotterViewModel) {
    val candidate = state.selectedCandidate
    val dist = candidate?.let {
        if (state.isRoundTrip) it.totalDistanceKm * 2 else it.totalDistanceKm
    } ?: 0.0

    Column(Modifier.fillMaxSize()) {
        PlotterMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            startPoint = state.startPoint,
            endPoint = state.endPoint,
            waypoints = state.activeWaypoints,
            candidates = candidate?.let { listOf(it) } ?: emptyList(),
            selectedCandidateId = state.selectedCandidateId,
            isRoundTrip = state.isRoundTrip,
            startPointName = state.startPointName,
            endPointName = state.endPointName,
            onMapTap = {},
        )

        HorizontalDivider()

        RouteInfoPanel(
            modifier = Modifier.weight(1f),
            distanceKm = dist,
            elevGainM = candidate?.totalElevationGainMeters ?: 0.0,
            elevLossM = candidate?.totalElevationLossMeters ?: 0.0,
            elevationProfile = candidate?.elevationProfile ?: emptyList(),
            sportType = state.sportType,
            isRoundTrip = state.isRoundTrip,
            waypoints = emptyList(),
            editable = true,
            onSportTypeChange = vm::setSportType,
            onRoundTripChange = vm::setRoundTrip,
        )
    }
}