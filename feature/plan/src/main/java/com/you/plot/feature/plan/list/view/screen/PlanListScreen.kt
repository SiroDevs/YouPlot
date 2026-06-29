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

package com.you.plot.feature.plan.list.view.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.feature.plan.list.view.components.PlanItem
import com.you.plot.feature.plan.list.viewmodel.PlanListUiState
import com.you.plot.feature.plan.list.viewmodel.PlanListViewModel

@Composable
fun PlanListScreen(
    viewModel: PlanListViewModel,
    onCreatePlan: () -> Unit,
    onPlanClick: (Long) -> Unit,
    onStartTracking: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    PlanListScreenContent(
        state = state,
        onCreatePlan = onCreatePlan,
        onPlanClick = onPlanClick,
        onStartTracking = onStartTracking,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanListScreenContent(
    state: PlanListUiState,
    onCreatePlan: () -> Unit,
    onPlanClick: (Long) -> Unit,
    onStartTracking: (Long) -> Unit,
) {
    Scaffold(
        topBar = { AppTopBar(title = "Plans") },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePlan) {
                Icon(Icons.Default.Add, contentDescription = "Create plan")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.plans.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No plans yet", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Create a plan from a route", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(state.plans, key = { it.id }) { plan ->
                    PlanItem(
                        plan = plan,
                        onClick = { onPlanClick(plan.id) },
                        onStartTracking = { onStartTracking(plan.id) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanListScreenPreview() {
    val plans = listOf(
        ActivityPlan(
            id = 1L,
            routeId = 1L,
            name = "Coast Tour Plan",
            description = "Five days along the Indian Ocean coast.",
            startDateMillis = 0L,
            numberOfDays = 5,
            avgSpeedKmh = 18.0,
            avgDistancePerDayKm = 96.0,
        ),
        ActivityPlan(
            id = 2L,
            routeId = 2L,
            name = "Mountain Loop",
            description = "Weekend mountain hike.",
            startDateMillis = 0L,
            numberOfDays = 2,
            avgSpeedKmh = 5.0,
            avgDistancePerDayKm = 12.0,
        ),
    )
    AppTheme {
        PlanListScreenContent(
            state = PlanListUiState(plans = plans, isLoading = false),
            onCreatePlan = {},
            onPlanClick = {},
            onStartTracking = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanListScreenEmptyPreview() {
    AppTheme {
        PlanListScreenContent(
            state = PlanListUiState(plans = emptyList(), isLoading = false),
            onCreatePlan = {},
            onPlanClick = {},
            onStartTracking = {},
        )
    }
}
