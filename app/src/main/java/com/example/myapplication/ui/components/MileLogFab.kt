// Extended floating action button with custom elevation and shape.

package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MileLogElevation
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.theme.level2Shadow

@Composable
fun MileLogFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    icon: ImageVector = Icons.Rounded.AddCircle
) {
    // contentDescription is retained for backward compatibility with existing
    // callers but intentionally not applied to the icon: the extended FAB's
    // visible text label already provides the accessibility label per M3
    // guidance, so the icon's content description stays null to avoid a
    // redundant announcement.
    ExtendedFloatingActionButton(
        text = { Text(stringResource(R.string.fab_add_label)) },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        onClick = onClick,
        modifier = modifier
            .level2Shadow(MileLogShapes.full)
            .heightIn(min = 48.dp),
        shape = MileLogShapes.full,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = MileLogElevation.level2,
            pressedElevation = MileLogElevation.level2,
            focusedElevation = MileLogElevation.level2,
            hoveredElevation = MileLogElevation.level2
        )
    )
}
