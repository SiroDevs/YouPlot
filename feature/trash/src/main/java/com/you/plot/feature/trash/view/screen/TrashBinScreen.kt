package com.you.plot.feature.trash.view.screen

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.utils.dateFmt
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.feature.trash.view.components.TrashRow
import com.you.plot.feature.trash.viewmodel.TrashBinViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TrashBinScreen(
    viewModel: TrashBinViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    val tabs = remember(state) {
        buildList {
            if (state.trashedRoutes.isNotEmpty()) add("Routes")
            if (state.trashedPlans.isNotEmpty()) add("Plans")
            if (state.trashedStartPoints.isNotEmpty()) add("Start Points")
        }
    }

    val pagerState = rememberPagerState(initialPage = 0) { tabs.size.coerceAtLeast(1) }
    LaunchedEffect(tabs.size) {
        if (pagerState.currentPage >= tabs.size && tabs.isNotEmpty()) {
            pagerState.scrollToPage(0)
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Trash Bin", showGoBack = true, onNavIconClick = onBack) },
    ) { padding ->
        Column(Modifier
            .fillMaxSize()
            .padding(padding)) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                tabs.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Trash bin is empty",
                            style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Deleted items land here for 30 days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                else -> {
                    PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                        tabs.forEachIndexed { i, title ->
                            Tab(
                                selected = pagerState.currentPage == i,
                                onClick = {},
                                text = { Text(title) },
                            )
                        }
                    }
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        when (tabs[page]) {
                            "Routes" -> LazyColumn(Modifier.fillMaxSize()) {
                                item { Spacer(Modifier.height(8.dp)) }
                                items(state.trashedRoutes, key = { it.id }) { r ->
                                    TrashRow(
                                        title = r.name,
                                        subtitle = "%.1f km · deleted ${
                                            r.deletedAt?.let { dateFmt.format(Date(it)) } ?: "recently"
                                        }".format(r.totalDist),
                                        onRestore = { viewModel.restoreRoute(r.id) },
                                        onDelete = { viewModel.permanentlyDeleteRoute(r.id) },
                                    )
                                }
                                item { Spacer(Modifier.height(88.dp)) }
                            }
                            "Plans" -> LazyColumn(Modifier.fillMaxSize()) {
                                item { Spacer(Modifier.height(8.dp)) }
                                items(state.trashedPlans, key = { it.id }) { p ->
                                    TrashRow(
                                        title = p.name,
                                        subtitle = "${p.numberOfDays} day${if (p.numberOfDays == 1) "" else "s"} · deleted ${
                                            p.deletedAt?.let { dateFmt.format(Date(it)) } ?: "recently"
                                        }",
                                        onRestore = { viewModel.restorePlan(p.id) },
                                        onDelete = { viewModel.permanentlyDeletePlan(p.id) },
                                    )
                                }
                                item { Spacer(Modifier.height(88.dp)) }
                            }
                            "Start Points" -> LazyColumn(Modifier.fillMaxSize()) {
                                item { Spacer(Modifier.height(8.dp)) }
                                items(state.trashedStartPoints, key = { it.id }) { sp ->
                                    TrashRow(
                                        title = sp.name,
                                        subtitle = "Used ${sp.usageCount} time${
                                            if (sp.usageCount == 1) "" else "s"
                                        } · deleted ${
                                            sp.deletedAt?.let { dateFmt.format(Date(it)) } ?: "recently"
                                        }",
                                        onRestore = { viewModel.restoreStartPoint(sp.id) },
                                        onDelete = { viewModel.permanentlyDeleteStartPoint(sp.id) },
                                    )
                                }
                                item { Spacer(Modifier.height(88.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
