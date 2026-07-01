package com.you.plot.feature.route.plotter.view.screen.stages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.core.domain.entity.StartPoint
import com.you.plot.core.domain.entity.WaypointSearchResult
import com.you.plot.feature.route.plotter.utils.PlotterUiState
import com.you.plot.core.ui.maps.LocationSearchBar
import com.you.plot.feature.route.plotter.view.components.SelectedPointChip
import com.you.plot.feature.route.plotter.viewmodel.PlotterViewModel

@Composable
fun PlotterStage1(state: PlotterUiState, vm: PlotterViewModel) {
    PlotterStage1Content(
        state = state,
        onQryClear = vm::onQryClear,
        onSearch = vm::onSearch,
        onWaypointSearchResultSelected = vm::onWaypointSearchResultSelected,
        onCountrySelected = vm::setCountryCode,
        onUseMyLocation = vm::onUseMyLocation,
        onSavedStartPointPicked = vm::onSavedStartPointPicked,
    )
}

@Composable
private fun PlotterStage1Content(
    state: PlotterUiState,
    onQryClear: () -> Unit,
    onSearch: (String) -> Unit,
    onWaypointSearchResultSelected: (WaypointSearchResult) -> Unit,
    onCountrySelected: (String) -> Unit,
    onUseMyLocation: () -> Unit,
    onSavedStartPointPicked: (StartPoint) -> Unit = {},
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        ) {
            LocationSearchBar(
                query = state.searchQuery,
                onQryChange = { onQryClear() },
                onSearch = onSearch,
                results = state.searchResults,
                isSearching = state.isSearching,
                placeholder = "Search start location ...",
                onResultSelected = onWaypointSearchResultSelected,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                selectedCtryCode = state.selectedCtryCode,
                onCountrySelected = onCountrySelected,
                onChooseOnMap = { onQryClear() },
                onUseMyLocation = onUseMyLocation,
                savedStartPoints = state.savedStartPoints,
                onStartPointPicked = onSavedStartPointPicked,
            )

            state.startPoint?.let {
                SelectedPointChip(
                    label = state.startPointName.ifBlank { "Start point set" },
                    isLoading = state.isReverseGeocoding,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlotterStage1Preview() {
    AppTheme {
        PlotterStage1Content(
            state = PlotterUiState(
                searchQuery = "Nairobi CBD",
                startPoint = LatLng(-1.286, 36.817),
                startPointName = "Nairobi CBD",
            ),
            onQryClear = {},
            onSearch = {},
            onWaypointSearchResultSelected = {},
            onCountrySelected = {},
            onUseMyLocation = {},
        )
    }
}
