package com.example.myapplication.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.components.MileLogWordmark
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.theme.ledger
import com.example.myapplication.ui.theme.spacing

private data class NavDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val navDestinations = listOf(
    NavDestination(
        route = MileLogRoutes.DASHBOARD,
        labelRes = R.string.bottom_nav_dashboard_label,
        selectedIcon = Icons.Rounded.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    ),
    NavDestination(
        route = MileLogRoutes.HISTORY,
        labelRes = R.string.bottom_nav_history_label,
        selectedIcon = Icons.Rounded.History,
        unselectedIcon = Icons.Outlined.History
    ),
    NavDestination(
        route = MileLogRoutes.REPORTS,
        labelRes = R.string.bottom_nav_reports_label,
        selectedIcon = Icons.Rounded.Assessment,
        unselectedIcon = Icons.Outlined.Assessment
    ),
    NavDestination(
        route = MileLogRoutes.SETTINGS,
        labelRes = R.string.bottom_nav_settings_label,
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)

/**
 * Bottom navigation for compact and medium widths. Dark in every appearance:
 * the bar belongs to the instrument, not to the logbook, so it never takes the
 * paper color. Selection reads as a raised fill plus brighter label, never as a
 * colored stripe.
 */
@Composable
fun MileLogBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ledger = MaterialTheme.ledger
    NavigationBar(
        modifier = modifier,
        containerColor = ledger.chrome,
        contentColor = ledger.chromeText,
        tonalElevation = 0.dp
    ) {
        navDestinations.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(destination.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.labelRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ledger.chromeText,
                    selectedTextColor = ledger.chromeText,
                    unselectedIconColor = ledger.chromeTextMuted,
                    unselectedTextColor = ledger.chromeTextMuted,
                    indicatorColor = ledger.chromeRaisedHigh
                )
            )
        }
    }
}

/**
 * Navigation for expanded widths. Same destinations as [MileLogBottomBar], the
 * same dark instrument material, plus the wordmark and the primary action. This
 * is what stops the wide layout from losing its navigation entirely.
 */
@Composable
fun MileLogRail(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    onAddEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ledger = MaterialTheme.ledger
    val spacing = MaterialTheme.spacing

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(228.dp)
            .background(ledger.chrome)
            .padding(horizontal = spacing.md, vertical = spacing.lg)
    ) {
        MileLogWordmark()
        Spacer(Modifier.height(spacing.xl))

        navDestinations.forEach { destination ->
            RailItem(
                destination = destination,
                selected = currentRoute == destination.route,
                onClick = { onTabSelected(destination.route) }
            )
            Spacer(Modifier.height(2.dp))
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onAddEntry,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp),
            shape = MileLogShapes.sm,
            colors = ButtonDefaults.buttonColors(
                containerColor = ledger.chromeMarker,
                contentColor = ledger.chromeOnMarker
            )
        ) {
            Text(text = stringResource(R.string.action_add_fuel_entry))
        }

        Spacer(Modifier.height(spacing.md))
        Text(
            text = stringResource(R.string.rail_offline_note),
            style = MaterialTheme.typography.labelSmall,
            color = ledger.chromeTextMuted
        )
    }
}

@Composable
private fun RailItem(
    destination: NavDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val ledger = MaterialTheme.ledger
    val spacing = MaterialTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MileLogShapes.sm)
            .background(if (selected) ledger.chromeRaised else Color.Transparent)
            .clickable(onClick = onClick)
            .heightIn(min = 44.dp)
            .padding(horizontal = spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
            contentDescription = null,
            tint = if (selected) ledger.chromeText else ledger.chromeTextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(spacing.md))
        Text(
            text = stringResource(destination.labelRes),
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) ledger.chromeText else ledger.chromeTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
