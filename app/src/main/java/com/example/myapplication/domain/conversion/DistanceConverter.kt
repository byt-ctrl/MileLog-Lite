package com.example.myapplication.domain.conversion

import java.text.NumberFormat
import java.util.Locale

/**
 * Kilometre to mile conversion for display.
 *
 * Every distance in the database and in every calculation is kilometres. This
 * is the single place a value changes unit, so no screen and no column has to
 * know which unit the user reads in.
 */
object DistanceConverter {

    /** Miles in one kilometre. */
    const val MILES_PER_KM: Double = 0.621371

    fun kmToMiles(km: Double): Double = km * MILES_PER_KM

    /** A kilometre distance expressed in [unit]. */
    fun convertDistance(km: Double, unit: DistanceUnit): Double =
        if (unit == DistanceUnit.MILES) kmToMiles(km) else km

    /**
     * A value entered in [unit], expressed as stored kilometres. The inverse of
     * [convertDistance], so a typed odometer reading can be persisted.
     */
    fun toKilometres(value: Double, unit: DistanceUnit): Double =
        if (unit == DistanceUnit.MILES) value / MILES_PER_KM else value

    /**
     * A km/L mileage reading expressed in [unit] per litre. Fuel is always
     * litres, so only the distance half of the reading changes unit.
     */
    fun convertMileage(kmPerLitre: Double, unit: DistanceUnit): Double =
        convertDistance(kmPerLitre, unit)

    /** A cost-per-kilometre figure expressed as cost per [unit]. */
    fun convertCostPerDistance(costPerKm: Double, unit: DistanceUnit): Double =
        if (unit == DistanceUnit.MILES) costPerKm / MILES_PER_KM else costPerKm

    /**
     * Formats [km] in [unit] with the locale's group separators, for example
     * `7,670`. The unit label is appended by the caller so the copy stays in
     * the string resources.
     */
    fun formatDistance(km: Double, unit: DistanceUnit): String =
        NumberFormat.getIntegerInstance(Locale.getDefault())
            .format(convertDistance(km, unit))
}
