/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.you.plot.feature.route.detail.view.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.RouteCandidate
import com.you.plot.core.common.utils.MapConstants
import com.you.plot.core.common.utils.dateFmt
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.core.ui.action.AppTopBar
import com.you.plot.core.ui.maps.PlotterMap
import com.you.plot.feature.route.detail.view.components.RouteInfoPanel
import com.you.plot.feature.route.detail.viewmodel.RouteDetailUiState
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailContent(
    state: RouteDetailUiState,
    onBack: () -> Unit,
    onCreatePlan: (Long) -> Unit,
    onDeleteRoute: () -> Unit,
    onExportClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    showMap: Boolean,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Route?") },
            text = { Text("This permanently deletes the route and all associated plans.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDeleteRoute() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = state.route?.name ?: "Route",
                showGoBack = true,
                onNavIconClick = onBack,
                actions = {
                    IconButton(
                        onClick = onEditClick,
                        enabled = state.route != null,
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit route")
                    }
                    // ⋮ More menu
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export Route") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.IosShare, null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    onExportClick()
                                },
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Delete Route",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    showDeleteDialog = true
                                },
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            state.route?.let { route ->
                ExtendedFloatingActionButton(
                    onClick = { onCreatePlan(route.id) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Plan this Route") },
                )
            }
        },
    ) { padding ->

        if (state.isLoading) {
            Box(Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val route = state.route ?: run {
            Box(Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                Text("Route not found")
            }
            return@Scaffold
        }

        Column(Modifier
            .fillMaxSize()
            .padding(padding)) {

            if (showMap) {
                val orderedWps = route.waypoints.sortedBy { it.orderIndex }
                val tailToDrop = when {
                    orderedWps.size < 2 -> 0
                    route.isRoundTrip && orderedWps.size >= 3 -> 2
                    else -> 1
                }
                val intermediates = orderedWps
                    .drop(1)
                    .dropLast(tailToDrop)
                    .map { it.position }
                val endName = when {
                    route.isRoundTrip && orderedWps.size >= 2 ->
                        orderedWps[orderedWps.size - 2].name
                    else -> orderedWps.lastOrNull()?.name ?: "Finish"
                }
                val geometry = route.polyline.ifEmpty { orderedWps.map { it.position } }
                val displayCandidate = RouteCandidate(
                    id = 0,
                    waypoints = geometry,
                    elevationPoints = route.elevationPoints,
                    totalDist = route.totalDist,
                    elevationGain = route.elevationGain,
                    elevationLoss = route.elevationLoss,
                    colorArgb = MapConstants.CANDIDATE_COLORS.first(),
                )
                PlotterMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    startPoint = route.startPoint,
                    endPoint = route.endPoint,
                    waypoints = intermediates,
                    candidates = listOf(displayCandidate),
                    selectedCandidateId = 0,
                    isRoundTrip = route.isRoundTrip,
                    startPointName = orderedWps.firstOrNull()?.name ?: "Start",
                    endPointName = endName,
                    onMapTap = {},
                )
            }

            RouteInfoPanel(
                distanceKm = route.totalDist,
                elevGainM = route.elevationGain,
                elevLossM = route.elevationLoss,
                elevationPoints = route.elevationPoints,
                sportType = route.sportType,
                isRoundTrip = route.isRoundTrip,
                waypoints = route.waypoints,
                createdAt = dateFmt.format(Date(route.createdAt)),
                description = route.description,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RouteDetailScreenLoadedPreview() {
    AppTheme {
        RouteDetailContent(
//            state = RouteDetailUiState(isLoading = false, route = sampleRoute()),
            state = RouteDetailUiState(isLoading = false, route = null),
//            state = RouteDetailUiState(isLoading = true),
            onBack = {},
            onCreatePlan = {},
            onDeleteRoute = {},
            showMap = false,
        )
    }
}
