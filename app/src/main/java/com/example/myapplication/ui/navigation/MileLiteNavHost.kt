package com.example.myapplication.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
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
import com.example.myapplication.ui.theme.MileLogWindow

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
 * Root navigation host.
 *
 * The app shell is one instrument: a dark rail on expanded widths, a dark
 * bottom bar below them. Both carry the same destinations, so widening the
 * window never removes a way to move. The primary action (log a fill-up) is a
 * single petrol FAB owned by the shell, so no screen competes with another for
 * the same job.
 */
@Composable
fun MileLiteNavHost(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Entry and edit are full-screen flows; everything else is a destination
    // the shell keeps navigation for.
    val isEditDestination = currentRoute == MileLogRoutes.EDIT_ENTRY ||
        currentRoute?.startsWith("edit_entry/") == true
    val isTopLevel = !isEditDestination &&
        currentRoute != MileLogRoutes.ADD_ENTRY &&
        (currentRoute == MileLogRoutes.DASHBOARD ||
            currentRoute == MileLogRoutes.HISTORY ||
            currentRoute == MileLogRoutes.CHARTS ||
            currentRoute == MileLogRoutes.SETTINGS)

    val onTabSelected: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(MileLogRoutes.DASHBOARD) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    val onAddEntry: () -> Unit = { navController.navigate(MileLogRoutes.ADD_ENTRY) }

    BoxWithConstraints {
        val expanded = maxWidth >= MileLogWindow.expanded

        if (expanded) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                MileLogRail(
                    currentRoute = currentRoute,
                    onTabSelected = onTabSelected,
                    onAddEntry = onAddEntry
                )
                ShellScaffold(
                    showBottomBar = false,
                    currentRoute = currentRoute,
                    onTabSelected = onTabSelected,
                    onAddEntry = onAddEntry,
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            ShellScaffold(
                showBottomBar = isTopLevel,
                currentRoute = currentRoute,
                onTabSelected = onTabSelected,
                onAddEntry = onAddEntry,
                navController = navController,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ShellScaffold(
    showBottomBar: Boolean,
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    onAddEntry: () -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // The log screens have something to add; Settings does not, so the
    // primary action stays where it means something.
    val showFab = showBottomBar && currentRoute != MileLogRoutes.SETTINGS

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                MileLogBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = onTabSelected
                )
            }
        },
        floatingActionButton = {
            if (showFab) {
                MileLogFab(onClick = onAddEntry)
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MileLogRoutes.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MileLogRoutes.DASHBOARD) {
                DashboardScreen(
                    onAddEntry = onAddEntry,
                    onViewHistory = { onTabSelected(MileLogRoutes.HISTORY) },
                    onViewCharts = { onTabSelected(MileLogRoutes.CHARTS) }
                )
            }
            composable(MileLogRoutes.CHARTS) {
                ChartsScreen(onAddEntry = onAddEntry)
            }
            composable(MileLogRoutes.HISTORY) {
                HistoryScreen(
                    onEditEntry = { entryId ->
                        navController.navigate(MileLogRoutes.editEntry(entryId))
                    },
                    onAddEntry = onAddEntry
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
