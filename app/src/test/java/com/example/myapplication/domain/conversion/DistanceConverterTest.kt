package com.example.myapplication.domain.conversion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Kilometre to mile conversion, and the formatting the screens read back.
 */
class DistanceConverterTest {

    private val tolerance = 0.0001

    @Test
    fun kmToMiles_convertsWithTheDocumentedFactor() {
        assertEquals(62.1371, DistanceConverter.kmToMiles(100.0), tolerance)
        assertEquals(0.0, DistanceConverter.kmToMiles(0.0), tolerance)
        assertEquals(0.621371, DistanceConverter.kmToMiles(1.0), tolerance)
    }

    @Test
    fun toKilometres_isTheInverseOfConvertDistance() {
        assertEquals(100.0, DistanceConverter.toKilometres(100.0, DistanceUnit.KILOMETERS), tolerance)
        assertEquals(
            100.0,
            DistanceConverter.toKilometres(
                DistanceConverter.convertDistance(100.0, DistanceUnit.MILES),
                DistanceUnit.MILES
            ),
            tolerance
        )
    }

    @Test
    fun aTypedOdometerReading_neverDriftsByMoreThanAKilometre() {
        val stored = listOf(1_000, 25_905, 36_388, 100_000, 250_000)
        stored.forEach { km ->
            val typed = DistanceConverter.convertDistance(km.toDouble(), DistanceUnit.MILES).roundToInt()
            val written = DistanceConverter.toKilometres(typed.toDouble(), DistanceUnit.MILES).roundToInt()
            assertTrue(
                "$km km drifted to $written, more than a kilometre",
                abs(written - km) <= 1
            )
        }
    }

    @Test
    fun theRoundTrip_isNotAlwaysExact() {
        // A mile is longer than a kilometre, so rounding to whole miles is not
        // always invertible. This is the reason the entry form writes a stored
        // reading back untouched when the odometer field was never edited
        // instead of converting what it shows.
        val typed = DistanceConverter.convertDistance(1_000.0, DistanceUnit.MILES).roundToInt()
        val written = DistanceConverter.toKilometres(typed.toDouble(), DistanceUnit.MILES).roundToInt()

        assertEquals(621, typed)
        assertEquals(999, written)
    }

    @Test
    fun convertDistance_leavesKilometresAlone() {
        assertEquals(12_345.0, DistanceConverter.convertDistance(12_345.0, DistanceUnit.KILOMETERS), tolerance)
    }

    @Test
    fun convertDistance_convertsForMiles() {
        assertEquals(
            62.1371,
            DistanceConverter.convertDistance(100.0, DistanceUnit.MILES),
            tolerance
        )
    }

    @Test
    fun convertMileage_changesOnlyTheDistanceHalfOfTheReading() {
        // 18 km/L is 11.18 mi/L: fuel stays litres in both units.
        assertEquals(18.0, DistanceConverter.convertMileage(18.0, DistanceUnit.KILOMETERS), tolerance)
        assertEquals(
            18.0 * DistanceConverter.MILES_PER_KM,
            DistanceConverter.convertMileage(18.0, DistanceUnit.MILES),
            tolerance
        )
    }

    @Test
    fun convertCostPerDistance_risesWhenTheUnitGetsLonger() {
        // A mile costs more than a kilometre, so the figure has to go up.
        val perKm = 8.0
        val perMile = DistanceConverter.convertCostPerDistance(perKm, DistanceUnit.MILES)

        assertEquals(perKm, DistanceConverter.convertCostPerDistance(perKm, DistanceUnit.KILOMETERS), tolerance)
        assertTrue(perMile > perKm)
        assertEquals(perKm / DistanceConverter.MILES_PER_KM, perMile, tolerance)
        // ...and converting back gives the original.
        assertEquals(perKm, perMile * DistanceConverter.MILES_PER_KM, tolerance)
    }

    @Test
    fun formatDistance_groupsTheConvertedValue() {
        val grouped100Km = NumberFormat.getIntegerInstance(Locale.getDefault()).format(100)
        val grouped62Mi = NumberFormat.getIntegerInstance(Locale.getDefault()).format(62)

        assertEquals(grouped100Km, DistanceConverter.formatDistance(100.0, DistanceUnit.KILOMETERS))
        assertEquals(grouped62Mi, DistanceConverter.formatDistance(100.0, DistanceUnit.MILES))
    }

    @Test
    fun formatDistance_roundsToWholeUnits() {
        // 1 km is 0.62 mi, which reads as 1 rather than 0.6.
        val expected = NumberFormat.getIntegerInstance(Locale.getDefault()).format(1)
        assertEquals(expected, DistanceConverter.formatDistance(1.0, DistanceUnit.MILES))
    }

    @Test
    fun formatDistance_carriesNoUnitSuffix() {
        val formatted = DistanceConverter.formatDistance(100.0, DistanceUnit.KILOMETERS)
        assertTrue(formatted.none { it.isLetter() })
    }

    @Test
    fun default_isKilometres() {
        assertEquals(DistanceUnit.KILOMETERS, DistanceUnit.DEFAULT)
    }

    @Test
    fun fromStored_resolvesEveryStoredValueAndFallsBack() {
        DistanceUnit.entries.forEach { unit ->
            assertEquals(unit, DistanceUnit.fromStored(unit.storedValue))
        }
        assertEquals(DistanceUnit.DEFAULT, DistanceUnit.fromStored(null))
        assertEquals(DistanceUnit.DEFAULT, DistanceUnit.fromStored("furlongs"))
    }
}
