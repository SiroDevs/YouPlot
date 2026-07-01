package com.you.plot.feature.dashboard.view.screen

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.feature.dashboard.dashboard.utils.DashboardUiState
import com.you.plot.feature.dashboard.dashboard.utils.PlanFilter
import com.you.plot.feature.dashboard.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("ConstantLocale")
val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onPlotRoute: () -> Unit,
    onViewAllRoutes: () -> Unit,
    onViewAllPlans: () -> Unit,
    onRouteClick: (Long) -> Unit,
    onCreatePlan: () -> Unit,
    onPlanClick: (Long) -> Unit,
    onStartTracking: (Long) -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    onHelpFeedback: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    DashboardContent(
        state = state,
        onPlotRoute = onPlotRoute,
        onViewAllRoutes = onViewAllRoutes,
        onViewAllPlans = onViewAllPlans,
        onRouteClick = onRouteClick,
        onCreatePlan = onCreatePlan,
        onPlanClick = onPlanClick,
        onStartTracking = onStartTracking,
        onSettings = onSettings,
        onAbout = onAbout,
        onHelpFeedback = onHelpFeedback,
        onSetPlanFilter = viewModel::setPlanFilter,
    )
}
