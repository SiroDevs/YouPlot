package com.you.plot.feature.route.list.view

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
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.ui.action.AppTopBar
import com.you.plot.feature.route.list.viewmodel.RouteListTab
import com.you.plot.feature.route.list.viewmodel.RouteListViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RouteListScreen(
    viewModel: RouteListViewModel,
    onCreateRoute: () -> Unit,
    onRouteClick: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    val pagerState = rememberPagerState(initialPage = 0) { RouteListTab.entries.size }
    LaunchedEffect(pagerState.currentPage) {
        viewModel.selectTab(RouteListTab.entries[pagerState.currentPage])
    }

    state.deleteError?.let { err ->
        AlertDialog(
            onDismissRequest = viewModel::clearDeleteError,
            title = { Text("Cannot delete route") },
            text = { Text(err) },
            confirmButton = {
                TextButton(onClick = viewModel::clearDeleteError) { Text("OK") }
            },
        )
    }

    Scaffold(
        topBar = { AppTopBar(title = "Routes", showGoBack = true) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateRoute) {
                Icon(Icons.Default.Add, contentDescription = "Plot route")
            }
        },
    ) { padding ->
        Column(Modifier
            .fillMaxSize()
            .padding(padding)) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                RouteListTab.entries.forEachIndexed { i, tab ->
                    Tab(
                        selected = pagerState.currentPage == i,
                        onClick = {},
                        text = { Text(tab.label) },
                    )
                }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (RouteListTab.entries[page]) {
                    RouteListTab.RECENT -> RouteListBody(
                        isLoading = state.isLoading && state.recent.isEmpty(),
                        routes = state.recent,
                        menuTargetId = state.menuTargetId,
                        onOpenMenu = viewModel::openMenu,
                        onDismissMenu = viewModel::dismissMenu,
                        onRouteClick = onRouteClick,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onDelete = viewModel::delete,
                    )
                    RouteListTab.FAVORITES -> RouteListBody(
                        isLoading = false,
                        routes = state.favorites,
                        menuTargetId = state.menuTargetId,
                        onOpenMenu = viewModel::openMenu,
                        onDismissMenu = viewModel::dismissMenu,
                        onRouteClick = onRouteClick,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onDelete = viewModel::delete,
                        emptyLabel = "No favorites yet — long-press a route to favorite it",
                    )
                    RouteListTab.LISTS -> Box(
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

private val RouteListTab.label: String
    get() = when (this) {
        RouteListTab.RECENT -> "Recent"
        RouteListTab.LISTS -> "Lists"
        RouteListTab.FAVORITES -> "Favorites"
    }

@Composable
private fun RouteListBody(
    isLoading: Boolean,
    routes: List<Route>,
    menuTargetId: Long?,
    onOpenMenu: (Long) -> Unit,
    onDismissMenu: () -> Unit,
    onRouteClick: (Long) -> Unit,
    onToggleFavorite: (Route) -> Unit,
    onDelete: (Long) -> Unit,
    emptyLabel: String = "No routes yet — tap + to plot your first",
) {
    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        routes.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.Map, null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    modifier = Modifier.size(56.dp))
                Text(emptyLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        else -> LazyColumn(Modifier.fillMaxSize()) {
            item { Spacer(Modifier.height(8.dp)) }
            items(routes, key = { it.id }) { route ->
                RouteListItem(
                    route = route,
                    menuOpen = menuTargetId == route.id,
                    onClick = { onRouteClick(route.id) },
                    onOpenMenu = { onOpenMenu(route.id) },
                    onDismissMenu = onDismissMenu,
                    onToggleFavorite = { onToggleFavorite(route) },
                    onDelete = { onDelete(route.id) },
                )
            }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RouteListItem(
    route: Route,
    menuOpen: Boolean,
    onClick: () -> Unit,
    onOpenMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .combinedClickable(onClick = onClick, onLongClick = onOpenMenu),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .padding(2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.DirectionsRun, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    route.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "%.1f km".format(route.totalDist),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                    Text("·", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        route.sportType.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (route.isRoundTrip) {
                        Text("·", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Round trip",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (route.isFavorite) {
                Icon(
                    Icons.Outlined.Star,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Box {
                Text(
                    "↑%.0fm".format(route.elevationGain),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = onDismissMenu) {
                    DropdownMenuItem(
                        text = { Text(if (route.isFavorite) "Unfavorite" else "Favorite") },
                        leadingIcon = {
                            Icon(
                                if (route.isFavorite) Icons.Outlined.Star
                                else Icons.Outlined.StarOutline,
                                null,
                            )
                        },
                        onClick = { onDismissMenu(); onToggleFavorite() },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = { onDismissMenu(); onDelete() },
                    )
                }
            }
        }
    }
}
