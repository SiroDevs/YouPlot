package com.you.plot.feature.route.plotter.view.screen.stages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.feature.route.list.viewmodel.RoutePlotterUiState
import com.you.plot.feature.route.plotter.view.components.LocationSearchBar
import com.you.plot.feature.route.plotter.view.components.SelectedPointChip
import com.you.plot.feature.route.plotter.view.screen.fmt
import com.you.plot.feature.route.plotter.viewmodel.RoutePlotterViewModel

@Composable
fun PlotterStage1(state: RoutePlotterUiState, vm: RoutePlotterViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
    ) {
        LocationSearchBar(
            query = state.searchQuery,
            onQueryChange = vm::onSearchQueryChange,
            results = state.searchResults,
            isSearching = state.isSearching,
            placeholder = "Search start location…",
            onResultSelected = vm::onSearchResultSelected,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            // "Choose on the Map" — just dismisses the keyboard; the map is already tappable
            onChooseOnMap = { vm.onSearchQueryChange("") },
            onUseMyLocation = vm::onUseMyLocation,
        )

        state.startPoint?.let { pt ->
            SelectedPointChip(
                label = "Start: ${pt.latitude.fmt()}, ${pt.longitude.fmt()}",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}