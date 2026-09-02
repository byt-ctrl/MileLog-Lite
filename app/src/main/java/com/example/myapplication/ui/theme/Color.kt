package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Color

// --- MileLog Lite brand palette (dependable blue + business green + teal tertiary) ---
// Kinetic Logic design system. Light values come from the DESIGN.md tokens; dark values
// are derived from the inverse counterparts (inverseSurface #243145, inverseOnSurface
// #EBF1FF, inversePrimary #B2C5FF) per Sprint 6 §6.1.1.

// Primary: dependable blue
val BluePrimaryLight = Color(0xFF003D9B)
val BluePrimaryDark = Color(0xFFB2C5FF)
val OnBluePrimaryLight = Color(0xFFFFFFFF)
val OnBluePrimaryDark = Color(0xFF00215F)
val BlueContainerLight = Color(0xFF0052CC)
val BlueContainerDark = Color(0xFF0052CC)
val OnBlueContainerLight = Color(0xFFC4D2FF)
val OnBlueContainerDark = Color(0xFFC4D2FF)

// Primary fixed (identical across light and dark)
val PrimaryFixed = Color(0xFFDAE2FF)
val PrimaryFixedDim = Color(0xFFB2C5FF)
val OnPrimaryFixed = Color(0xFF001848)
val OnPrimaryFixedVariant = Color(0xFF0040A2)

// Secondary: business/success green
val GreenSecondaryLight = Color(0xFF006C47)
val GreenSecondaryDark = Color(0xFF65DCA4)
val OnGreenSecondaryLight = Color(0xFFFFFFFF)
val OnGreenSecondaryDark = Color(0xFF005235)
val GreenContainerLight = Color(0xFF82F9BE)
val GreenContainerDark = Color(0xFF005235)
val OnGreenContainerLight = Color(0xFF00734C)
val OnGreenContainerDark = Color(0xFF82F9BE)

// Secondary fixed (identical across light and dark)
val SecondaryFixed = Color(0xFF82F9BE)
val SecondaryFixedDim = Color(0xFF65DCA4)
val OnSecondaryFixed = Color(0xFF002113)
val OnSecondaryFixedVariant = Color(0xFF005235)

// Tertiary: interactive teal (secondary highlights, data-viz trends)
val TertiaryLight = Color(0xFF004B51)
val TertiaryDark = Color(0xFF4BD9E5)
val OnTertiaryLight = Color(0xFFFFFFFF)
val OnTertiaryDark = Color(0xFF002022)
val TertiaryContainerLight = Color(0xFF00656C)
val TertiaryContainerDark = Color(0xFF004F55)
val OnTertiaryContainerLight = Color(0xFF5BE6F2)
val OnTertiaryContainerDark = Color(0xFF7FF4FF)

// Tertiary fixed (identical across light and dark)
val TertiaryFixed = Color(0xFF7FF4FF)
val TertiaryFixedDim = Color(0xFF4BD9E5)
val OnTertiaryFixed = Color(0xFF002022)
val OnTertiaryFixedVariant = Color(0xFF004F55)

// Accents: trip classification & active status (Kinetic Logic)
val PersonalOrange = Color(0xFFFF8B00)
val BusinessGreen = Color(0xFF36B37E)
val ActiveStatusRed = Color(0xFFDE350B)

// Neutrals: blue-tinted surface ramp (never pure gray)
val SurfaceLight = Color(0xFFF9F9FF)
val SurfaceDark = Color(0xFF243145)
val SurfaceDimLight = Color(0xFFCDDBF5)
val SurfaceDimDark = Color(0xFF121824)
val SurfaceBrightLight = Color(0xFFF9F9FF)
val SurfaceBrightDark = Color(0xFF2C3B52)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowestDark = Color(0xFF161E2B)
val SurfaceContainerLowLight = Color(0xFFF0F3FF)
val SurfaceContainerLowDark = Color(0xFF1C2634)
val SurfaceContainerLight = Color(0xFFE7EEFF)
val SurfaceContainerDark = Color(0xFF212C3D)
val SurfaceContainerHighLight = Color(0xFFDEE8FF)
val SurfaceContainerHighDark = Color(0xFF2A3850)
val SurfaceContainerHighestLight = Color(0xFFD6E3FE)
val SurfaceContainerHighestDark = Color(0xFF304159)
val OnSurfaceLight = Color(0xFF0E1C2F)
val OnSurfaceDark = Color(0xFFEBF1FF)
val SurfaceVariantLight = Color(0xFFD6E3FE)
val SurfaceVariantDark = Color(0xFF434654)
val OnSurfaceVariantLight = Color(0xFF434654)
val OnSurfaceVariantDark = Color(0xFFC3C6D6)
val OutlineLight = Color(0xFF737685)
val OutlineDark = Color(0xFF8D92A5)
val OutlineVariantLight = Color(0xFFC3C6D6)
val OutlineVariantDark = Color(0xFF434654)
val SurfaceTintLight = Color(0xFF0C56D0)
val SurfaceTintDark = Color(0xFFB2C5FF)

// Background: application canvas (surface-bg token, elevation Level 0)
val BackgroundLight = Color(0xFFF4F5F7)
val BackgroundDark = Color(0xFF1A2330)

// Inverse roles (snackbars/toasts and flipped-surface content)
val InverseSurfaceLight = Color(0xFF243145)
val InverseSurfaceDark = Color(0xFFEBF1FF)
val InverseOnSurfaceLight = Color(0xFFEBF1FF)
val InverseOnSurfaceDark = Color(0xFF243145)
val InversePrimaryLight = Color(0xFFB2C5FF)
val InversePrimaryDark = Color(0xFF003D9B)

// Error
val ErrorLight = Color(0xFFBA1A1A)
val ErrorDark = Color(0xFFFFB4AB)
val OnErrorLight = Color(0xFFFFFFFF)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerLight = Color(0xFFFFDAD6)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerLight = Color(0xFF410002)
val OnErrorContainerDark = Color(0xFFFFDAD6)
