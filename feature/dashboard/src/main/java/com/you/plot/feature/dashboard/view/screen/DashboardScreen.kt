package com.you.plot.feature.dashboard.view.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.feature.dashboard.dashboard.utils.DashboardUiState
import com.you.plot.feature.dashboard.dashboard.utils.PlanFilter
import com.you.plot.feature.dashboard.view.components.DashboardPlanItem
import com.you.plot.feature.dashboard.view.components.DashboardSectionHeader
import com.you.plot.feature.dashboard.view.components.EmptyDashboard
import com.you.plot.feature.dashboard.view.components.PlanFilterRow
import com.you.plot.feature.dashboard.view.components.RecentRoutesRow
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
    onRouteClick: (Long) -> Unit,
    onCreatePlan: () -> Unit,
    onPlanClick: (Long) -> Unit,
    onStartTracking: (Long) -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    onHelpFeedback: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    DashboardScreenContent(
        state = state,
        onPlotRoute = onPlotRoute,
        onViewAllRoutes = onViewAllRoutes,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreenContent(
    state: DashboardUiState,
    onPlotRoute: () -> Unit,
    onViewAllRoutes: () -> Unit,
    onRouteClick: (Long) -> Unit,
    onCreatePlan: () -> Unit,
    onPlanClick: (Long) -> Unit,
    onStartTracking: (Long) -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    onHelpFeedback: () -> Unit,
    onSetPlanFilter: (PlanFilter) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "YouPlot",
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = { menuExpanded = false; onSettings() },
                            )
                            DropdownMenuItem(
                                text = { Text("About YouPlot") },
                                onClick = { menuExpanded = false; onAbout() },
                            )
                            DropdownMenuItem(
                                text = { Text("Help & Feedback") },
                                onClick = { menuExpanded = false; onHelpFeedback() },
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (!state.isEmpty && !state.isLoading) ExtendedFloatingActionButton(
                onClick = onPlotRoute,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Plot Now") },
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

        if (state.isEmpty) {
            EmptyDashboard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onGetStarted = onPlotRoute,
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (state.recentRoutes.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    DashboardSectionHeader(
                        title = "Recent Routes",
                        actionLabel = "View All",
                        onAction = onViewAllRoutes,
                    )
                }
                item {
                    RecentRoutesRow(
                        routes = state.recentRoutes,
                        onRouteClick = onRouteClick,
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                DashboardSectionHeader(
                    title = "Plans",
                    actionLabel = "New Plan",
                    onAction = onCreatePlan,
                )
                PlanFilterRow(
                    selected = state.planFilter,
                    onSelect = onSetPlanFilter,
                )
                Spacer(Modifier.height(4.dp))
            }

            if (state.filteredPlans.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            when (state.planFilter) {
                                PlanFilter.UPCOMING -> "No upcoming plans"
                                PlanFilter.PAST -> "No past plans"
                                else -> "No plans yet — create one from a route"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(state.filteredPlans, key = { it.id }) { plan ->
                    DashboardPlanItem(
                        plan = plan,
                        onClick = { onPlanClick(plan.id) },
                        onStartTracking = { onStartTracking(plan.id) },
                    )
                }
            }

            item { Spacer(Modifier.height(88.dp)) } // FAB clearance
        }
    }
}

private val sampleRoutes = listOf(
    Route(
        id = 1L,
        name = "Morning Loop",
        sportType = SportType.RUNNING,
        startPoint = LatLng(-1.286, 36.817),
        endPoint = LatLng(-1.300, 36.830),
        totalDistanceKm = 8.4,
    ),
    Route(
        id = 2L,
        name = "Karura Forest Trail",
        sportType = SportType.HIKING,
        startPoint = LatLng(-1.243, 36.835),
        endPoint = LatLng(-1.250, 36.850),
        totalDistanceKm = 12.7,
    ),
    Route(
        id = 3L,
        name = "Lakeside Ride",
        sportType = SportType.CYCLING,
        startPoint = LatLng(-1.310, 36.700),
        endPoint = LatLng(-1.320, 36.720),
        totalDistanceKm = 24.0,
    ),
)

private val samplePlans = listOf(
    ActivityPlan(
        id = 10L,
        routeId = 1L,
        name = "Weekend Run",
        description = "Easy morning loop",
        startDateMillis = System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
        numberOfDays = 1,
        avgSpeedKmh = 10.0,
        avgDistancePerDayKm = 8.4,
    ),
    ActivityPlan(
        id = 11L,
        routeId = 2L,
        name = "Forest Hike",
        description = "Half-day trail outing",
        startDateMillis = System.currentTimeMillis() + 3L * 24 * 60 * 60 * 1000L,
        numberOfDays = 1,
        avgSpeedKmh = 4.5,
        avgDistancePerDayKm = 12.7,
    ),
)

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    AppTheme {
        DashboardScreenContent(
            state = DashboardUiState(
                recentRoutes = sampleRoutes,
                plans = samplePlans,
                planFilter = PlanFilter.ALL,
                isLoading = false,
            ),
            onPlotRoute = {},
            onViewAllRoutes = {},
            onRouteClick = {},
            onCreatePlan = {},
            onPlanClick = {},
            onStartTracking = {},
            onSettings = {},
            onAbout = {},
            onHelpFeedback = {},
            onSetPlanFilter = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardEmptyPreview() {
    AppTheme {
        DashboardScreenContent(
            state = DashboardUiState(
                recentRoutes = emptyList(),
                plans = emptyList(),
                planFilter = PlanFilter.ALL,
                isLoading = false,
            ),
            onPlotRoute = {},
            onViewAllRoutes = {},
            onRouteClick = {},
            onCreatePlan = {},
            onPlanClick = {},
            onStartTracking = {},
            onSettings = {},
            onAbout = {},
            onHelpFeedback = {},
            onSetPlanFilter = {},
        )
    }
}
