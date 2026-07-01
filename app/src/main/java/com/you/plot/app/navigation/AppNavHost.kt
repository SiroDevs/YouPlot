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
import com.you.plot.feature.plan.planner.view.screen.PlannerScreen
import com.you.plot.feature.plan.details.view.screen.PlanDetailScreen
import com.you.plot.feature.plan.list.view.screen.PlanListScreen
import com.you.plot.feature.route.detail.view.screen.RouteDetailScreen
import com.you.plot.feature.route.edit.view.screen.RouteEditScreen
import com.you.plot.feature.route.list.view.RouteListScreen
import com.you.plot.feature.route.plotter.view.screen.PlotterScreen
import com.you.plot.feature.startpoint.form.view.StartPointFormScreen
import com.you.plot.feature.startpoint.list.view.screen.StartPointListScreen
import com.you.plot.feature.trash.view.screen.TrashBinScreen
import com.you.plot.feature.settings.view.screen.SettingsScreen
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
                onPlotRoute = { navController.navigate(Routes.routePlotter()) },
                onViewAllRoutes = { navController.navigate(Routes.ROUTE_LIST) },
                onViewAllPlans = { navController.navigate(Routes.PLAN_LIST) },
                onRouteClick = { routeId -> navController.navigate(Routes.routeDetail(routeId)) },
                onCreatePlan = { navController.navigate(Routes.PLAN_CREATE) },
                onPlanClick = { planId -> navController.navigate(Routes.planDetail(planId)) },
                onStartTracking = { planId -> navController.navigate(Routes.tracker(planId)) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onAbout = { navController.navigate(Routes.ABOUT) },
                onHelpFeedback = { navController.navigate(Routes.HELP_FEEDBACK) },
                onStartPoints = { navController.navigate(Routes.START_POINT_LIST) },
                onTrashBin = { navController.navigate(Routes.TRASH_BIN) },
            )
        }

        composable(Routes.ROUTE_LIST) {
            RouteListScreen(
                viewModel = hiltViewModel(),
                onCreateRoute = { navController.navigate(Routes.routePlotter()) },
                onRouteClick = { routeId -> navController.navigate(Routes.routeDetail(routeId)) },
            )
        }

        composable(
            route = Routes.ROUTE_DETAIL,
            arguments = listOf(navArgument("routeId") { type = NavType.LongType }),
        ) {
            RouteDetailScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onCreatePlan = { routeId -> navController.navigate(Routes.planCreateForRoute(routeId)) },
                onEditRoute = { routeId -> navController.navigate(Routes.routeEdit(routeId)) },
            )
        }

        composable(
            route = Routes.ROUTE_EDIT,
            arguments = listOf(navArgument("routeId") { type = NavType.LongType }),
        ) {
            RouteEditScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.ROUTE_PLOTTER,
            arguments = listOf(navArgument("startPointId") {
                type = NavType.LongType
                defaultValue = 0L
            }),
        ) {
            PlotterScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onRouteSaved = { routeId ->
                    navController.navigate(Routes.routeDetail(routeId)) {
                        popUpTo(Routes.ROUTE_PLOTTER) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.START_POINT_LIST) {
            StartPointListScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onAddNew = { navController.navigate(Routes.START_POINT_ADD) },
                onEdit = { id -> navController.navigate(Routes.startPointEdit(id)) },
                onStartRouteFrom = { sp ->
                    navController.navigate(Routes.routePlotter(sp.id))
                },
            )
        }

        composable(
            route = Routes.START_POINT_EDIT,
            arguments = listOf(navArgument("startPointId") { type = NavType.LongType }),
        ) {
            StartPointFormScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(Routes.TRASH_BIN) {
            TrashBinScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
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
            PlannerScreen(
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
            PlannerScreen(
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