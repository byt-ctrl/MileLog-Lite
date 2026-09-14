package com.example.myapplication.data.repository

import com.example.myapplication.data.local.ThemeMode
import com.example.myapplication.data.local.UserPreferences
import com.example.myapplication.domain.conversion.DistanceUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The two preferences that persist across launches: the appearance the logbook
 * follows and the unit every distance is printed in.
 */
interface SettingsRepository {

    /** The appearance the logbook follows. */
    val themeMode: StateFlow<ThemeMode>

    /** The unit distances are printed in. Storage stays kilometres. */
    val distanceUnit: StateFlow<DistanceUnit>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setDistanceUnit(unit: DistanceUnit)
}

/**
 * [SettingsRepository] backed by [UserPreferences].
 *
 * The state flows are seeded from disk and only moved by the setters, so the
 * whole app reads one value per preference. Writes reach disk before the flow
 * changes: a flow that moved first would announce a setting the next launch
 * would not find.
 */
class OfflineSettingsRepository(
    private val preferences: UserPreferences
) : SettingsRepository {

    private val _themeMode = MutableStateFlow(preferences.readThemeMode())
    private val _distanceUnit = MutableStateFlow(preferences.readDistanceUnit())

    override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    override val distanceUnit: StateFlow<DistanceUnit> = _distanceUnit.asStateFlow()

    override suspend fun setThemeMode(mode: ThemeMode) {
        preferences.writeThemeMode(mode)
        _themeMode.value = mode
    }

    override suspend fun setDistanceUnit(unit: DistanceUnit) {
        preferences.writeDistanceUnit(unit)
        _distanceUnit.value = unit
    }
}
