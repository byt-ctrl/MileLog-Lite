package com.example.myapplication.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The appearance preference: what each stored value means, and what happens to
 * a value the app does not recognise.
 */
class ThemeModeTest {

    @Test
    fun storedValues_areTheOnesTheSpecNames() {
        assertEquals("light", ThemeMode.LIGHT.storedValue)
        assertEquals("dark", ThemeMode.DARK.storedValue)
        assertEquals("system", ThemeMode.SYSTEM.storedValue)
    }

    @Test
    fun default_isSystem() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.DEFAULT)
    }

    @Test
    fun light_isNeverDark_whateverTheDeviceSays() {
        assertFalse(ThemeMode.LIGHT.isDark(systemInDarkTheme = false))
        assertFalse(ThemeMode.LIGHT.isDark(systemInDarkTheme = true))
    }

    @Test
    fun dark_isAlwaysDark_whateverTheDeviceSays() {
        assertTrue(ThemeMode.DARK.isDark(systemInDarkTheme = false))
        assertTrue(ThemeMode.DARK.isDark(systemInDarkTheme = true))
    }

    @Test
    fun system_followsTheDevice() {
        assertFalse(ThemeMode.SYSTEM.isDark(systemInDarkTheme = false))
        assertTrue(ThemeMode.SYSTEM.isDark(systemInDarkTheme = true))
    }

    @Test
    fun fromStored_resolvesEveryStoredValue() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(mode, ThemeMode.fromStored(mode.storedValue))
        }
    }

    @Test
    fun fromStored_fallsBackToTheDefaultForAnythingElse() {
        assertEquals(ThemeMode.DEFAULT, ThemeMode.fromStored(null))
        assertEquals(ThemeMode.DEFAULT, ThemeMode.fromStored(""))
        assertEquals(ThemeMode.DEFAULT, ThemeMode.fromStored("SYSTEM"))
        assertEquals(ThemeMode.DEFAULT, ThemeMode.fromStored("sepia"))
    }
}
