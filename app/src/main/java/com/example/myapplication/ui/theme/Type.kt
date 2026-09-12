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
 * Data face. Every number the driver compares (odometer, litres, km/L, cost,
 * gauge readouts, chart labels) is set in the platform monospace with tabular
 * figures, so digits take a fixed advance and columns line up down the ledger.
 *
 * This is a functional choice, not a technical costume: those numbers are the
 * product, and a proportional face leaves them ragged.
 */
val DataMono = FontFamily.Monospace

private const val TABULAR = "tnum"

/**
 * Instrument Ledger type scale.
 *
 * Two faces, one job each. [DataMono] carries readings and anything that gets
 * compared down a column. [Inter] carries prose, labels, and controls. Weight
 * and size carry the hierarchy; the scale steps at roughly 1.3x.
 */
val Typography = Typography(
    // Readings and hero metrics.
    displayLarge = TextStyle(
        fontFamily = DataMono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = TABULAR
    ),
    displayMedium = TextStyle(
        fontFamily = DataMono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.4).sp,
        fontFeatureSettings = TABULAR
    ),
    displaySmall = TextStyle(
        fontFamily = DataMono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp,
        fontFeatureSettings = TABULAR
    ),

    // Screen titles and section breaks.
    headlineLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),

    // Row and card titles.
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),

    // Prose, form values, descriptions.
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),

    // Metadata, captions, secondary actions.
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.05.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.08.sp
    )
)

/** Ledger row data: mono, tabular, aligned. */
val DataTextStyle = TextStyle(
    fontFamily = DataMono,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    fontFeatureSettings = TABULAR
)

/** Compact data: chart caps, secondary readings. */
val DataTextStyleSmall = TextStyle(
    fontFamily = DataMono,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    fontFeatureSettings = TABULAR
)

/** Uppercase micro-label: readout labels, table headers, section notes. */
val MicroLabelStyle = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.08.sp
)
