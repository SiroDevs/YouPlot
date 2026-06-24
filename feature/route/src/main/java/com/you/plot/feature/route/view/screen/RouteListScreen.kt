package com.you.plot.feature.route.view.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.entity.SportType
import com.you.plot.feature.route.viewmodel.RouteListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteListScreen(
    viewModel: RouteListViewModel,
    onCreateRoute: () -> Unit,
    onRouteClick: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Routes") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateRoute) {
                Icon(Icons.Default.Add, contentDescription = "Plot route")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.routes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No routes yet", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Tap + to plot your first route", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(state.routes, key = { it.id }) { route ->
                    RouteItem(route = route, onClick = { onRouteClick(route.id) })
                }
            }
        }
    }
}

@Composable
private fun RouteItem(route: Route, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onClick,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(route.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("%.1f km".format(route.totalDistanceKm), style = MaterialTheme.typography.bodySmall)
                Text(route.sportType.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall)
                if (route.isRoundTrip) Text("Round trip", style = MaterialTheme.typography.bodySmall)
            }
            if (route.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(route.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
        }
    }
}
