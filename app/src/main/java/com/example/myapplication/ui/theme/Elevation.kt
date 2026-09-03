package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * MileLog Lite elevation system (Kinetic Logic tonal layers, Sprint 6 §6.1.4).
 *
 * Level 0 (Base): application canvas, `background` (`surfaceBg #F4F5F7` light).
 * Level 1 (Cards): white `surfaceContainerLowest` surfaces, 1.dp elevation,
 *   8.dp shape, black at 10% with a 4.dp blur target.
 * Level 2 (Active): FAB and Bottom Nav, 4.dp elevation, black at 15% with a
 *   12.dp blur target.
 * Overlays: bottom sheets, dialogs and modals track a 20% dim token. The
 *   stock M3 dialogs in this BOM revision expose no scrim override, so the
 *   token is applied via a custom `Dialog`/`ModalBottomSheet` scrim layer
 *   when one is introduced — never by restyling `AlertDialog` internals.
 *
 * `Modifier.shadow` derives diffusion from `elevation`; the `*Blur` tokens
 * record the spec's blur targets for the `dropShadow` migration path.
 */
object MileLogElevation {
    val level1: Dp = 1.dp
    val level1Blur: Dp = 4.dp
    val level1Shadow: Color = Color.Black.copy(alpha = 0.10f)
    val level2: Dp = 4.dp
    val level2Blur: Dp = 12.dp
    val level2Shadow: Color = Color.Black.copy(alpha = 0.15f)
    val overlayScrim: Color = Color.Black.copy(alpha = 0.20f)
}

fun Modifier.level1Shadow(shape: Shape = MileLogShapes.md): Modifier = shadow(
    elevation = MileLogElevation.level1,
    shape = shape,
    ambientColor = MileLogElevation.level1Shadow,
    spotColor = MileLogElevation.level1Shadow
)

fun Modifier.level2Shadow(shape: Shape = MileLogShapes.md): Modifier = shadow(
    elevation = MileLogElevation.level2,
    shape = shape,
    ambientColor = MileLogElevation.level2Shadow,
    spotColor = MileLogElevation.level2Shadow
)

val LocalMileLogElevation = staticCompositionLocalOf { MileLogElevation }

val MaterialTheme.mileLogElevation: MileLogElevation
    @Composable
    @ReadOnlyComposable
    get() = LocalMileLogElevation.current