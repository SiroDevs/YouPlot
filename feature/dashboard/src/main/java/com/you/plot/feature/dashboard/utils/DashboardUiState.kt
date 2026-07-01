package com.you.plot.feature.dashboard.dashboard.utils

import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.Route
import java.util.Date

enum class PlanFilter { ALL, UPCOMING, PAST }

data class DashboardUiState(
    val recentRoutes: List<Route> = emptyList(),
    val plans: List<ActivityPlan> = emptyList(),
    val routesById: Map<Long, Route> = emptyMap(),
    val planFilter: PlanFilter = PlanFilter.ALL,
    val isLoading: Boolean = true,
) {
    val isEmpty: Boolean get() = !isLoading && recentRoutes.isEmpty() && plans.isEmpty()

    val filteredPlans: List<ActivityPlan>
        get() {
            val now = System.currentTimeMillis()
            return when (planFilter) {
                PlanFilter.ALL      -> plans
                PlanFilter.UPCOMING -> plans.filter { it.startDate >= now }
                PlanFilter.PAST     -> plans.filter { it.startDate < now }
            }
        }
}
