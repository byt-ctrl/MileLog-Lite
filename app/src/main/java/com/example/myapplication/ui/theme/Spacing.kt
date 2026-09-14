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

    /**
     * Content width at which two panels can sit side by side without cramping.
     * Measured against the screen's own frame, not the window: once the shell's
     * rail is on screen it has already taken 228dp out of the content, so a
     * window-width threshold would split a tablet into two unreadable columns.
     */
    val wide: Dp = 720.dp

    /**
     * Widest the logbook content is allowed to get. Backgrounds stay full-bleed;
     * only text and controls are capped, so a wide window centres the ledger
     * instead of stretching the instrument across it.
     */
    val contentMaxWidth: Dp = 1040.dp
}
