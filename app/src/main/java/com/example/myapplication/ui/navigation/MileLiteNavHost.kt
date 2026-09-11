package com.example.myapplication.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.charts.ChartsScreen
import com.example.myapplication.ui.components.MileLogFab
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.entry.AddEditEntryScreen
import com.example.myapplication.ui.history.HistoryScreen
import com.example.myapplication.ui.settings.SettingsScreen

/**
 * Route constants for the MileLog Lite navigation graph.
 */
object MileLogRoutes {
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val ADD_ENTRY = "add_entry"
    const val EDIT_ENTRY = "edit_entry/{entryId}"
    const val CHARTS = "charts"
    const val SETTINGS = "settings"

    /**
     * The bottom-navigation "Reports" tab renders the Charts destination.
     * A Navigation route uniquely identifies a destination, so the tab must
     * reference the canonical [CHARTS] route rather than register a second
     * destination; this alias keeps the tab layer decoupled from the route
     * name without duplicating graph entries.
     */
    const val REPORTS = CHARTS

    fun editEntry(entryId: Long): String = "edit_entry/$entryId"
}

/**
 * Root navigation host wiring the MileLog Lite screens together.
 *
 * Wiring-only global chrome (approved Add plan): a single Scaffold owns the
 * bottom bar + centered FAB overlay. M3 Scaffold has no true docked/cutout
 * slot, so FabPosition.Center is the docked-center concept — no manual y
 * offset is applied. Per-screen FABs on Dashboard/History are left intact.
 */
@Composable
fun MileLiteNavHost(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Bottom bar is top-level only; entry/edit are full-screen flows.
    // EDIT_ENTRY carries an argument, so match the "edit_entry/" prefix in
    // addition to the templated route string.
    val isEditDestination = currentRoute == MileLogRoutes.EDIT_ENTRY ||
        currentRoute?.startsWith("edit_entry/") == true
    val showBottomBar = !isEditDestination &&
        currentRoute != MileLogRoutes.ADD_ENTRY &&
        (currentRoute == MileLogRoutes.DASHBOARD ||
        currentRoute == MileLogRoutes.HISTORY ||
        currentRoute == MileLogRoutes.CHARTS ||
        currentRoute == MileLogRoutes.SETTINGS)

    // Charts has no local FAB, so the global one gives it an Add path.
    // Dashboard/History keep their existing per-screen FABs (single-primary-
    // action rule: no duplicate global FAB there).
    val showFab = currentRoute == MileLogRoutes.CHARTS

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(MileLogRoutes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showFab) {
                MileLogFab(
                    onClick = { navController.navigate(MileLogRoutes.ADD_ENTRY) }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MileLogRoutes.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
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
            composable(MileLogRoutes.SETTINGS) {
                SettingsScreen()
            }
            composable(MileLogRoutes.ADD_ENTRY) {
                AddEditEntryScreen(
                    entryId = 0L,
                    onNavigateUp = { navController.navigateUp() }
                )
            }
            composable(MileLogRoutes.EDIT_ENTRY) { entryBackStackEntry ->
                val entryId = entryBackStackEntry.arguments
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
}
