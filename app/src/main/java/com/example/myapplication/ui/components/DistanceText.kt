package com.example.myapplication.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.example.myapplication.R
import com.example.myapplication.domain.conversion.DistanceConverter
import com.example.myapplication.domain.conversion.DistanceUnit

/**
 * Unit-aware copy for the instrument and the logbook.
 *
 * Storage is always kilometres, so every one of these takes the stored value
 * and converts it on the way to the screen. Nothing else in the UI layer
 * multiplies by a conversion factor.
 */

/** The symbol distances are shown in, for example `km`. */
@Composable
@ReadOnlyComposable
fun DistanceUnit.distanceLabel(): String = stringResource(labelRes)

/**
 * The mileage unit, for example `km/L`. Fuel is always litres, so only the
 * distance half of the reading changes with the unit.
 */
@Composable
@ReadOnlyComposable
fun DistanceUnit.mileageLabel(): String = stringResource(
    if (this == DistanceUnit.MILES) {
        R.string.unit_miles_per_litre
    } else {
        R.string.unit_kilometres_per_litre
    }
)

/** A value and its unit, for example `12,345 km`. */
@Composable
@ReadOnlyComposable
fun withUnit(value: String, unit: String): String =
    stringResource(R.string.value_with_unit, value, unit)

/** A stored kilometre distance, converted, grouped and labelled. */
@Composable
@ReadOnlyComposable
fun formatDistanceWithUnit(km: Double, unit: DistanceUnit): String =
    withUnit(DistanceConverter.formatDistance(km, unit), unit.distanceLabel())

/** A stored km/L reading, converted and labelled to one decimal. */
@Composable
@ReadOnlyComposable
fun formatMileageWithUnit(kmPerLitre: Double, unit: DistanceUnit): String =
    withUnit(formatOne(DistanceConverter.convertMileage(kmPerLitre, unit)), unit.mileageLabel())
