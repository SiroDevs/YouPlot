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
import androidx.compose.ui.unit.dp
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.feature.dashboard.dashboard.utils.PlanFilter
import com.you.plot.feature.dashboard.view.components.DashboardPlanItem
import com.you.plot.feature.dashboard.view.components.DashboardSectionHeader
import com.you.plot.feature.dashboard.view.components.PlanFilterRow
import com.you.plot.feature.dashboard.view.components.RecentRoutesRow
import com.you.plot.feature.dashboard.viewmodel.DashboardViewModel
import com.you.plot.feature.dashboard.view.components.EmptyDashboard
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("ConstantLocale")
val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
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
            ExtendedFloatingActionButton(
                onClick = onPlotRoute,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("+ Plot Now") },
            )
        },
    ) { padding ->

        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (state.isEmpty) {
            EmptyDashboard(
                modifier = Modifier.fillMaxSize().padding(padding),
                onGetStarted = onPlotRoute,
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
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
                    actionLabel = if (state.plans.isNotEmpty()) "New Plan" else null,
                    onAction = if (state.plans.isNotEmpty()) onCreatePlan else null,
                )
                PlanFilterRow(
                    selected = state.planFilter,
                    onSelect = viewModel::setPlanFilter,
                )
                Spacer(Modifier.height(4.dp))
            }

            if (state.filteredPlans.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            when (state.planFilter) {
                                PlanFilter.UPCOMING -> "No upcoming plans"
                                PlanFilter.PAST     -> "No past plans"
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
