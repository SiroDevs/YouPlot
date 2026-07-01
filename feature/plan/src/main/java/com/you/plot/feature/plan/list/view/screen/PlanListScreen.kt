package com.you.plot.feature.plan.list.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.ui.action.AppTopBar
import com.you.plot.feature.plan.list.view.components.PlanItem
import com.you.plot.feature.plan.list.viewmodel.PlanListTab
import com.you.plot.feature.plan.list.viewmodel.PlanListViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlanListScreen(
    viewModel: PlanListViewModel,
    onCreatePlan: () -> Unit,
    onPlanClick: (Long) -> Unit,
    onStartTracking: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    val pagerState = rememberPagerState(initialPage = 0) { PlanListTab.entries.size }
    LaunchedEffect(pagerState.currentPage) {
        viewModel.selectTab(PlanListTab.entries[pagerState.currentPage])
    }

    // When duplicate() finishes, jump into the new plan so the user can adjust it.
    LaunchedEffect(state.clonedPlanId) {
        state.clonedPlanId?.let { id ->
            onPlanClick(id)
            viewModel.consumeClonedId()
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Plans") },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePlan) {
                Icon(Icons.Default.Add, contentDescription = "Create plan")
            }
        }
    ) { padding ->
        Column(Modifier
            .fillMaxSize()
            .padding(padding)) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                PlanListTab.entries.forEachIndexed { i, tab ->
                    Tab(
                        selected = pagerState.currentPage == i,
                        onClick = {},
                        text = { Text(tab.label) },
                    )
                }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (PlanListTab.entries[page]) {
                    PlanListTab.RECENT -> PlanListBody(
                        isLoading = state.isLoading && state.recent.isEmpty(),
                        plans = state.recent,
                        menuTargetId = state.menuTargetId,
                        onPlanClick = onPlanClick,
                        onStartTracking = onStartTracking,
                        onOpenMenu = viewModel::openMenu,
                        onDismissMenu = viewModel::dismissMenu,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onDelete = viewModel::delete,
                        onDuplicate = viewModel::duplicate,
                    )
                    PlanListTab.FAVORITES -> PlanListBody(
                        isLoading = false,
                        plans = state.favorites,
                        menuTargetId = state.menuTargetId,
                        onPlanClick = onPlanClick,
                        onStartTracking = onStartTracking,
                        onOpenMenu = viewModel::openMenu,
                        onDismissMenu = viewModel::dismissMenu,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onDelete = viewModel::delete,
                        onDuplicate = viewModel::duplicate,
                        emptyLabel = "No favorite plans yet",
                    )
                    PlanListTab.LISTS -> Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Custom lists coming soon",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private val PlanListTab.label: String
    get() = when (this) {
        PlanListTab.RECENT -> "Recent"
        PlanListTab.LISTS -> "Lists"
        PlanListTab.FAVORITES -> "Favorites"
    }

@Composable
private fun PlanListBody(
    isLoading: Boolean,
    plans: List<ActivityPlan>,
    menuTargetId: Long?,
    onPlanClick: (Long) -> Unit,
    onStartTracking: (Long) -> Unit,
    onOpenMenu: (Long) -> Unit,
    onDismissMenu: () -> Unit,
    onToggleFavorite: (ActivityPlan) -> Unit,
    onDelete: (Long) -> Unit,
    onDuplicate: (ActivityPlan) -> Unit,
    emptyLabel: String = "No plans yet — create a plan from a route",
) {
    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        plans.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No plans", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    emptyLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        else -> LazyColumn(Modifier.fillMaxSize()) {
            items(plans, key = { it.id }) { plan ->
                PlanItem(
                    plan = plan,
                    menuOpen = menuTargetId == plan.id,
                    onClick = { onPlanClick(plan.id) },
                    onStartTracking = { onStartTracking(plan.id) },
                    onOpenMenu = { onOpenMenu(plan.id) },
                    onDismissMenu = onDismissMenu,
                    onToggleFavorite = { onToggleFavorite(plan) },
                    onDelete = { onDelete(plan.id) },
                    onDuplicate = { onDuplicate(plan) },
                )
            }
        }
    }
}
