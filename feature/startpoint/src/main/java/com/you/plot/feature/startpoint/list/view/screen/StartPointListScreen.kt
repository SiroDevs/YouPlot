package com.you.plot.feature.startpoint.list.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.you.plot.core.domain.entity.StartPoint
import com.you.plot.core.ui.action.AppTopBar
import com.you.plot.feature.startpoint.list.view.components.StartPointListBody
import com.you.plot.feature.startpoint.list.viewmodel.StartPointListTab
import com.you.plot.feature.startpoint.list.viewmodel.StartPointListViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StartPointListScreen(
    viewModel: StartPointListViewModel,
    onBack: () -> Unit,
    onAddNew: () -> Unit,
    onEdit: (Long) -> Unit,
    onStartRouteFrom: (StartPoint) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    val hasRecent = state.recent.isNotEmpty()
    val hasFavorites = state.favorites.isNotEmpty()
    val showTabs = hasRecent && hasFavorites

    // If only one list has content, pin the pager to that tab.
    val effectiveTab = when {
        showTabs -> state.selectedTab
        hasFavorites -> StartPointListTab.FAVORITES
        else -> StartPointListTab.RECENT
    }

    val pagerState = rememberPagerState(
        initialPage = effectiveTab.ordinal,
    ) { StartPointListTab.entries.size }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.selectTab(StartPointListTab.entries[pagerState.currentPage])
    }

    // Keep the pager in sync when list contents change and tabs get hidden/shown.
    LaunchedEffect(effectiveTab, showTabs) {
        if (!showTabs && pagerState.currentPage != effectiveTab.ordinal) {
            pagerState.scrollToPage(effectiveTab.ordinal)
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Start Points", showGoBack = true, onNavIconClick = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNew) {
                Icon(Icons.Default.Add, contentDescription = "Add start point")
            }
        },
    ) { padding ->
        Column(Modifier
            .fillMaxSize()
            .padding(padding)) {
            if (showTabs) {
                PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                    StartPointListTab.entries.forEachIndexed { i, tab ->
                        Tab(
                            selected = pagerState.currentPage == i,
                            onClick = {},
                            text = { Text(tab.label) },
                        )
                    }
                }
            }
            if (showTabs) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    val list = when (StartPointListTab.entries[page]) {
                        StartPointListTab.RECENT -> state.recent
                        StartPointListTab.FAVORITES -> state.favorites
                    }
                    StartPointListBody(
                        isLoading = state.isLoading && list.isEmpty(),
                        startPoints = list,
                        menuTargetId = state.menuTargetId,
                        onOpenMenu = viewModel::openMenu,
                        onDismissMenu = viewModel::dismissMenu,
                        onStartRoute = onStartRouteFrom,
                        onEdit = onEdit,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onDelete = viewModel::delete,
                    )
                }
            } else {
                val list = if (hasFavorites) state.favorites else state.recent
                StartPointListBody(
                    isLoading = state.isLoading && list.isEmpty(),
                    startPoints = list,
                    menuTargetId = state.menuTargetId,
                    onOpenMenu = viewModel::openMenu,
                    onDismissMenu = viewModel::dismissMenu,
                    onStartRoute = onStartRouteFrom,
                    onEdit = onEdit,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onDelete = viewModel::delete,
                )
            }
        }
    }
}

private val StartPointListTab.label: String
    get() = when (this) {
        StartPointListTab.RECENT -> "Recent"
        StartPointListTab.FAVORITES -> "Favorites"
    }

