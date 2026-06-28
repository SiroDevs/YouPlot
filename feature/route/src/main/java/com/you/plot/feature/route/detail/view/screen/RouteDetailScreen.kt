package com.you.plot.feature.route.detail.view.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.utils.dateFmt
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.feature.route.detail.view.components.RouteDetailRow
import com.you.plot.feature.route.detail.view.components.RouteDetailStatCard
import com.you.plot.feature.route.detail.view.components.RouteDetailWaypointRow
import com.you.plot.feature.route.detail.viewmodel.RouteDetailViewModel
import com.you.plot.feature.route.plotter.view.components.ElevationProfileGraph
import com.you.plot.feature.route.plotter.view.components.PlotterMap
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

    LaunchedEffect(state.isDeleted) { if (state.isDeleted) onBack() }

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
                TextButton(onClick = {
                    showDeleteDialog = false
                }) { Text("Cancel") }
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
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                },
            )
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

        LazyColumn(Modifier
            .fillMaxSize()
            .padding(padding)) {
            item {
                PlotterMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    startPoint = route.startPoint,
                    endPoint = route.endPoint,
                    waypoints = route.waypoints
                        .filter {
                            !it.name.equals("Start", true) && !it.name.equals(
                                "Finish",
                                true
                            )
                        }
                        .map { it.position },
                    candidates = emptyList(),
                    selectedCandidateId = null,
                    isRoundTrip = route.isRoundTrip,
                    startPointName = route.waypoints.minByOrNull { it.orderIndex }?.name ?: "Start",
                    endPointName = route.waypoints.maxByOrNull { it.orderIndex }?.name ?: "Finish",
                    onMapTap = {},
                )
            }

            item {
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RouteDetailStatCard(
                        "Distance",
                        "%.1f km".format(route.totalDistanceKm),
                        Modifier.weight(1f)
                    )
                    RouteDetailStatCard(
                        "↑ Gain",
                        "%.0f m".format(route.totalElevationGainMeters),
                        Modifier.weight(1f)
                    )
                    RouteDetailStatCard(
                        "↓ Loss",
                        "%.0f m".format(route.totalElevationLossMeters),
                        Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            if (route.elevationProfile.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            "Elevation Profile", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        ElevationProfileGraph(
                            profile = route.elevationProfile,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(10.dp)),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            item {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RouteDetailRow(
                            "Sport",
                            route.sportType.name.lowercase().replaceFirstChar { it.uppercase() })
                        RouteDetailRow("Type", if (route.isRoundTrip) "Round Trip" else "One-Way")
                        RouteDetailRow("Created", dateFmt.format(Date(route.createdAt)))
                        if (route.description.isNotBlank()) {
                            HorizontalDivider()
                            Text(
                                route.description, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (route.waypoints.isNotEmpty()) {
                item {
                    Text(
                        "Waypoints", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                }
                items(route.waypoints.sortedBy { it.orderIndex }) { wp ->
                    RouteDetailWaypointRow(wp = wp, total = route.totalDistanceKm)
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            item {
                Button(
                    onClick = { onCreatePlan(route.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Plan this Route")
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
