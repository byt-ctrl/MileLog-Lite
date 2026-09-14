package com.example.myapplication.data.local

import com.example.myapplication.domain.conversion.DistanceUnit
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap

/**
 * Unit tests for the preference surface. The storage is faked so the tests run
 * on the JVM; the real SharedPreferences file is covered by
 * `UserPreferencesPersistenceTest` on a device.
 */
class UserPreferencesTest {

    private val storage = InMemoryPreferencesStorage()
    private val preferences = UserPreferences(storage)

    @Test
    fun defaults_areTheAppDefaultsWhenNothingIsStored() {
        assertEquals(ThemeMode.SYSTEM, preferences.readThemeMode())
        assertEquals(DistanceUnit.KILOMETERS, preferences.readDistanceUnit())
    }

    @Test
    fun writes_useTheDocumentedKeysAndValues() = runBlocking {
        preferences.writeThemeMode(ThemeMode.DARK)
        preferences.writeDistanceUnit(DistanceUnit.MILES)

        assertEquals("dark", storage.readString(UserPreferences.KEY_THEME_MODE, null))
        assertEquals("mi", storage.readString(UserPreferences.KEY_DISTANCE_UNIT, null))
    }

    @Test
    fun writes_areReadBackByANewInstance() = runBlocking {
        preferences.writeThemeMode(ThemeMode.LIGHT)
        preferences.writeDistanceUnit(DistanceUnit.MILES)

        val reopened = UserPreferences(storage)

        assertEquals(ThemeMode.LIGHT, reopened.readThemeMode())
        assertEquals(DistanceUnit.MILES, reopened.readDistanceUnit())
    }

    @Test
    fun unknownStoredValues_fallBackToTheDefaults() {
        storage.writeString(UserPreferences.KEY_THEME_MODE, "sepia")
        storage.writeString(UserPreferences.KEY_DISTANCE_UNIT, "furlongs")

        assertEquals(ThemeMode.SYSTEM, preferences.readThemeMode())
        assertEquals(DistanceUnit.KILOMETERS, preferences.readDistanceUnit())
    }

    @Test
    fun everyRoundTripValue_isStable() = runBlocking {
        ThemeMode.entries.forEach { mode ->
            preferences.writeThemeMode(mode)
            assertEquals(mode, preferences.readThemeMode())
        }
        DistanceUnit.entries.forEach { unit ->
            preferences.writeDistanceUnit(unit)
            assertEquals(unit, preferences.readDistanceUnit())
        }
    }
}

/** Preferences held in a map: same contract as the file, none of the Android. */
class InMemoryPreferencesStorage(
    private val values: MutableMap<String, String> = ConcurrentHashMap()
) : PreferencesStorage {

    override fun readString(key: String, defaultValue: String?): String? =
        values[key] ?: defaultValue

    override fun writeString(key: String, value: String) {
        values[key] = value
    }
}
