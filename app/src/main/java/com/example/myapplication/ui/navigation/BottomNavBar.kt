package com.example.myapplication.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.myapplication.R

/**
 * Bottom navigation bar for the app's primary destinations (Sprint 6
 * §6.2.1).
 *
 * The bar is stateless: the caller supplies the active [currentRoute] and
 * receives tab taps through [onTabSelected], so navigation options such as
 * pop-up-to-start stay with the NavHost wiring.
 *
 * Tabs are declared in [bottomNavTabs] and rendered in order. The Dashboard
 * tab is implemented first; History, Add, Reports and Settings land
 * incrementally per the sprint plan. Container metrics (64dp height,
 * surface color, Level 2 shadow, top-corner rounding) and the active and
 * inactive item colors follow in the remaining §6.2.1 bullets.
 */
@Composable
fun BottomNavBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        bottomNavTabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        // The visible label announces the destination; a
                        // duplicate icon description would be read twice
                        // by screen readers.
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(tab.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

/**
 * One bottom-navigation destination. [route] refers to a [MileLogRoutes]
 * constant; the Reports tab will reuse the Charts destination through the
 * REPORTS alias.
 */
private data class BottomNavTab(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Tab inventory rendered by [BottomNavBar]. Dashboard and History are the
 * first of the five planned tabs (Sprint 6 §6.2.1); Add, Reports and
 * Settings are appended here as they are implemented. Labels use the
 * dedicated bottom_nav_* strings: tab labels are shorter than screen
 * titles (e.g. history_title is "Fuel History") and are localizable
 * independently.
 */
private val bottomNavTabs = listOf(
    BottomNavTab(
        route = MileLogRoutes.DASHBOARD,
        labelRes = R.string.bottom_nav_dashboard_label,
        selectedIcon = Icons.Rounded.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    ),
    BottomNavTab(
        route = MileLogRoutes.HISTORY,
        labelRes = R.string.bottom_nav_history_label,
        selectedIcon = Icons.Rounded.History,
        unselectedIcon = Icons.Outlined.History
    ),
    BottomNavTab(
        route = MileLogRoutes.REPORTS,
        labelRes = R.string.bottom_nav_reports_label,
        selectedIcon = Icons.Rounded.Assessment,
        unselectedIcon = Icons.Outlined.Assessment
    ),
    BottomNavTab(
        route = MileLogRoutes.SETTINGS,
        labelRes = R.string.bottom_nav_settings_label,
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)
