package com.you.plot.feature.route.plotter.view.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.you.plot.feature.route.list.viewmodel.PlotterStage
import com.you.plot.feature.route.plotter.view.components.StageProgressBar
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
            TopAppBar(
                title = { Text(stageTitles[state.stage] ?: "Plot Route") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.stage == PlotterStage.STAGE_1) onBack()
                        else viewModel.goBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier
            .fillMaxSize()
            .padding(padding)) {
            StageProgressBar(state.stage)
            AnimatedContent(
                targetState = state.stage,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    if (forward)
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    else
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                },
                label = "stage_transition",
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

fun Double.fmt() = "%.5f".format(this)
