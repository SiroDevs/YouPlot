package com.you.plot.feature.route.plotter.view.screen.stages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.ElevationPoint
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.RouteCandidate
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.core.ui.components.maps.PlotterMap
import com.you.plot.feature.route.detail.view.components.RouteInfoPanel
import com.you.plot.feature.route.plotter.utils.PlotterUiState
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

        PlotterStage5Panel(
            state = state,
            distanceKm = dist,
            candidate = candidate,
            onSportTypeChange = vm::setSportType,
            onRoundTripChange = vm::setRoundTrip,
        )
    }
}

@Composable
private fun PlotterStage5Panel(
    state: PlotterUiState,
    distanceKm: Double,
    candidate: RouteCandidate?,
    onSportTypeChange: (SportType) -> Unit,
    onRoundTripChange: (Boolean) -> Unit,
) {
    RouteInfoPanel(
        modifier = Modifier.fillMaxSize(),
        distanceKm = distanceKm,
        elevGainM = candidate?.totalElevationGainMeters ?: 0.0,
        elevLossM = candidate?.totalElevationLossMeters ?: 0.0,
        elevationProfile = candidate?.elevationProfile ?: emptyList(),
        sportType = state.sportType,
        isRoundTrip = state.isRoundTrip,
        waypoints = emptyList(),
        editable = true,
        onSportTypeChange = onSportTypeChange,
        onRoundTripChange = onRoundTripChange,
    )
}

@Preview(showBackground = true)
@Composable
private fun PlotterStage5PanelPreview() {
    val sampleProfile = listOf(
        ElevationPoint(0.0, 1700.0),
        ElevationPoint(1.0, 1720.0),
        ElevationPoint(2.0, 1750.0),
        ElevationPoint(3.0, 1735.0),
        ElevationPoint(4.0, 1760.0),
    )
    val candidate = RouteCandidate(
        id = 0,
        waypoints = listOf(LatLng(-1.286, 36.817), LatLng(-1.300, 36.830)),
        elevationProfile = sampleProfile,
        totalDistanceKm = 8.4,
        totalElevationGainMeters = 120.0,
        totalElevationLossMeters = 95.0,
        colorArgb = 0xFF1976D2L,
    )
    AppTheme {
        PlotterStage5Panel(
            state = PlotterUiState(
                startPoint = LatLng(-1.286, 36.817),
                endPoint = LatLng(-1.300, 36.830),
                startPointName = "Nairobi CBD",
                endPointName = "Lavington",
                sportType = SportType.RUNNING,
                isRoundTrip = false,
                routeCandidates = listOf(candidate),
                selectedCandidateId = 0,
            ),
            distanceKm = candidate.totalDistanceKm,
            candidate = candidate,
            onSportTypeChange = {},
            onRoundTripChange = {},
        )
    }
}
