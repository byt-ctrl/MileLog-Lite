package com.example.myapplication.data.repository

import com.example.myapplication.data.local.InMemoryPreferencesStorage
import com.example.myapplication.data.local.ThemeMode
import com.example.myapplication.data.local.UserPreferences
import com.example.myapplication.domain.conversion.DistanceUnit
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Read/write behaviour of the settings repository, including what the next
 * launch would see.
 */
class SettingsRepositoryTest {

    private val storage = InMemoryPreferencesStorage()

    private fun repository() = OfflineSettingsRepository(UserPreferences(storage))

    @Test
    fun flows_startAtTheStoredValues() {
        storage.writeString(UserPreferences.KEY_THEME_MODE, "dark")
        storage.writeString(UserPreferences.KEY_DISTANCE_UNIT, "mi")

        val repository = repository()

        assertEquals(ThemeMode.DARK, repository.themeMode.value)
        assertEquals(DistanceUnit.MILES, repository.distanceUnit.value)
    }

    @Test
    fun flows_startAtTheDefaultsWhenNothingIsStored() {
        val repository = repository()

        assertEquals(ThemeMode.DEFAULT, repository.themeMode.value)
        assertEquals(DistanceUnit.DEFAULT, repository.distanceUnit.value)
    }

    @Test
    fun setThemeMode_movesTheFlow() = runBlocking {
        val repository = repository()

        repository.setThemeMode(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, repository.themeMode.value)
    }

    @Test
    fun setDistanceUnit_movesTheFlow() = runBlocking {
        val repository = repository()

        repository.setDistanceUnit(DistanceUnit.MILES)

        assertEquals(DistanceUnit.MILES, repository.distanceUnit.value)
    }

    @Test
    fun aWrite_isVisibleToTheNextInstance() = runBlocking {
        repository().apply {
            setThemeMode(ThemeMode.DARK)
            setDistanceUnit(DistanceUnit.MILES)
        }

        // A fresh repository stands in for the next launch: it starts from
        // storage, so anything it reports came off the previous write.
        val next = repository()

        assertEquals(ThemeMode.DARK, next.themeMode.value)
        assertEquals(DistanceUnit.MILES, next.distanceUnit.value)
    }

    @Test
    fun eachPreference_isIndependent() = runBlocking {
        val repository = repository()

        repository.setDistanceUnit(DistanceUnit.MILES)

        assertEquals(DistanceUnit.MILES, repository.distanceUnit.value)
        assertEquals(ThemeMode.DEFAULT, repository.themeMode.value)
    }
}
