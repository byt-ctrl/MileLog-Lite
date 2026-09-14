package com.example.myapplication.ui.entry

import com.example.myapplication.domain.conversion.DistanceConverter
import com.example.myapplication.domain.conversion.DistanceUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.roundToInt

/**
 * The odometer field is typed in the unit the user reads in and stored in
 * kilometres, so this is the conversion between the two.
 */
class OdometerToStoreKmTest {

    private val storedKm = 1_000
    private val storedText = DistanceConverter
        .convertDistance(storedKm.toDouble(), DistanceUnit.MILES)
        .roundToInt()
        .toString()

    @Test
    fun anEditedReading_isConvertedToKilometres() {
        assertEquals(
            80_467,
            odometerToStoreKm("50000", DistanceUnit.MILES)
        )
        assertEquals(50_000, odometerToStoreKm("50000", DistanceUnit.KILOMETERS))
    }

    @Test
    fun kilometresNeedNoConversion() {
        assertEquals(25_905, odometerToStoreKm("25905", DistanceUnit.KILOMETERS))
    }

    @Test
    fun anUntouchedReading_keepsTheValueItWasLoadedWith() {
        // 1,000 km renders as 621 mi, and 621 mi converts back to 999 km. The
        // form must not rewrite the reading just because it was opened.
        assertEquals(621, storedText.toInt())
        assertEquals(
            storedKm,
            odometerToStoreKm(storedText, DistanceUnit.MILES, storedKm, storedText)
        )
    }

    @Test
    fun anEditedReading_isNotMaskedByTheStoredValue() {
        assertEquals(
            1_001,
            odometerToStoreKm("622", DistanceUnit.MILES, storedKm, storedText)
        )
    }

    @Test
    fun aBlankOrUnreadableReading_isNull() {
        assertNull(odometerToStoreKm("", DistanceUnit.MILES))
        assertNull(odometerToStoreKm("   ", DistanceUnit.KILOMETERS))
        assertNull(odometerToStoreKm("twelve", DistanceUnit.MILES))
    }

    @Test
    fun surroundingWhitespace_doesNotHideAnUneditedField() {
        assertEquals(
            storedKm,
            odometerToStoreKm("  $storedText  ", DistanceUnit.MILES, storedKm, storedText)
        )
    }
}
