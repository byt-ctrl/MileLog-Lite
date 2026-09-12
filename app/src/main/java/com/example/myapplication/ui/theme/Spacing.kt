package com.example.myapplication.ui.theme

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Instrument Ledger spacing scale. Every step is a multiple of the base unit:
 * 4 (micro), 8/12 (within a group), 16/24 (between groups), 32/48 (section
 * breaks). The gap between groups is always at least twice the gap within one.
 */
object Spacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 48.dp
    val touchTarget: Dp = 48.dp
    val touchTargetMin: Dp = 44.dp
}

val LocalSpacing = staticCompositionLocalOf { Spacing }

val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

fun Modifier.touchTargetMinHeight(): Modifier = heightIn(min = Spacing.touchTarget)

fun Modifier.minTouchTargetHeight(): Modifier = heightIn(min = Spacing.touchTargetMin)

/**
 * Width breakpoints. The ledger recomposes rather than stretches: the readout
 * strip is a ruled list under [medium], the navigation is a bottom bar under
 * [expanded], and the ledger table shows its full column set at and above it.
 */
object MileLogWindow {
    val medium: Dp = 600.dp
    val expanded: Dp = 840.dp
}
