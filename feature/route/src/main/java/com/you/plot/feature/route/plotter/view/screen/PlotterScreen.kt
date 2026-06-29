package com.you.plot.feature.route.plotter.view.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.PlotterStage
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.feature.route.plotter.view.components.PlotterMap
import com.you.plot.feature.route.plotter.view.screen.stages.PlotterStage1
import com.you.plot.feature.route.plotter.view.screen.stages.PlotterStage2
import com.you.plot.feature.route.plotter.view.screen.stages.PlotterStage3
import com.you.plot.feature.route.plotter.view.screen.stages.PlotterStage4
import com.you.plot.feature.route.plotter.view.screen.stages.PlotterStage5
import com.you.plot.feature.route.plotter.viewmodel.PlotterViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import com.you.plot.feature.route.plotter.utils.PlotterUiState
import com.you.plot.feature.route.plotter.view.components.SaveRouteDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlotterScreen(
    viewModel: PlotterViewModel,
    onBack: () -> Unit,
    onRouteSaved: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }

    // System permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )
    }

    // Triggered when user taps "Use your location" without permission
    LaunchedEffect(state.needsLocationPermission) {
        if (state.needsLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
            viewModel.clearNeedsPermission()
        }
    }

    LaunchedEffect(state.savedRouteId) {
        state.savedRouteId?.let { onRouteSaved(it) }
    }

    // Don't leave the save dialog open underneath an error alert
    LaunchedEffect(state.error) {
        if (state.error != null) showSaveDialog = false
    }

    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Notice") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } },
        )
    }

    if (showSaveDialog) {
        SaveRouteDialog(
            state = state,
            vm = viewModel,
            onDismiss = { showSaveDialog = false },
            onConfirm = { viewModel.advanceStage() },
        )
    }

    val pagerState = rememberPagerState(pageCount = { PlotterStage.entries.size })

    LaunchedEffect(state.stage) {
        if (pagerState.currentPage != state.stage.ordinal) {
            pagerState.animateScrollToPage(state.stage.ordinal)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val target = PlotterStage.entries[pagerState.currentPage]
        when {
            target.ordinal > state.stage.ordinal -> {
                val canAdvance = state.stage.nextButtonConfig(state)?.enabled == true
                if (canAdvance) viewModel.advanceStage()
                else pagerState.animateScrollToPage(state.stage.ordinal)
            }
            target.ordinal < state.stage.ordinal -> viewModel.goBack()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stageTitles[state.stage] ?: "Plot Route",
                showGoBack = true,
                onNavIconClick = {
                    if (state.stage == PlotterStage.STAGE_1) onBack()
                    else viewModel.goBack()
                },
                stepCurrent = state.stage.ordinal,
                stepTotal = PlotterStage.entries.size,
            )
        },
        floatingActionButton = {
            state.stage.nextButtonConfig(state)?.let { cfg ->
                ExtendedFloatingActionButton(
                    onClick = {
                        if (state.stage == PlotterStage.STAGE_5) showSaveDialog = true
                        else viewModel.advanceStage()
                    },
                    icon = {
                        if (state.isSaving)
                            CircularProgressIndicator(
                                modifier = Modifier.padding(2.dp),
                                strokeWidth = 2.dp
                            )
                        else
                            Icon(
                                if (state.stage == PlotterStage.STAGE_5) Icons.Default.Check
                                else Icons.Default.ArrowForward,
                                contentDescription = null,
                            )
                    },
                    text = { Text(cfg.label) },
                    expanded = cfg.enabled,
                )
            }
        },
    ) { padding ->
        Box(Modifier
            .fillMaxSize()
            .padding(padding)) {
            if (state.stage != PlotterStage.STAGE_5)
                PlotterMap(
                    modifier = Modifier.fillMaxSize(),
                    startPoint = state.startPoint,
                    endPoint = state.endPoint,
                    waypoints = state.activeWaypoints,
                    candidates = state.routeCandidates,
                    selectedCandidateId = state.selectedCandidateId,
                    isRoundTrip = state.isRoundTrip,
                    startPointName = state.startPointName,
                    endPointName = state.endPointName,
                    onMapTap = if (state.stage.mapsAreTappable) viewModel::onMapTap else { _ -> },
                    onWaypointMoved = if (state.stage == PlotterStage.STAGE_3) viewModel::onWaypointMoved else null,
                    onWaypointDelete = if (state.stage == PlotterStage.STAGE_3) viewModel::removeManualWaypoint else null,
                )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (PlotterStage.entries[page]) {
                    PlotterStage.STAGE_1 -> PlotterStage1(state, viewModel)
                    PlotterStage.STAGE_2 -> PlotterStage2(state, viewModel)
                    PlotterStage.STAGE_3 -> PlotterStage3(state, viewModel)
                    PlotterStage.STAGE_4 -> PlotterStage4(state, viewModel)
                    PlotterStage.STAGE_5 -> PlotterStage5(state, viewModel)
                }
            }
        }
    }
}

val stageTitles = mapOf(
    PlotterStage.STAGE_1 to "Select Start Point",
    PlotterStage.STAGE_2 to "Select Destination",
    PlotterStage.STAGE_3 to "Waypoints & Route Type",
    PlotterStage.STAGE_4 to "Compare Routes",
    PlotterStage.STAGE_5 to "Save Route",
)

private val PlotterStage.mapsAreTappable: Boolean
    get() = this == PlotterStage.STAGE_1 || this == PlotterStage.STAGE_2 || this == PlotterStage.STAGE_3

data class NextButtonConfig(val label: String, val enabled: Boolean)

private fun PlotterStage.nextButtonConfig(state: PlotterUiState): NextButtonConfig? =
    when (this) {
        PlotterStage.STAGE_1 -> NextButtonConfig("Set Destination", state.startPoint != null)
        PlotterStage.STAGE_2 -> NextButtonConfig("Add Waypoints", state.endPoint != null)
        PlotterStage.STAGE_3 -> NextButtonConfig("Compare Routes", true)
        PlotterStage.STAGE_4 -> NextButtonConfig(
            "Review & Save",
            state.selectedCandidateId != null
        )

        PlotterStage.STAGE_5 -> NextButtonConfig(
            if (state.isSaving) "Saving ..." else "Save Route",
            !state.isSaving
        )
    }
