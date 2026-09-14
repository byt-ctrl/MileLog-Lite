package com.example.myapplication.data.local

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.data.repository.OfflineSettingsRepository
import com.example.myapplication.domain.conversion.DistanceUnit
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * The preference file itself: that a choice survives the process that made it,
 * and that it is on disk rather than only in memory.
 *
 * The first half of each case builds a repository, changes a setting and
 * throws it away. The second half builds a brand new one from the same context
 * - nothing is carried over in memory, so everything it reports it had to read
 * back - and the raw XML is checked as well, because a `SharedPreferences`
 * instance is cached per process and could otherwise be reporting an in-memory
 * map rather than the file.
 */
@RunWith(AndroidJUnit4::class)
class UserPreferencesPersistenceTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val preferencesFile: File
        get() = File(context.dataDir, "shared_prefs/${UserPreferences.PREFERENCES_NAME}.xml")

    @Before
    fun clearBefore() = clearPreferences()

    @After
    fun clearAfter() = clearPreferences()

    private fun clearPreferences() {
        context.getSharedPreferences(UserPreferences.PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    private fun restart(): OfflineSettingsRepository =
        OfflineSettingsRepository(UserPreferences(SharedPreferencesStorage(context)))

    @Test
    fun aFreshInstall_startsAtTheDefaults() {
        val repository = restart()

        assertEquals(ThemeMode.SYSTEM, repository.themeMode.value)
        assertEquals(DistanceUnit.KILOMETERS, repository.distanceUnit.value)
    }

    @Test
    fun bothPreferences_surviveARestart() = runBlocking {
        val before = restart()
        before.setThemeMode(ThemeMode.DARK)
        before.setDistanceUnit(DistanceUnit.MILES)

        val after = restart()

        assertEquals(ThemeMode.DARK, after.themeMode.value)
        assertEquals(DistanceUnit.MILES, after.distanceUnit.value)
    }

    @Test
    fun everyThemeMode_survivesARestart() = runBlocking {
        ThemeMode.entries.forEach { mode ->
            restart().setThemeMode(mode)
            assertEquals(mode, restart().themeMode.value)
        }
    }

    @Test
    fun everyDistanceUnit_survivesARestart() = runBlocking {
        DistanceUnit.entries.forEach { unit ->
            restart().setDistanceUnit(unit)
            assertEquals(unit, restart().distanceUnit.value)
        }
    }

    @Test
    fun aWrittenPreference_reachesTheFileOnDisk() = runBlocking {
        restart().setThemeMode(ThemeMode.LIGHT)
        restart().setDistanceUnit(DistanceUnit.MILES)

        assertTrue("preferences file was never written", preferencesFile.exists())

        val xml = preferencesFile.readText()

        assertTrue(
            "theme_mode was not persisted, file was: $xml",
            xml.contains("name=\"${UserPreferences.KEY_THEME_MODE}\"") &&
                xml.contains(">${ThemeMode.LIGHT.storedValue}<")
        )
        assertTrue(
            "distance_unit was not persisted, file was: $xml",
            xml.contains("name=\"${UserPreferences.KEY_DISTANCE_UNIT}\"") &&
                xml.contains(">${DistanceUnit.MILES.storedValue}<")
        )
    }

    @Test
    fun changingBack_isAlsoPersisted() = runBlocking {
        restart().apply {
            setThemeMode(ThemeMode.DARK)
            setDistanceUnit(DistanceUnit.MILES)
        }
        restart().apply {
            setThemeMode(ThemeMode.SYSTEM)
            setDistanceUnit(DistanceUnit.KILOMETERS)
        }

        val after = restart()

        assertEquals(ThemeMode.SYSTEM, after.themeMode.value)
        assertEquals(DistanceUnit.KILOMETERS, after.distanceUnit.value)
    }
}
