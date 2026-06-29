package com.you.plot.feature.route.plotter.view.screen.stages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.DestinationMode
import com.you.plot.core.common.utils.AppSpecs
import com.you.plot.feature.route.plotter.utils.PlotterUiState
import com.you.plot.feature.route.plotter.view.components.LocationSearchBar
import com.you.plot.feature.route.plotter.view.components.SelectedPointChip
import com.you.plot.feature.route.plotter.view.components.SuggestionRow
import com.you.plot.feature.route.plotter.viewmodel.PlotterViewModel

@Composable
fun PlotterStage2(state: PlotterUiState, vm: PlotterViewModel) {
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(8.dp),
                        clip = false
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
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
                        onResultSelected = vm::onWaypointSearchResultSelected,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        selectedCountryCode = state.selectedCountryCode,
                        onCountrySelected = vm::setCountryCode,
                        onChooseOnMap = { vm.onSearchQueryChange("") },
                        onUseMyLocation = vm::onUseMyLocation,
                    )
                    state.endPoint?.let {
                        SelectedPointChip(
                            label = state.endPointName.ifBlank { "Destination set" },
                            isLoading = state.isReverseGeocoding,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }

                DestinationMode.TARGET_DISTANCE -> {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(8.dp),
                                clip = false
                            )
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = state.targetDistanceQuery,
                            onValueChange = vm::onTargetDistanceChange,
                            label = { Text("Distance (km)") },
                            shape = AppSpecs.FULL_SHAPE,
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

                    state.endPoint?.let {
                        SelectedPointChip(
                            label = state.endPointName.ifBlank { "Destination set" },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(72.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun PlotterStage2Preview() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                clip = false
            )
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Set Target Distance") },
            shape = AppSpecs.FULL_SHAPE,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        Button(onClick = {}) { Text("Find") }
    }
}