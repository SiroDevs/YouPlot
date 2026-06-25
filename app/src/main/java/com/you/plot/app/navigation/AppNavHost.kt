package com.you.plot.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.you.plot.core.common.utils.Routes
import com.you.plot.core.data.repos.PrefsRepo
import com.you.plot.core.data.repos.ThemeRepo
import com.you.plot.feature.plan.creator.view.PlanCreatorScreen
import com.you.plot.feature.plan.details.view.PlanDetailScreen
import com.you.plot.feature.plan.list.view.PlanListScreen
import com.you.plot.feature.route.list.view.RouteListScreen
import com.you.plot.feature.route.plotter.view.screen.RoutePlotterScreen
import com.you.plot.feature.tracker.view.TrackerScreen

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Routes.ROUTE_LIST, "Routes", Icons.Default.LocationOn),
    BottomNavItem(Routes.PLAN_LIST, "Plans", Icons.Default.List),
)

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    themeRepo: ThemeRepo,
    prefsRepo: PrefsRepo,
) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDest = navBackStack?.destination

    val showBottomNav = currentDest?.route in listOf(
        Routes.ROUTE_LIST,
        Routes.PLAN_LIST,
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.ROUTE_LIST,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.ROUTE_LIST) {
                RouteListScreen(
                    viewModel = hiltViewModel(),
                    onCreateRoute = { navController.navigate(Routes.ROUTE_PLOTTER) },
                    onRouteClick = { navController.navigate(Routes.PLAN_LIST) },
                )
            }

            composable(Routes.ROUTE_PLOTTER) {
                RoutePlotterScreen(
                    viewModel = hiltViewModel(),
                    onBack = { navController.popBackStack() },
                    onRouteSaved = { navController.popBackStack() },
                )
            }

            composable(Routes.PLAN_LIST) {
                PlanListScreen(
                    viewModel = hiltViewModel(),
                    onCreatePlan = { navController.navigate(Routes.PLAN_CREATE) },
                    onPlanClick = { planId ->
                        navController.navigate(Routes.planDetail(planId))
                    },
                    onStartTracking = { planId ->
                        navController.navigate(Routes.tracker(planId))
                    },
                )
            }

            composable(Routes.PLAN_CREATE) {
                PlanCreatorScreen(
                    viewModel = hiltViewModel(),
                    onBack = { navController.popBackStack() },
                    onPlanSaved = { planId ->
                        navController.navigate(Routes.planDetail(planId)) {
                            popUpTo(Routes.PLAN_LIST)
                        }
                    },
                )
            }

            composable(
                route = Routes.PLAN_DETAIL,
                arguments = listOf(navArgument("planId") { type = NavType.LongType }),
            ) {
                PlanDetailScreen(
                    viewModel = hiltViewModel(),
                    onBack = { navController.popBackStack() },
                    onStartTracking = { planId ->
                        navController.navigate(Routes.tracker(planId))
                    },
                )
            }

            composable(
                route = Routes.TRACKER,
                arguments = listOf(navArgument("planId") { type = NavType.LongType }),
            ) {
                TrackerScreen(
                    viewModel = hiltViewModel(),
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
