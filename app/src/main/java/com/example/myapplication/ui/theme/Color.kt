package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Instrument Ledger palette.
//
// Two materials, one object. The instrument (chrome) is always dark and never
// follows the theme: it is the backlit cluster of a car, where readings live.
// The logbook is the paper below it, and only the logbook flips between light
// and dark. Depth comes from that material change, not from shadow.

// --- Logbook, light -------------------------------------------------------

val PaperLight = Color(0xFFEDF0EF)
val PanelLight = Color(0xFFFBFCFB)
val PanelSunkenLight = Color(0xFFF4F7F6)
val PanelHighLight = Color(0xFFE6EAE8)
val SurfaceHighestLight = Color(0xFFDFE4E2)
val SurfaceDimLight = Color(0xFFE3E7E5)
val SurfaceBrightLight = Color(0xFFFFFFFF)
val SurfaceLowestLight = Color(0xFFFFFFFF)

val InkLight = Color(0xFF0E1413)
val Ink2Light = Color(0xFF414A47)
val Ink3Light = Color(0xFF5A635F)

val RuleLight = Color(0xFFD5DBD9)
val RuleStrongLight = Color(0xFFB9C2BF)

val PetrolLight = Color(0xFF0B4A46)
val PetrolStrongLight = Color(0xFF073B38)
val OnPetrolLight = Color(0xFFFFFFFF)
val PetrolTintLight = Color(0xFFDBE8E6)

val FuelLight = Color(0xFF8A5200)
val FuelTintLight = Color(0xFFF3E6CF)

val DangerLight = Color(0xFF9C2419)
val OnDangerLight = Color(0xFFFFFFFF)
val DangerTintLight = Color(0xFFF4DEDB)
val OnDangerTintLight = Color(0xFF5C150E)

val GoodLight = Color(0xFF1D6340)
val GoodTintLight = Color(0xFFDCEBE1)

val SlateLight = Color(0xFF4E5478)
val OnSlateLight = Color(0xFFFFFFFF)
val SlateTintLight = Color(0xFFE2E4F0)
val OnSlateTintLight = Color(0xFF2C3155)

// --- Logbook, dark --------------------------------------------------------

val PaperDark = Color(0xFF0C1112)
val PanelDark = Color(0xFF141A1C)
val PanelSunkenDark = Color(0xFF101618)
val PanelHighDark = Color(0xFF1E2628)
val SurfaceHighestDark = Color(0xFF283032)
val SurfaceDimDark = Color(0xFF080C0D)
val SurfaceBrightDark = Color(0xFF2A3436)
val SurfaceLowestDark = Color(0xFF0A0F10)

val InkDark = Color(0xFFE7EDEB)
val Ink2Dark = Color(0xFFB2BDBA)
val Ink3Dark = Color(0xFF8D9A96)

val RuleDark = Color(0xFF222C2E)
val RuleStrongDark = Color(0xFF33403F)

val PetrolDark = Color(0xFF6FC8BB)
val PetrolStrongDark = Color(0xFF8AD6CA)
val OnPetrolDark = Color(0xFF06201E)
val PetrolTintDark = Color(0xFF122E2B)

val FuelDark = Color(0xFFEDB25A)
val FuelTintDark = Color(0xFF2E2413)

val DangerDark = Color(0xFFEF9086)
val OnDangerDark = Color(0xFF4A0F0A)
val DangerTintDark = Color(0xFF331A17)
val OnDangerTintDark = Color(0xFFF4DEDB)

val GoodDark = Color(0xFF7CC79C)
val GoodTintDark = Color(0xFF142E20)

val SlateDark = Color(0xFFA9AFD6)
val OnSlateDark = Color(0xFF1E2240)
val SlateTintDark = Color(0xFF343A5E)
val OnSlateTintDark = Color(0xFFE2E4F0)

// --- Instrument (identical in both appearances) ---------------------------

val InstrumentSurface = Color(0xFF101618)
val InstrumentRaised = Color(0xFF182023)
val InstrumentRaisedHigh = Color(0xFF212B2E)
val InstrumentRule = Color(0xFF2B3639)
val InstrumentText = Color(0xFFE6EDEC)
val InstrumentTextMuted = Color(0xFF9BA8A6)
val InstrumentReadout = Color(0xFF57C0B2)
val InstrumentMarker = Color(0xFFF0A83C)
val InstrumentOnMarker = Color(0xFF1A1204)

/**
 * Semantic colors that Material 3 has no role for. Provided by
 * [MileLogTheme] so screens never hard-code a hex value.
 */
@Immutable
data class LedgerColors(
    val chrome: Color,
    val chromeRaised: Color,
    val chromeRaisedHigh: Color,
    val chromeRule: Color,
    val chromeText: Color,
    val chromeTextMuted: Color,
    val chromeReadout: Color,
    val chromeMarker: Color,
    val chromeOnMarker: Color,
    val fuel: Color,
    val fuelTint: Color,
    val good: Color,
    val goodTint: Color,
    val rule: Color,
    val ruleStrong: Color
)

val LightLedgerColors = LedgerColors(
    chrome = InstrumentSurface,
    chromeRaised = InstrumentRaised,
    chromeRaisedHigh = InstrumentRaisedHigh,
    chromeRule = InstrumentRule,
    chromeText = InstrumentText,
    chromeTextMuted = InstrumentTextMuted,
    chromeReadout = InstrumentReadout,
    chromeMarker = InstrumentMarker,
    chromeOnMarker = InstrumentOnMarker,
    fuel = FuelLight,
    fuelTint = FuelTintLight,
    good = GoodLight,
    goodTint = GoodTintLight,
    rule = RuleLight,
    ruleStrong = RuleStrongLight
)

val DarkLedgerColors = LightLedgerColors.copy(
    fuel = FuelDark,
    fuelTint = FuelTintDark,
    good = GoodDark,
    goodTint = GoodTintDark,
    rule = RuleDark,
    ruleStrong = RuleStrongDark
)

val LocalLedgerColors = staticCompositionLocalOf { LightLedgerColors }

/**
 * Semantic colors outside the Material scheme: the instrument roles (constant),
 * plus fuel, good, and the hairline rule tokens.
 */
val MaterialTheme.ledger: LedgerColors
    @Composable
    @ReadOnlyComposable
    get() = LocalLedgerColors.current
