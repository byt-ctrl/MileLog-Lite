package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Instrument Ledger depth model: flat.
 *
 * The redesign removes elevation as a hierarchy device. Separation comes from
 * the two-material split (dark instrument over light logbook) and from hairline
 * rules, so every elevation token resolves to 0.dp. The helpers stay so any
 * remaining call site is a harmless no-op rather than a shadow that sneaks back
 * in by inertia.
 */
object MileLogElevation {
    val level0: Dp = 0.dp
    val level1: Dp = 0.dp
    val level2: Dp = 0.dp
}

fun Modifier.level1Shadow(shape: Shape = MileLogShapes.md): Modifier = this

fun Modifier.level2Shadow(shape: Shape = MileLogShapes.md): Modifier = this

val LocalMileLogElevation = staticCompositionLocalOf { MileLogElevation }

val MaterialTheme.mileLogElevation: MileLogElevation
    @Composable
    @ReadOnlyComposable
    get() = LocalMileLogElevation.current
