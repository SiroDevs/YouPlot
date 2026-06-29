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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.utils.dateFmt
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.feature.route.detail.view.components.RouteInfoPanel
import com.you.plot.feature.route.detail.viewmodel.RouteDetailViewModel
import com.you.plot.core.ui.components.maps.PlotterMap
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailScreen(
    viewModel: RouteDetailViewModel,
    onBack: () -> Unit,
    onCreatePlan: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) { if (state.isDeleted) onBack() }

    // ── Delete confirmation ───────────────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Route?") },
            text = { Text("This permanently deletes the route and all associated plans.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; viewModel.deleteRoute() }) {
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
                    // Edit button
                    IconButton(onClick = { /* TODO: navigate to edit screen */ }) {
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
                                    // WIP — no action yet
                                },
                                enabled = false,   // clearly WIP
                                trailingIcon = {
                                    Text(
                                        "Soon",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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

            PlotterMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                startPoint = route.startPoint,
                endPoint = route.endPoint,
                waypoints = emptyList(),     // shown via routePolyline instead
                candidates = emptyList(),
                selectedCandidateId = null,
                isRoundTrip = route.isRoundTrip,
                startPointName = route.waypoints.minByOrNull { it.orderIndex }?.name ?: "Start",
                endPointName = route.waypoints.maxByOrNull { it.orderIndex }?.name ?: "Finish",
                routePolyline = route.waypoints.sortedBy { it.orderIndex }.map { it.position },
                onMapTap = {},
            )

            RouteInfoPanel(
                distanceKm = route.totalDistanceKm,
                elevGainM = route.totalElevationGainMeters,
                elevLossM = route.totalElevationLossMeters,
                elevationProfile = route.elevationProfile,
                sportType = route.sportType,
                isRoundTrip = route.isRoundTrip,
                waypoints = route.waypoints,
                createdAt = dateFmt.format(Date(route.createdAt)),
                description = route.description,
                editable = false,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
