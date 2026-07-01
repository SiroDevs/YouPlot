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
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.utils.AppSpecs
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.core.domain.entity.WaypointSearchResult
import com.you.plot.feature.route.plotter.utils.PlotterUiState
import com.you.plot.core.ui.components.maps.LocationSearchBar
import com.you.plot.feature.route.plotter.view.components.SelectedPointChip
import com.you.plot.core.ui.components.maps.SuggestionRow
import com.you.plot.feature.route.plotter.viewmodel.PlotterViewModel

@Composable
fun PlotterStage2(state: PlotterUiState, vm: PlotterViewModel) {
    PlotterStage2Content(
        state = state,
        onDestinationModeChange = vm::setDestinationMode,
        onQryClear = vm::onQryClear,
        onSearch = vm::onSearch,
        onWaypointSearchResultSelected = vm::onWaypointSearchResultSelected,
        onCountrySelected = vm::setCountryCode,
        onUseMyLocation = vm::onUseMyLocation,
        onTargetDistanceChange = vm::onTargetDistanceChange,
        onSuggestDestinations = vm::suggestDestinationsForDistance,
        onSelectDistanceSuggestion = vm::selectDistanceSuggestion,
    )
}

@Composable
private fun PlotterStage2Content(
    state: PlotterUiState,
    onDestinationModeChange: (DestinationMode) -> Unit,
    onQryClear: () -> Unit,
    onSearch: (String) -> Unit,
    onWaypointSearchResultSelected: (WaypointSearchResult) -> Unit,
    onCountrySelected: (String) -> Unit,
    onUseMyLocation: () -> Unit,
    onTargetDistanceChange: (String) -> Unit,
    onSuggestDestinations: () -> Unit,
    onSelectDistanceSuggestion: (WaypointSearchResult) -> Unit,
) {
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
                    onClick = { onDestinationModeChange(DestinationMode.PICK_POINT) },
                    label = { Text("Pick on Map") },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = state.destinationMode == DestinationMode.TARGET_DISTANCE,
                    onClick = { onDestinationModeChange(DestinationMode.TARGET_DISTANCE) },
                    label = { Text("Target Distance") },
                    modifier = Modifier.weight(1f),
                )
            }

            when (state.destinationMode) {
                DestinationMode.PICK_POINT -> {
                    LocationSearchBar(
                        query = state.searchQuery,
                        onQryChange = onQryClear,
                        onSearch = onSearch,
                        results = state.searchResults,
                        isSearching = state.isSearching,
                        placeholder = "Search destination ...",
                        onResultSelected = onWaypointSearchResultSelected,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        selectedCtryCode = state.selectedCtryCode,
                        onCountrySelected = onCountrySelected,
                        onChooseOnMap = onQryClear,
                        onUseMyLocation = onUseMyLocation,
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
                            value = state.targetDistQry,
                            onValueChange = onTargetDistanceChange,
                            label = { Text("Distance (km)") },
                            shape = AppSpecs.FULL_SHAPE,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        Button(onClick = onSuggestDestinations) { Text("Find") }
                    }

                    if (state.distSuggestions.isNotEmpty()) {
                        Text(
                            "Suggested endpoints:",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                        state.distSuggestions.forEach { suggestion ->
                            SuggestionRow(
                                result = suggestion,
                                isSelected = state.endPoint == suggestion.latLng,
                                onClick = { onSelectDistanceSuggestion(suggestion) },
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

        Spacer(Modifier.height(72.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun PlotterStage2PickPointPreview() {
    AppTheme {
        PlotterStage2Content(
            state = PlotterUiState(
                destinationMode = DestinationMode.PICK_POINT,
                searchQuery = "Karura Forest",
                endPoint = LatLng(-1.245, 36.832),
                endPointName = "Karura Forest",
            ),
            onDestinationModeChange = {},
            onQryClear = {},
            onSearch = {},
            onWaypointSearchResultSelected = {},
            onCountrySelected = {},
            onUseMyLocation = {},
            onTargetDistanceChange = {},
            onSuggestDestinations = {},
            onSelectDistanceSuggestion = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlotterStage2TargetDistancePreview() {
    AppTheme {
        PlotterStage2Content(
            state = PlotterUiState(
                destinationMode = DestinationMode.TARGET_DISTANCE,
                targetDistQry = "8",
                targetDist = 8.0,
                distSuggestions = listOf(
                    WaypointSearchResult("North (≈ 8 km)", LatLng(-1.214, 36.817)),
                    WaypointSearchResult("North-East (≈ 8 km)", LatLng(-1.235, 36.870)),
                ),
            ),
            onDestinationModeChange = {},
            onQryClear = {},
            onSearch = {},
            onWaypointSearchResultSelected = {},
            onCountrySelected = {},
            onUseMyLocation = {},
            onTargetDistanceChange = {},
            onSuggestDestinations = {},
            onSelectDistanceSuggestion = {},
        )
    }
}
