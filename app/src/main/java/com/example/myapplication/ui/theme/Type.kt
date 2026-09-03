package com.example.myapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

/**
 * MileLog Lite type system (Kinetic Logic).
 *
 * Hierarchy:
 *   Display  — dashboard KPI values (odometer, cost, mileage)
 *   Title    — screen headers, card titles, section labels
 *   Body     — form text, history rows, descriptions, validation
 *   Label    — chart captions, axis text, metadata, timestamps
 *
 * Single family (Inter) in four weights keeps the surface fast and
 * consistent. Weight contrast does the hierarchy work, not competing
 * typefaces. displayLarge/Medium use tabular figures (tnum) so KPI
 * numbers align vertically.
 */
val Typography = Typography(
    // ── Display: KPI hero values ──────────────────────────
    displayLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.96).sp,
        fontFeatureSettings = "tnum"
    ),
    displayMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.72).sp,
        fontFeatureSettings = "tnum"
    ),
    displaySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),

    // ── Headline: major section breaks ────────────────────
    headlineLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),

    // ── Title: screen headers, card titles ────────────────
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // ── Body: forms, history rows, descriptions ───────────
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // ── Label: chart captions, axis text, metadata ────────
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.55.sp
    )
)
