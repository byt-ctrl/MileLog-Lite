package com.example.myapplication.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleValidatorTest {

    @Test
    fun `a unique non blank name is valid`() {
        val result = VehicleValidator.validate("Hyundai Creta", listOf("Kia Seltos"))

        assertTrue(result.isValid)
        assertNull(result.nameError)
    }

    @Test
    fun `a blank name is rejected`() {
        val result = VehicleValidator.validate("   ", emptyList())

        assertFalse(result.isValid)
        assertEquals(VehicleFieldError.NAME_REQUIRED, result.nameError)
    }

    @Test
    fun `an empty name is rejected`() {
        val result = VehicleValidator.validate("", emptyList())

        assertFalse(result.isValid)
        assertEquals(VehicleFieldError.NAME_REQUIRED, result.nameError)
    }

    @Test
    fun `a duplicate name is rejected regardless of case`() {
        val result = VehicleValidator.validate("hyundai creta", listOf("Hyundai Creta"))

        assertFalse(result.isValid)
        assertEquals(VehicleFieldError.NAME_DUPLICATE, result.nameError)
    }

    @Test
    fun `surrounding whitespace is ignored when checking for duplicates`() {
        val result = VehicleValidator.validate("  Hyundai Creta  ", listOf("Hyundai Creta"))

        assertFalse(result.isValid)
        assertEquals(VehicleFieldError.NAME_DUPLICATE, result.nameError)
    }

    @Test
    fun `an edited vehicle does not collide with itself`() {
        // The caller excludes the vehicle being edited, so its own name never
        // appears in otherVehicleNames.
        val result = VehicleValidator.validate("Hyundai Creta", listOf("Kia Seltos"))

        assertTrue(result.isValid)
    }

    @Test
    fun `no other vehicles means no duplicate`() {
        val result = VehicleValidator.validate("My vehicle", emptyList())

        assertTrue(result.isValid)
    }
}
