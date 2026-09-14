package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.myapplication.data.local.ThemeMode

/**
 * Instrument Ledger color schemes.
 *
 * The logbook roles are built from the paper and ink tokens in [Color.kt]. The
 * instrument roles are not part of the Material scheme at all: they are
 * constant across appearances and travel through [LocalLedgerColors].
 *
 * Dynamic color stays OFF so the two-material identity is identical on every
 * device.
 */
private val LightColorScheme = lightColorScheme(
    primary = PetrolLight,
    onPrimary = OnPetrolLight,
    primaryContainer = PetrolTintLight,
    onPrimaryContainer = PetrolLight,
    secondary = FuelLight,
    onSecondary = SurfaceLowestLight,
    secondaryContainer = FuelTintLight,
    onSecondaryContainer = InkLight,
    tertiary = SlateLight,
    onTertiary = OnSlateLight,
    tertiaryContainer = SlateTintLight,
    onTertiaryContainer = OnSlateTintLight,
    error = DangerLight,
    onError = OnDangerLight,
    errorContainer = DangerTintLight,
    onErrorContainer = OnDangerTintLight,
    background = PaperLight,
    onBackground = InkLight,
    surface = PanelLight,
    onSurface = InkLight,
    surfaceVariant = PanelHighLight,
    onSurfaceVariant = Ink2Light,
    surfaceDim = SurfaceDimLight,
    surfaceBright = SurfaceBrightLight,
    surfaceContainerLowest = SurfaceLowestLight,
    surfaceContainerLow = PanelSunkenLight,
    surfaceContainer = PaperLight,
    surfaceContainerHigh = PanelHighLight,
    surfaceContainerHighest = SurfaceHighestLight,
    surfaceTint = PetrolLight,
    inverseSurface = InstrumentSurface,
    inverseOnSurface = InstrumentText,
    inversePrimary = PetrolDark,
    outline = Ink3Light,
    outlineVariant = RuleLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PetrolDark,
    onPrimary = OnPetrolDark,
    primaryContainer = PetrolTintDark,
    onPrimaryContainer = PetrolDark,
    secondary = FuelDark,
    onSecondary = OnPetrolDark,
    secondaryContainer = FuelTintDark,
    onSecondaryContainer = FuelDark,
    tertiary = SlateDark,
    onTertiary = OnSlateDark,
    tertiaryContainer = SlateTintDark,
    onTertiaryContainer = OnSlateTintDark,
    error = DangerDark,
    onError = OnDangerDark,
    errorContainer = DangerTintDark,
    onErrorContainer = OnDangerTintDark,
    background = PaperDark,
    onBackground = InkDark,
    surface = PanelDark,
    onSurface = InkDark,
    surfaceVariant = PanelHighDark,
    onSurfaceVariant = Ink2Dark,
    surfaceDim = SurfaceDimDark,
    surfaceBright = SurfaceBrightDark,
    surfaceContainerLowest = SurfaceLowestDark,
    surfaceContainerLow = PanelSunkenDark,
    surfaceContainer = PanelDark,
    surfaceContainerHigh = PanelHighDark,
    surfaceContainerHighest = SurfaceHighestDark,
    surfaceTint = PetrolDark,
    inverseSurface = InkDark,
    inverseOnSurface = PanelDark,
    inversePrimary = PetrolLight,
    outline = Ink3Dark,
    outlineVariant = RuleDark
)

/**
 * Applies the Instrument Ledger identity.
 *
 * @param themeMode The appearance chosen in Settings. Only the logbook follows
 *   it: the instrument roles travel through [LocalLedgerColors] and stay dark
 *   in every mode, which is why there is no dark-instrument variant.
 */
@Composable
fun MileLogTheme(
    themeMode: ThemeMode = ThemeMode.DEFAULT,
    content: @Composable () -> Unit
) {
    val darkTheme = themeMode.isDark(isSystemInDarkTheme())
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val ledgerColors = if (darkTheme) DarkLedgerColors else LightLedgerColors

    CompositionLocalProvider(
        LocalSpacing provides Spacing,
        LocalMileLogShapes provides MileLogShapes,
        LocalMileLogElevation provides MileLogElevation,
        LocalLedgerColors provides ledgerColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = MileLogM3Shapes,
            content = content
        )
    }
}
