package com.example.myapplication.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
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
import com.example.myapplication.ui.theme.MileLogMotion
import com.example.myapplication.ui.theme.MileLogWindow
import com.example.myapplication.ui.vehicle.AddEditVehicleScreen
import com.example.myapplication.ui.vehicle.VehiclesScreen

/**
 * Route constants for the MileLog Lite navigation graph.
 */
object MileLogRoutes {
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val ADD_ENTRY = "add_entry"
    const val EDIT_ENTRY = "edit_entry/{entryId}"
    const val CHARTS = "charts"
    const val VEHICLES = "vehicles"
    const val SETTINGS = "settings"
    const val VEHICLE_ADD = "vehicle_add"
    const val VEHICLE_EDIT = "vehicle_edit/{vehicleId}"

    /**
     * The bottom-navigation "Reports" tab renders the Charts destination.
     * A Navigation route uniquely identifies a destination, so the tab must
     * reference the canonical [CHARTS] route rather than register a second
     * destination; this alias keeps the tab layer decoupled from the route
     * name without duplicating graph entries.
     */
    const val REPORTS = CHARTS

    fun editEntry(entryId: Long): String = "edit_entry/$entryId"

    fun editVehicle(vehicleId: Long): String = "vehicle_edit/$vehicleId"
}

/**
 * How far a destination travels as it arrives or leaves, as a fraction of the
 * screen. A hint of direction rather than a slide show: the whole screen never
 * crosses the glass. Timings come from [MileLogMotion] so navigation keeps the
 * same tempo as every other transition in the app.
 */
private const val TRANSITION_SLIDE_FRACTION = 12

/** How the app shell is laid out at the current window width. */
enum class ShellLayout { Compact, Expanded }

/**
 * The shell's own layout mode, published so screens can react to it. A screen
 * cannot infer this from its own width: on expanded windows that width is the
 * window minus the rail, so an 800dp window would look "expanded" to a screen
 * while no rail exists.
 */
val LocalShellLayout = staticCompositionLocalOf { ShellLayout.Compact }

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
            currentRoute == MileLogRoutes.VEHICLES ||
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

        CompositionLocalProvider(
            LocalShellLayout provides if (expanded) ShellLayout.Expanded else ShellLayout.Compact
        ) {
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
    // The log screens have something to add; Settings and the vehicle list do
    // not, so the primary action stays where it means something.
    val showFab = showBottomBar &&
        currentRoute != MileLogRoutes.SETTINGS &&
        currentRoute != MileLogRoutes.VEHICLES

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
            modifier = Modifier.padding(innerPadding),
            // A short fade carries the arriving destination in; the small
            // horizontal offset only hints at direction (forward on push,
            // back on pop) instead of sliding whole screens past each other.
            enterTransition = {
                fadeIn(
                    animationSpec = tween(MileLogMotion.screenEnter, easing = MileLogMotion.easing)
                ) + slideInHorizontally(
                    animationSpec = tween(MileLogMotion.screenEnter, easing = MileLogMotion.easing),
                    initialOffsetX = { width -> width / TRANSITION_SLIDE_FRACTION }
                )
            },
            exitTransition = {
                // Leaving is quicker than arriving: the user has already read
                // the surface they are leaving behind.
                fadeOut(
                    animationSpec = tween(MileLogMotion.screenExit, easing = MileLogMotion.easing)
                )
            },
            popEnterTransition = {
                fadeIn(
                    animationSpec = tween(MileLogMotion.screenEnter, easing = MileLogMotion.easing)
                ) + slideInHorizontally(
                    animationSpec = tween(MileLogMotion.screenEnter, easing = MileLogMotion.easing),
                    initialOffsetX = { width -> -width / TRANSITION_SLIDE_FRACTION }
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(MileLogMotion.screenExit, easing = MileLogMotion.easing)
                ) + slideOutHorizontally(
                    animationSpec = tween(MileLogMotion.screenExit, easing = MileLogMotion.easing),
                    targetOffsetX = { width -> width / TRANSITION_SLIDE_FRACTION }
                )
            }
        ) {
            composable(MileLogRoutes.DASHBOARD) {
                DashboardScreen(
                    onAddEntry = onAddEntry,
                    onViewHistory = { onTabSelected(MileLogRoutes.HISTORY) },
                    onViewCharts = { onTabSelected(MileLogRoutes.CHARTS) },
                    onAddVehicle = { navController.navigate(MileLogRoutes.VEHICLE_ADD) },
                    onEditEntry = { entryId ->
                        navController.navigate(MileLogRoutes.editEntry(entryId))
                    }
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
            composable(MileLogRoutes.VEHICLES) {
                VehiclesScreen(
                    onAddVehicle = { navController.navigate(MileLogRoutes.VEHICLE_ADD) },
                    onEditVehicle = { vehicleId ->
                        navController.navigate(MileLogRoutes.editVehicle(vehicleId))
                    }
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
            composable(MileLogRoutes.VEHICLE_ADD) {
                AddEditVehicleScreen(
                    vehicleId = 0L,
                    onNavigateUp = { navController.navigateUp() }
                )
            }
            composable(MileLogRoutes.VEHICLE_EDIT) { vehicleBackStackEntry ->
                val vehicleId = vehicleBackStackEntry.arguments
                    ?.getString("vehicleId")
                    ?.toLongOrNull()
                    ?: 0L
                AddEditVehicleScreen(
                    vehicleId = vehicleId,
                    onNavigateUp = { navController.navigateUp() }
                )
            }
        }
    }
}
