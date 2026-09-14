package com.example.myapplication.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.myapplication.domain.conversion.DistanceUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The narrow slice of preference storage this app writes.
 *
 * Keeping it an interface is what lets the settings logic be exercised on the
 * JVM, where the Android implementation cannot run.
 */
interface PreferencesStorage {

    fun readString(key: String, defaultValue: String?): String?

    fun writeString(key: String, value: String)
}

/** [PreferencesStorage] backed by the app's private preferences file. */
class SharedPreferencesStorage(context: Context) : PreferencesStorage {

    private val preferences: SharedPreferences = context.applicationContext
        .getSharedPreferences(UserPreferences.PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun readString(key: String, defaultValue: String?): String? =
        preferences.getString(key, defaultValue)

    /**
     * Writes immediately rather than in the background: the value has to be on
     * disk before this returns, so a preference is never reported as saved while
     * the next launch would still read the old one.
     *
     * `commit` is the point, not an oversight. The write is only ever reached
     * from `UserPreferences`, which runs it on `Dispatchers.IO`, so it never
     * blocks the main thread.
     */
    @SuppressLint("ApplySharedPref")
    override fun writeString(key: String, value: String) {
        preferences.edit(commit = true) { putString(key, value) }
    }
}

/**
 * The preferences that outlive a launch, read and written through a typed
 * surface so no caller has to know a key or invent a default.
 */
class UserPreferences(private val storage: PreferencesStorage) {

    fun readThemeMode(): ThemeMode = ThemeMode.fromStored(
        storage.readString(KEY_THEME_MODE, ThemeMode.DEFAULT.storedValue)
    )

    fun readDistanceUnit(): DistanceUnit = DistanceUnit.fromStored(
        storage.readString(KEY_DISTANCE_UNIT, DistanceUnit.DEFAULT.storedValue)
    )

    suspend fun writeThemeMode(mode: ThemeMode) {
        withContext(Dispatchers.IO) {
            storage.writeString(KEY_THEME_MODE, mode.storedValue)
        }
    }

    suspend fun writeDistanceUnit(unit: DistanceUnit) {
        withContext(Dispatchers.IO) {
            storage.writeString(KEY_DISTANCE_UNIT, unit.storedValue)
        }
    }

    companion object {
        const val PREFERENCES_NAME: String = "milelog_prefs"
        const val KEY_THEME_MODE: String = "theme_mode"
        const val KEY_DISTANCE_UNIT: String = "distance_unit"
    }
}
