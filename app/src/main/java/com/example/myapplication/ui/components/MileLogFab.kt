// floating action button with custom elevation and shape

package com.example.myapplication.ui.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myapplication.ui.theme.MileLogElevation
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.theme.level2Shadow

@Composable
fun MileLogFab(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.level2Shadow(MileLogShapes.full),
        shape = MileLogShapes.full,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = MileLogElevation.level2,
            pressedElevation = MileLogElevation.level2,
            focusedElevation = MileLogElevation.level2,
            hoveredElevation = MileLogElevation.level2
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}