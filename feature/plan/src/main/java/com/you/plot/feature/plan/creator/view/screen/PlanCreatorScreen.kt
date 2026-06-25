package com.you.plot.feature.plan.creator.view.screen

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
import com.you.plot.core.ui.components.state.StepIndicator
import com.you.plot.feature.plan.creator.view.screen.steps.PlanCreatorStep0
import com.you.plot.feature.plan.creator.view.screen.steps.PlanCreatorStep1
import com.you.plot.feature.plan.creator.view.screen.steps.PlanCreatorStep2
import com.you.plot.feature.plan.creator.view.screen.steps.PlanCreatorStep3
import com.you.plot.feature.plan.creator.viewmodel.PlanCreatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanCreatorScreen(
    viewModel: PlanCreatorViewModel,
    onBack: () -> Unit,
    onPlanSaved: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.savedPlanId) {
        state.savedPlanId?.let { onPlanSaved(it) }
    }

    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Notice") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } },
        )
    }

    val stepTitles = listOf("Choose Source", "Setup", "Schedule", "Save")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Plan — ${stepTitles.getOrElse(state.currentStep) { "" }}") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.currentStep > 0) viewModel.prevStep() else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            StepIndicator(current = state.currentStep, total = stepTitles.size)

            when (state.currentStep) {
                0 -> PlanCreatorStep0(state, viewModel)
                1 -> PlanCreatorStep1(state, viewModel)
                2 -> PlanCreatorStep2(state, viewModel)
                3 -> PlanCreatorStep3(state, viewModel)
            }
        }
    }
}
