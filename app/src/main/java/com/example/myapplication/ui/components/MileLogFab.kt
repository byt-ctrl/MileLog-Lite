package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MileLogShapes

/**
 * Primary action for logging a fill-up. Squared rather than pill-shaped to
 * match the ledger's edge language, flat rather than elevated because the
 * redesign removes shadow as a hierarchy device, and petrol rather than amber
 * so the logbook keeps one calm primary action.
 */
@Composable
fun MileLogFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        text = { Text(stringResource(R.string.fab_add_label)) },
        icon = {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null
            )
        },
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = MileLogShapes.sm,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        )
    )
}
