package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.charts.ChartsScreen
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.entry.AddEditEntryScreen
import com.example.myapplication.ui.history.HistoryScreen

/**
 * Route constants for the MileLog Lite navigation graph.
 */
object MileLogRoutes {
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val ADD_ENTRY = "add_entry"
    const val EDIT_ENTRY = "edit_entry/{entryId}"
    const val CHARTS = "charts"

    fun editEntry(entryId: Long): String = "edit_entry/$entryId"
}

/**
 * Root navigation host wiring the MileLog Lite screens together.
 */
@Composable
fun MileLiteNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = MileLogRoutes.DASHBOARD
    ) {
        composable(MileLogRoutes.DASHBOARD) {
            DashboardScreen(
                onAddEntry = { navController.navigate(MileLogRoutes.ADD_ENTRY) },
                onViewHistory = { navController.navigate(MileLogRoutes.HISTORY) },
                onViewCharts = { navController.navigate(MileLogRoutes.CHARTS) }
            )
        }
        composable(MileLogRoutes.CHARTS) {
            ChartsScreen(
                onNavigateUp = { navController.navigateUp() },
                onAddEntry = { navController.navigate(MileLogRoutes.ADD_ENTRY) }
            )
        }
        composable(MileLogRoutes.HISTORY) {
            HistoryScreen(
                onEditEntry = { entryId -> navController.navigate(MileLogRoutes.editEntry(entryId)) },
                onAddEntry = { navController.navigate(MileLogRoutes.ADD_ENTRY) },
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(MileLogRoutes.ADD_ENTRY) {
            AddEditEntryScreen(
                entryId = 0L,
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(MileLogRoutes.EDIT_ENTRY) { backStackEntry ->
            val entryId = backStackEntry.arguments
                ?.getString("entryId")
                ?.toLongOrNull()
                ?: 0L
            AddEditEntryScreen(
                entryId = entryId,
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}
