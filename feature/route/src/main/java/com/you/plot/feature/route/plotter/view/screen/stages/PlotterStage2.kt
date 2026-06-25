package com.you.plot.feature.route.plotter.view.screen.stages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.you.plot.feature.route.list.viewmodel.DestinationMode
import com.you.plot.feature.route.list.viewmodel.RoutePlotterUiState
import com.you.plot.feature.route.plotter.view.components.LocationSearchBar
import com.you.plot.feature.route.plotter.view.components.NextButton
import com.you.plot.feature.route.plotter.view.components.PlotterMap
import com.you.plot.feature.route.plotter.view.components.SelectedPointChip
import com.you.plot.feature.route.plotter.view.components.SuggestionRow
import com.you.plot.feature.route.plotter.view.screen.fmt
import com.you.plot.feature.route.plotter.viewmodel.RoutePlotterViewModel

@Composable
fun PlotterStage2(state: RoutePlotterUiState, vm: RoutePlotterViewModel) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.destinationMode == DestinationMode.PICK_POINT,
                onClick = { vm.setDestinationMode(DestinationMode.PICK_POINT) },
                label = { Text("Pick on Map") },
                modifier = Modifier.weight(1f),
            )
            FilterChip(
                selected = state.destinationMode == DestinationMode.TARGET_DISTANCE,
                onClick = { vm.setDestinationMode(DestinationMode.TARGET_DISTANCE) },
                label = { Text("Target Distance") },
                modifier = Modifier.weight(1f),
            )
        }

        when (state.destinationMode) {
            DestinationMode.PICK_POINT -> {
                LocationSearchBar(
                    query = state.searchQuery,
                    onQueryChange = vm::onSearchQueryChange,
                    results = state.searchResults,
                    isSearching = state.isSearching,
                    placeholder = "Search destination…",
                    onResultSelected = vm::onSearchResultSelected,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                state.endPoint?.let { pt ->
                    SelectedPointChip(
                        label = "Dest: ${pt.latitude.fmt()}, ${pt.longitude.fmt()}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
                PlotterMap(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    startPoint = state.startPoint,
                    endPoint = state.endPoint,
                    waypoints = emptyList(),
                    candidates = emptyList(),
                    selectedCandidateId = null,
                    onMapTap = vm::onMapTap,
                )
            }

            DestinationMode.TARGET_DISTANCE -> {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = state.targetDistanceQuery,
                        onValueChange = vm::onTargetDistanceChange,
                        label = { Text("Distance (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    Button(onClick = vm::suggestDestinationsForDistance) { Text("Find") }
                }

                if (state.distanceSuggestions.isNotEmpty()) {
                    Text(
                        "Suggested endpoints:",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                    state.distanceSuggestions.forEach { suggestion ->
                        SuggestionRow(
                            result = suggestion,
                            isSelected = state.endPoint == suggestion.latLng,
                            onClick = { vm.selectDistanceSuggestion(suggestion) },
                        )
                    }
                }

                state.endPoint?.let { pt ->
                    SelectedPointChip(
                        label = "Dest: ${pt.latitude.fmt()}, ${pt.longitude.fmt()}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }

                Spacer(Modifier.weight(1f))
            }
        }

        NextButton(
            label = "Add Waypoints →",
            enabled = state.endPoint != null,
            onClick = vm::advanceStage,
        )
    }
}