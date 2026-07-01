package com.you.plot.feature.plan.planner.view.screen.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.Route
import com.you.plot.feature.plan.planner.utils.PlannerUiState
import com.you.plot.feature.plan.planner.utils.PlanSource
import com.you.plot.feature.plan.planner.view.components.SourceCard
import com.you.plot.feature.plan.planner.viewmodel.PlannerViewModel

@Composable
internal fun PlannerStep0(state: PlannerUiState, vm: PlannerViewModel) {
    PlannerStep0Content(
        state = state,
        onPlanSourceChange = vm::setPlanSource,
        onRouteSelected = vm::selectRoute,
        onTemplateSelected = vm::selectTemplate,
    )
}

@Composable
private fun PlannerStep0Content(
    state: PlannerUiState,
    onPlanSourceChange: (PlanSource) -> Unit,
    onRouteSelected: (Route) -> Unit,
    onTemplateSelected: (ActivityPlan) -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.planSource == PlanSource.ROUTE,
                onClick = { onPlanSourceChange(PlanSource.ROUTE) },
                label = { Text("From Route") },
                modifier = Modifier.weight(1f),
            )
            FilterChip(
                selected = state.planSource == PlanSource.TEMPLATE,
                onClick = { onPlanSourceChange(PlanSource.TEMPLATE) },
                label = { Text("From Template") },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(12.dp))

        if (state.planSource == PlanSource.ROUTE) {
            Text("Select a Route", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (state.routes.isEmpty()) {
                Text("No routes saved yet. Plot a route first.", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                state.routes.forEach { route ->
                    SourceCard(
                        title = route.name,
                        subtitle = "${"%.1f".format(route.totalDist)} km · ${route.sportType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        isSelected = state.selectedRoute?.id == route.id,
                        onClick = { onRouteSelected(route) },
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
        } else {
            Text("Select a Template Plan", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (state.templatePlans.isEmpty()) {
                Text("No saved plans to use as templates yet.", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                state.templatePlans.forEach { plan ->
                    SourceCard(
                        title = plan.name,
                        subtitle = "${plan.numberOfDays} day(s) · ${"%.1f".format(plan.avgDailyDist)} km/day",
                        isSelected = state.selectedTemplate?.id == plan.id,
                        onClick = { onTemplateSelected(plan) },
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
        }

        Spacer(Modifier.height(88.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun PlannerStep0Preview() {
    val sampleRoute = Route(
        id = 1L,
        name = "Coast Tour",
        sportType = SportType.CYCLING,
        startPoint = LatLng(-1.286, 36.817),
        endPoint = LatLng(-4.04, 39.67),
        totalDist = 480.0,
    )
    val sampleRoute2 = Route(
        id = 2L,
        name = "Mountain Loop",
        sportType = SportType.HIKING,
        startPoint = LatLng(-1.286, 36.817),
        endPoint = LatLng(-1.0, 37.0),
        totalDist = 65.0,
    )
    AppTheme {
        PlannerStep0Content(
            state = PlannerUiState(
                routes = listOf(sampleRoute, sampleRoute2),
                selectedRoute = sampleRoute,
                planSource = PlanSource.ROUTE,
            ),
            onPlanSourceChange = {},
            onRouteSelected = {},
            onTemplateSelected = {},
        )
    }
}
