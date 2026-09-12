package com.example.myapplication.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Instrument Ledger edge language: small radii, machined rather than soft.
 * Panels are 6.dp, controls 4.dp, chips 4.dp. There is no pill shape outside
 * the circular tab action.
 */
object MileLogShapes {
    val sm = RoundedCornerShape(4.dp)
    val md = RoundedCornerShape(6.dp)
    val lg = RoundedCornerShape(8.dp)
    val xl = RoundedCornerShape(12.dp)
    val xxl = RoundedCornerShape(16.dp)
    val full = RoundedCornerShape(percent = 50)
    val chip = sm
}

val MileLogM3Shapes = Shapes(
    extraSmall = MileLogShapes.sm,
    small = MileLogShapes.sm,
    medium = MileLogShapes.md,
    large = MileLogShapes.lg,
    extraLarge = MileLogShapes.xl
)

val LocalMileLogShapes = staticCompositionLocalOf { MileLogShapes }

val MaterialTheme.mileLogShapes: MileLogShapes
    @Composable
    @ReadOnlyComposable
    get() = LocalMileLogShapes.current
