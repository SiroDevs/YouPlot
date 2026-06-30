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
import com.you.plot.core.domain.entity.WaypointSearchResult
import com.you.plot.feature.route.plotter.utils.PlotterUiState
import com.you.plot.feature.route.plotter.view.components.LocationSearchBar
import com.you.plot.feature.route.plotter.view.components.SelectedPointChip
import com.you.plot.feature.route.plotter.viewmodel.PlotterViewModel

@Composable
fun PlotterStage1(state: PlotterUiState, vm: PlotterViewModel) {
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
                onQueryChange = vm::onSearchQueryChange,
                results = state.searchResults,
                isSearching = state.isSearching,
                placeholder = "Search start location…",
                onResultSelected = vm::onWaypointSearchResultSelected,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                selectedCountryCode = state.selectedCountryCode,
                onCountrySelected = vm::setCountryCode,
                onChooseOnMap = { vm.onSearchQueryChange("") },
                onUseMyLocation = vm::onUseMyLocation,
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
fun PlotterStage1Preview() {
    val result =
        WaypointSearchResult(displayName = "Shell GPO", latLng = LatLng(latitude = 0.0, longitude = 0.0))
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
                query = "",
                onQueryChange = {},
                results = listOf(result),
                isSearching = false,
                placeholder = "Search start location…",
                onResultSelected = {},
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                onChooseOnMap = { },
                onUseMyLocation = { },
            )
        }
    }
}
