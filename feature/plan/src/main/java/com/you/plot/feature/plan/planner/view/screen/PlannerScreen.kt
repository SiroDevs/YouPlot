package com.you.plot.feature.plan.planner.view.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.feature.plan.planner.view.screen.steps.PlannerStep0
import com.you.plot.feature.plan.planner.view.screen.steps.PlannerStep1
import com.you.plot.feature.plan.planner.view.screen.steps.PlannerStep2
import com.you.plot.feature.plan.planner.view.screen.steps.PlannerStep3
import com.you.plot.feature.plan.planner.viewmodel.PlannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel,
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

    val stepTitles = if (state.selectedRoute != null && state.currentStep >= 1)
        listOf("Setup", "Schedule", "Save")
    else
        listOf("Choose Source", "Setup", "Schedule", "Save")

    val displayStep = if (state.selectedRoute != null && state.currentStep >= 1)
        state.currentStep - 1 else state.currentStep

    // Per-step FAB configuration
    data class FabConfig(
        val label: String,
        val isLoading: Boolean = false,
        val enabled: Boolean = true,
        val onClick: () -> Unit,
    )

    val fabConfig: FabConfig = when (state.currentStep) {
        0 -> FabConfig(
            label = "Next: Setup",
            enabled = state.selectedRoute != null || state.selectedTemplate != null,
            onClick = viewModel::nextStep,
        )
        1 -> FabConfig(
            label = if (state.isGenerating) "Generating…" else "Generate Schedule",
            isLoading = state.isGenerating,
            onClick = { if (!state.isGenerating) viewModel.nextStep() },
        )
        2 -> FabConfig(
            label = "Review",
            onClick = viewModel::nextStep,
        )
        3 -> FabConfig(
            label = if (state.isSaving) "Saving…" else "Save Plan",
            isLoading = state.isSaving,
            onClick = { if (!state.isSaving) viewModel.savePlan() },
        )
        else -> FabConfig(label = "", onClick = {})
    }

    // Icon changes meaningfully per step
    val fabIcon: @Composable () -> Unit = {
        when {
            fabConfig.isLoading -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            state.currentStep == 1 -> Icon(Icons.Default.AutoAwesome, contentDescription = null)
            state.currentStep == 3 -> Icon(Icons.Default.Check, contentDescription = null)
            else -> Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stepTitles.getOrElse(displayStep) { "New Plan" },
                tagline = "New Plan",
                showGoBack = true,
                onNavIconClick = { if (state.currentStep > 0) viewModel.prevStep() else onBack() },
                stepCurrent = displayStep,
                stepTotal = stepTitles.size,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = fabConfig.onClick,
                expanded = fabConfig.enabled,
                icon = fabIcon,
                text = { Text(fabConfig.label) },
                containerColor = when {
                    !fabConfig.enabled -> MaterialTheme.colorScheme.surfaceVariant
                    fabConfig.isLoading -> MaterialTheme.colorScheme.secondaryContainer
                    state.currentStep == 3 -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.primaryContainer
                },
                contentColor = when {
                    !fabConfig.enabled -> MaterialTheme.colorScheme.onSurfaceVariant
                    fabConfig.isLoading -> MaterialTheme.colorScheme.onSecondaryContainer
                    state.currentStep == 3 -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state.currentStep) {
                0 -> PlannerStep0(state, viewModel)
                1 -> PlannerStep1(state, viewModel)
                2 -> PlannerStep2(state, viewModel)
                3 -> PlannerStep3(state, viewModel)
            }
        }
    }
}