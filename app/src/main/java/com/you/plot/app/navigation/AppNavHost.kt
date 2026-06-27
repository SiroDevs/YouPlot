package com.you.plot.app.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.you.plot.core.common.utils.Routes
import com.you.plot.core.data.repos.ThemeRepo
import com.you.plot.feature.dashboard.view.screen.DashboardScreen
import com.you.plot.feature.extra.about.AboutScreen
import com.you.plot.feature.extra.help.HelpFeedbackScreen
import com.you.plot.feature.plan.creator.view.screen.PlanCreatorScreen
import com.you.plot.feature.plan.details.view.screen.PlanDetailScreen
import com.you.plot.feature.plan.list.view.screen.PlanListScreen
import com.you.plot.feature.route.detail.view.RouteDetailScreen
import com.you.plot.feature.route.list.view.RouteListScreen
import com.you.plot.feature.route.plotter.view.screen.RoutePlotterScreen
import com.you.plot.feature.settings.view.SettingsScreen
import com.you.plot.feature.tracker.view.screen.TrackerScreen

@Composable
fun AppNavHost(
    themeRepo: ThemeRepo,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
    ) {

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = hiltViewModel(),
                onPlotRoute = { navController.navigate(Routes.ROUTE_PLOTTER) },
                onViewAllRoutes = { navController.navigate(Routes.ROUTE_LIST) },
                // FIX: was navigating to ROUTE_LIST instead of the tapped route detail
                onRouteClick = { routeId -> navController.navigate(Routes.routeDetail(routeId)) },
                // FIX: create plan with no pre-selected route
                onCreatePlan = { navController.navigate(Routes.PLAN_CREATE) },
                onPlanClick = { planId -> navController.navigate(Routes.planDetail(planId)) },
                onStartTracking = { planId -> navController.navigate(Routes.tracker(planId)) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onAbout = { navController.navigate(Routes.ABOUT) },
                onHelpFeedback = { navController.navigate(Routes.HELP_FEEDBACK) },
            )
        }

        composable(Routes.ROUTE_LIST) {
            RouteListScreen(
                viewModel = hiltViewModel(),
                onCreateRoute = { navController.navigate(Routes.ROUTE_PLOTTER) },
                // FIX: was navigating to PLAN_LIST instead of the tapped route detail
                onRouteClick = { routeId -> navController.navigate(Routes.routeDetail(routeId)) },
            )
        }

        // NEW: Route detail screen — shows stats, waypoints, elevation + "Plan this Route" button
        composable(
            route = Routes.ROUTE_DETAIL,
            arguments = listOf(navArgument("routeId") { type = NavType.LongType }),
        ) {
            RouteDetailScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onCreatePlan = { routeId -> navController.navigate(Routes.planCreateForRoute(routeId)) },
            )
        }

        composable(Routes.ROUTE_PLOTTER) {
            RoutePlotterScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onRouteSaved = { routeId ->
                    navController.navigate(Routes.routeDetail(routeId)) {
                        popUpTo(Routes.ROUTE_PLOTTER) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.PLAN_LIST) {
            PlanListScreen(
                viewModel = hiltViewModel(),
                onCreatePlan = { navController.navigate(Routes.PLAN_CREATE) },
                onPlanClick = { planId -> navController.navigate(Routes.planDetail(planId)) },
                onStartTracking = { planId -> navController.navigate(Routes.tracker(planId)) },
            )
        }

        composable(Routes.PLAN_CREATE) {
            PlanCreatorScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onPlanSaved = { planId ->
                    navController.navigate(Routes.planDetail(planId)) {
                        popUpTo(Routes.DASHBOARD)
                    }
                },
            )
        }

        composable(
            route = Routes.PLAN_CREATE_FOR_ROUTE,
            arguments = listOf(navArgument("routeId") { type = NavType.LongType }),
        ) {
            PlanCreatorScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onPlanSaved = { planId ->
                    navController.navigate(Routes.planDetail(planId)) {
                        popUpTo(Routes.DASHBOARD)
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
                onStartTracking = { planId -> navController.navigate(Routes.tracker(planId)) },
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

        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = hiltViewModel(),
                themeRepo = themeRepo,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.HELP_FEEDBACK) {
            HelpFeedbackScreen(onBack = { navController.popBackStack() })
        }
    }
}