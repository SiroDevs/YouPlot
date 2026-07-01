/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.you.plot.feature.startpoint.list.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.utils.countryFlag
import com.you.plot.core.domain.entity.StartPoint
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.feature.startpoint.list.view.components.StartPointRow
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
    val pagerState = rememberPagerState(
        initialPage = state.selectedTab.ordinal,
    ) { StartPointListTab.entries.size }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.selectTab(StartPointListTab.entries[pagerState.currentPage])
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
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                StartPointListTab.entries.forEachIndexed { i, tab ->
                    Tab(
                        selected = pagerState.currentPage == i,
                        onClick = {},
                        text = { Text(tab.label) },
                    )
                }
            }
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
        }
    }
}

private val StartPointListTab.label: String
    get() = when (this) {
        StartPointListTab.RECENT -> "Recent"
        StartPointListTab.FAVORITES -> "Favorites"
    }

@Composable
private fun StartPointListBody(
    isLoading: Boolean,
    startPoints: List<StartPoint>,
    menuTargetId: Long?,
    onOpenMenu: (Long) -> Unit,
    onDismissMenu: () -> Unit,
    onStartRoute: (StartPoint) -> Unit,
    onEdit: (Long) -> Unit,
    onToggleFavorite: (StartPoint) -> Unit,
    onDelete: (Long) -> Unit,
) {
    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        startPoints.isEmpty() -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "No start points here yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        else -> LazyColumn(Modifier.fillMaxSize()) {
            item { Spacer(Modifier.height(8.dp)) }
            items(startPoints, key = { it.id }) { sp ->
                StartPointRow(
                    sp = sp,
                    menuOpen = menuTargetId == sp.id,
                    onOpenMenu = { onOpenMenu(sp.id) },
                    onDismissMenu = onDismissMenu,
                    onStartRoute = { onStartRoute(sp) },
                    onEdit = { onEdit(sp.id) },
                    onToggleFavorite = { onToggleFavorite(sp) },
                    onDelete = { onDelete(sp.id) },
                )
            }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}
