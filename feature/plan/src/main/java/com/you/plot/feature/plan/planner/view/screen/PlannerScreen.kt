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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.feature.plan.planner.utils.PlannerUiState
import com.you.plot.feature.plan.planner.view.screen.steps.PlannerStep0
import com.you.plot.feature.plan.planner.view.screen.steps.PlannerStep1
import com.you.plot.feature.plan.planner.view.screen.steps.PlannerStep2
import com.you.plot.feature.plan.planner.view.screen.steps.PlannerStep3
import com.you.plot.feature.plan.planner.viewmodel.PlannerViewModel

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

    PlannerScreenContent(
        state = state,
        onBack = onBack,
        onClearError = viewModel::clearError,
        onPrevStep = viewModel::prevStep,
        onNextStep = viewModel::nextStep,
        onSavePlan = viewModel::savePlan,
        renderStep = { s ->
            when (s.currentStep) {
                0 -> PlannerStep0(s, viewModel)
                1 -> PlannerStep1(s, viewModel)
                2 -> PlannerStep2(s, viewModel)
                3 -> PlannerStep3(s, viewModel)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlannerScreenContent(
    state: PlannerUiState,
    onBack: () -> Unit,
    onClearError: () -> Unit,
    onPrevStep: () -> Unit,
    onNextStep: () -> Unit,
    onSavePlan: () -> Unit,
    renderStep: @Composable (PlannerUiState) -> Unit,
) {
    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = onClearError,
            title = { Text("Notice") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = onClearError) { Text("OK") } },
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
            onClick = onNextStep,
        )
        1 -> FabConfig(
            label = if (state.isGenerating) "Generating…" else "Generate Schedule",
            isLoading = state.isGenerating,
            onClick = { if (!state.isGenerating) onNextStep() },
        )
        2 -> FabConfig(
            label = "Review",
            onClick = onNextStep,
        )
        3 -> FabConfig(
            label = if (state.isSaving) "Saving…" else "Save Plan",
            isLoading = state.isSaving,
            onClick = { if (!state.isSaving) onSavePlan() },
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
                onNavIconClick = { if (state.currentStep > 0) onPrevStep() else onBack() },
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
            renderStep(state)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlannerScreenPreview() {
    val sampleRoute = Route(
        id = 1L,
        name = "Coast Tour",
        sportType = SportType.CYCLING,
        startPoint = LatLng(-1.286, 36.817),
        endPoint = LatLng(-4.04, 39.67),
        totalDistanceKm = 480.0,
    )
    AppTheme {
        PlannerScreenContent(
            state = PlannerUiState(
                routes = listOf(sampleRoute),
                selectedRoute = sampleRoute,
                planName = "Coast Tour Plan",
                numberOfDays = 5,
                currentStep = 1,
            ),
            onBack = {},
            onClearError = {},
            onPrevStep = {},
            onNextStep = {},
            onSavePlan = {},
            renderStep = {},
        )
    }
}
