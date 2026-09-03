package com.example.myapplication.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

object MileLogShapes {
    val sm = RoundedCornerShape(4.dp)
    val md = RoundedCornerShape(8.dp)
    val lg = RoundedCornerShape(12.dp)
    val xl = RoundedCornerShape(16.dp)
    val xxl = RoundedCornerShape(24.dp)
    val full = RoundedCornerShape(9999.dp)
    val chip = xl
}

val MileLogM3Shapes = Shapes(
    extraSmall = MileLogShapes.md,
    small = MileLogShapes.md,
    medium = MileLogShapes.md,
    large = MileLogShapes.lg,
    extraLarge = MileLogShapes.xxl
)

val LocalMileLogShapes = staticCompositionLocalOf { MileLogShapes }

val MaterialTheme.mileLogShapes: MileLogShapes
    @Composable
    @ReadOnlyComposable
    get() = LocalMileLogShapes.current