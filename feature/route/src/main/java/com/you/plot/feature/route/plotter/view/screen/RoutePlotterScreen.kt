package com.you.plot.feature.route.plotter.view.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.PlotterStage
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.core.ui.components.action.NextButton
import com.you.plot.core.ui.components.state.StepIndicator
import com.you.plot.feature.route.list.viewmodel.RoutePlotterUiState
import com.you.plot.feature.route.plotter.view.components.PlotterMap
import com.you.plot.feature.route.plotter.view.screen.stages.PlotterStage1
import com.you.plot.feature.route.plotter.view.screen.stages.PlotterStage2
import com.you.plot.feature.route.plotter.view.screen.stages.PlotterStage3
import com.you.plot.feature.route.plotter.view.screen.stages.PlotterStage4
import com.you.plot.feature.route.plotter.view.screen.stages.PlotterStage5
import com.you.plot.feature.route.plotter.view.screen.stages.PlotterStage6
import com.you.plot.feature.route.plotter.viewmodel.RoutePlotterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlotterScreen(
    viewModel: RoutePlotterViewModel,
    onBack: () -> Unit,
    onRouteSaved: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.savedRouteId) {
        state.savedRouteId?.let { onRouteSaved(it) }
    }

    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Notice") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
            },
        )
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
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            StepIndicator(
                current = state.stage.ordinal,
                total = PlotterStage.entries.size,
            )

            Box(Modifier.fillMaxSize()) {
                PlotterMap(
                    modifier = Modifier.fillMaxSize(),
                    startPoint = state.startPoint,
                    endPoint = state.endPoint,
                    waypoints = state.activeWaypoints,
                    candidates = state.routeCandidates,
                    selectedCandidateId = state.selectedCandidateId,
                    onMapTap = if (state.stage.mapsAreTappable) viewModel::onMapTap else { _ -> },
                )

                AnimatedContent(
                    targetState = state.stage,
                    transitionSpec = {
                        val forward = targetState.ordinal > initialState.ordinal
                        if (forward)
                            slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                        else
                            slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    },
                    label = "stage_transition",
                    modifier = Modifier.fillMaxSize(),
                ) { stage ->
                    when (stage) {
                        PlotterStage.STAGE_1 -> PlotterStage1(state, viewModel)
                        PlotterStage.STAGE_2 -> PlotterStage2(state, viewModel)
                        PlotterStage.STAGE_3 -> PlotterStage3(state, viewModel)
                        PlotterStage.STAGE_4 -> PlotterStage4(state, viewModel)
                        PlotterStage.STAGE_5 -> PlotterStage5(state, viewModel)
                        PlotterStage.STAGE_6 -> PlotterStage6(state, viewModel)
                    }
                }

                state.stage.nextButtonConfig(state)?.let { cfg ->
                    NextButton(
                        label = cfg.label,
                        enabled = cfg.enabled,
                        onClick = viewModel::advanceStage,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                    )
                }
            }
        }
    }
}

val stageTitles = mapOf(
    PlotterStage.STAGE_1 to "Select Start Point",
    PlotterStage.STAGE_2 to "Select Destination",
    PlotterStage.STAGE_3 to "Add Waypoints",
    PlotterStage.STAGE_4 to "Compare Routes",
    PlotterStage.STAGE_5 to "Route Type",
    PlotterStage.STAGE_6 to "Save Route",
)

private val PlotterStage.mapsAreTappable: Boolean
    get() = this == PlotterStage.STAGE_1
        || this == PlotterStage.STAGE_2
        || this == PlotterStage.STAGE_3

data class NextButtonConfig(val label: String, val enabled: Boolean)

private fun PlotterStage.nextButtonConfig(state: RoutePlotterUiState): NextButtonConfig? =
    when (this) {
        PlotterStage.STAGE_1 -> NextButtonConfig(
            "Confirm Start → Destination",
            state.startPoint != null
        )

        PlotterStage.STAGE_2 -> NextButtonConfig("Add Waypoints →", state.endPoint != null)
        PlotterStage.STAGE_3 -> NextButtonConfig("Compare Routes →", true)
        PlotterStage.STAGE_4 -> NextButtonConfig(
            "Choose Route Type →",
            state.selectedCandidateId != null
        )

        PlotterStage.STAGE_5 -> NextButtonConfig("Review & Save →", true)
        PlotterStage.STAGE_6 -> null
    }

fun Double.fmt() = "%.5f".format(this)
