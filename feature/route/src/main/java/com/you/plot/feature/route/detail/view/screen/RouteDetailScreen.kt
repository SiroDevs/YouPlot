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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.ElevationPoint
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.RouteCandidate
import com.you.plot.core.common.entity.SportType
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.you.plot.core.common.utils.MapConstants
import com.you.plot.core.common.utils.dateFmt
import com.you.plot.core.data.export.RouteExportFormat
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.entity.Waypoint
import com.you.plot.core.ui.action.AppTopBar
import com.you.plot.core.ui.dialog.ExportChoice
import com.you.plot.core.ui.dialog.ExportFormatSheet
import com.you.plot.feature.route.detail.view.components.RouteInfoPanel
import com.you.plot.feature.route.detail.viewmodel.RouteDetailUiState
import com.you.plot.feature.route.detail.viewmodel.RouteDetailViewModel
import com.you.plot.core.ui.maps.PlotterMap
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailScreen(
    viewModel: RouteDetailViewModel,
    onBack: () -> Unit,
    onCreatePlan: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showExportSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) { if (state.isDeleted) onBack() }

    // Fire the share intent as soon as the exporter finishes writing the file.
    LaunchedEffect(state.pendingShareUri) {
        val uri = state.pendingShareUri ?: return@LaunchedEffect
        val mime = state.pendingShareMime ?: "*/*"
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Export route"))
        viewModel.consumeShare()
    }

    state.exportError?.let { err ->
        AlertDialog(
            onDismissRequest = { viewModel.clearExportError() },
            title = { Text("Export failed") },
            text = { Text(err) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearExportError() }) { Text("OK") }
            },
        )
    }

    if (showExportSheet) {
        ExportFormatSheet(
            title = "Export route as",
            formats = RouteExportFormat.entries.map { format ->
                val stubbed = format == RouteExportFormat.FIT || format == RouteExportFormat.IMAGE
                ExportChoice(
                    label = format.display,
                    description = when (format) {
                        RouteExportFormat.GPX -> "GPX 1.1 track + routepoints"
                        RouteExportFormat.TCX -> "Garmin Training Center course"
                        RouteExportFormat.PDF -> "Printable text summary"
                        RouteExportFormat.FIT -> "Binary FIT — coming soon"
                        RouteExportFormat.IMAGE -> "Map screenshot — coming soon"
                    },
                    enabled = !stubbed,
                    onSelect = {
                        showExportSheet = false
                        viewModel.exportRoute(format)
                    },
                )
            },
            onDismiss = { showExportSheet = false },
        )
    }

    state.deleteError?.let { err ->
        AlertDialog(
            onDismissRequest = { viewModel.clearDeleteError() },
            title = { Text("Cannot delete route") },
            text = { Text(err) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearDeleteError() }) { Text("OK") }
            },
        )
    }

    RouteDetailContent(
        state = state,
        onBack = onBack,
        onCreatePlan = onCreatePlan,
        onDeleteRoute = viewModel::deleteRoute,
        showMap = true,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteDetailContent(
    state: RouteDetailUiState,
    onBack: () -> Unit,
    onCreatePlan: (Long) -> Unit,
    onDeleteRoute: () -> Unit,
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
                                    showExportSheet = true
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
                val intermediates = orderedWps
                    .drop(1)
                    .dropLast(if (orderedWps.size > 1) 1 else 0)
                    .map { it.position }
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
                    endPointName = orderedWps.lastOrNull()?.name ?: "Finish",
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

private fun sampleRoute(): Route {
    val createdAt = 1_700_000_000_000L
    val profile = listOf(
        ElevationPoint(0.0, 1700.0),
        ElevationPoint(1.0, 1720.0),
        ElevationPoint(2.0, 1755.0),
        ElevationPoint(3.0, 1742.0),
        ElevationPoint(4.0, 1768.0),
        ElevationPoint(5.0, 1750.0),
    )
    val waypoints = listOf(
        Waypoint(
            id = 1, routeId = 1L, name = "Nairobi CBD",
            position = LatLng(-1.286, 36.817), orderIndex = 0,
            elevation = 1700.0, distFromStart = 0.0,
        ),
        Waypoint(
            id = 2, routeId = 1L, name = "Uhuru Park",
            position = LatLng(-1.291, 36.819), orderIndex = 1,
            elevation = 1720.0, distFromStart = 2.1,
            isStopPlanned = true,
        ),
        Waypoint(
            id = 3, routeId = 1L, name = "Lavington",
            position = LatLng(-1.300, 36.830), orderIndex = 2,
            elevation = 1750.0, distFromStart = 8.4,
        ),
    )
    return Route(
        id = 1L,
        name = "Morning Loop",
        description = "Easy paced run through the city.",
        sportType = SportType.RUNNING,
        startPoint = LatLng(-1.286, 36.817),
        endPoint = LatLng(-1.300, 36.830),
        waypoints = waypoints,
        elevationPoints = profile,
        totalDist = 8.4,
        elevationGain = 120.0,
        elevationLoss = 95.0,
        isRoundTrip = false,
        createdAt = createdAt,
    )
}

@Preview(showBackground = true)
@Composable
private fun RouteDetailScreenLoadingPreview() {
    AppTheme {
        RouteDetailContent(
            state = RouteDetailUiState(isLoading = true),
            onBack = {},
            onCreatePlan = {},
            onDeleteRoute = {},
            showMap = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RouteDetailScreenNotFoundPreview() {
    AppTheme {
        RouteDetailContent(
            state = RouteDetailUiState(isLoading = false, route = null),
            onBack = {},
            onCreatePlan = {},
            onDeleteRoute = {},
            showMap = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RouteDetailScreenLoadedPreview() {
    AppTheme {
        RouteDetailContent(
            state = RouteDetailUiState(isLoading = false, route = sampleRoute()),
            onBack = {},
            onCreatePlan = {},
            onDeleteRoute = {},
            showMap = false,
        )
    }
}
