package com.sirofits.youplot.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sirofits.youplot.plan.ui.screen.PlanCreatorScreen
import com.sirofits.youplot.plan.ui.screen.PlanDetailScreen
import com.sirofits.youplot.plan.ui.screen.PlanListScreen
import com.sirofits.youplot.route.ui.screen.RouteListScreen
import com.sirofits.youplot.route.ui.screen.RoutePlotterScreen
import com.sirofits.youplot.tracker.ui.screen.TrackerScreen

// ─── Route destinations ───────────────────────────────────────────────────────

object Destinations {
    const val ROUTE_LIST = "routes"
    const val ROUTE_PLOTTER = "routes/plot"
    const val ROUTE_DETAIL = "routes/{routeId}"

    const val PLAN_LIST = "plans"
    const val PLAN_CREATE = "plans/create"
    const val PLAN_DETAIL = "plans/{planId}"

    const val TRACKER = "tracker/{planId}"

    fun planDetail(planId: Long) = "plans/$planId"
    fun tracker(planId: Long) = "tracker/$planId"
}

// ─── Bottom nav items ─────────────────────────────────────────────────────────

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Destinations.ROUTE_LIST, "Routes", Icons.Default.LocationOn),
    BottomNavItem(Destinations.PLAN_LIST, "Plans", Icons.Default.List),
)

// ─── Main NavGraph ─────────────────────────────────────────────────────────────

@Composable
fun YouPlotNavGraph() {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDest = navBackStack?.destination

    val showBottomNav = currentDest?.route in listOf(
        Destinations.ROUTE_LIST,
        Destinations.PLAN_LIST,
    )

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, item.label) },
                            label = { Text(item.label) },
                            selected = currentDest?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.ROUTE_LIST,
        ) {
            // ── Routes ─────────────────────────────────────────────────────
            composable(Destinations.ROUTE_LIST) {
                RouteListScreen(
                    onCreateRoute = { navController.navigate(Destinations.ROUTE_PLOTTER) },
                    onRouteClick = { routeId ->
                        // Navigate to plans filtered by route (future expansion)
                        navController.navigate(Destinations.PLAN_LIST)
                    },
                )
            }

            composable(Destinations.ROUTE_PLOTTER) {
                RoutePlotterScreen(
                    onBack = { navController.popBackStack() },
                    onRouteSaved = { navController.popBackStack() },
                )
            }

            // ── Plans ──────────────────────────────────────────────────────
            composable(Destinations.PLAN_LIST) {
                PlanListScreen(
                    onCreatePlan = { navController.navigate(Destinations.PLAN_CREATE) },
                    onPlanClick = { planId ->
                        navController.navigate(Destinations.planDetail(planId))
                    },
                    onStartTracking = { planId ->
                        navController.navigate(Destinations.tracker(planId))
                    },
                )
            }

            composable(Destinations.PLAN_CREATE) {
                PlanCreatorScreen(
                    onBack = { navController.popBackStack() },
                    onPlanSaved = { planId ->
                        navController.navigate(Destinations.planDetail(planId)) {
                            popUpTo(Destinations.PLAN_LIST)
                        }
                    },
                )
            }

            composable(
                route = Destinations.PLAN_DETAIL,
                arguments = listOf(navArgument("planId") { type = NavType.LongType }),
            ) {
                PlanDetailScreen(
                    onBack = { navController.popBackStack() },
                    onStartTracking = { planId ->
                        navController.navigate(Destinations.tracker(planId))
                    },
                )
            }

            // ── Tracker ────────────────────────────────────────────────────
            composable(
                route = Destinations.TRACKER,
                arguments = listOf(navArgument("planId") { type = NavType.LongType }),
            ) {
                TrackerScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
