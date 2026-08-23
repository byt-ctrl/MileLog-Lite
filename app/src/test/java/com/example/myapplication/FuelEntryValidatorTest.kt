package com.example.myapplication

import com.example.myapplication.domain.validation.FuelEntryValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FuelEntryValidatorTest {

    @Test
    fun `valid inputs return valid result`() {
        val result = FuelEntryValidator.validate(
            dateMillis = System.currentTimeMillis(),
            odometerStr = "12500",
            litersStr = "35.5",
            costStr = "3500.0",
            previousOdometer = 12000
        )
        assertTrue(result.isValid)
        assertNull(result.dateError)
        assertNull(result.odometerError)
        assertNull(result.litersError)
        assertNull(result.costError)
    }

    @Test
    fun `empty inputs return errors`() {
        val result = FuelEntryValidator.validate(
            dateMillis = null,
            odometerStr = "",
            litersStr = "",
            costStr = ""
        )
        assertFalse(result.isValid)
        assertNotNull(result.dateError)
        assertNotNull(result.odometerError)
        assertNotNull(result.litersError)
        assertNotNull(result.costError)
    }

    @Test
    fun `odometer less than or equal to previous returns error`() {
        val result = FuelEntryValidator.validate(
            dateMillis = System.currentTimeMillis(),
            odometerStr = "12000",
            litersStr = "30.0",
            costStr = "3000.0",
            previousOdometer = 12000
        )
        assertFalse(result.isValid)
        assertEquals("Odometer must be greater than previous reading (12000 km)", result.odometerError)
    }

    @Test
    fun `negative and zero values return errors`() {
        val result = FuelEntryValidator.validate(
            dateMillis = System.currentTimeMillis(),
            odometerStr = "-50",
            litersStr = "0",
            costStr = "-100"
        )
        assertFalse(result.isValid)
        assertNotNull(result.odometerError)
        assertNotNull(result.litersError)
        assertNotNull(result.costError)
    }
}
