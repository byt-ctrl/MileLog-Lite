package com.example.myapplication.data.local

import androidx.annotation.StringRes
import com.example.myapplication.R

/**
 * The appearance chosen in Settings.
 *
 * Only the logbook follows this; the instrument stays dark in every mode, so
 * the choice changes the paper and nothing else.
 *
 * @param storedValue Value written to preferences. Renaming an entry would
 *   silently reset every install that already used it.
 * @param labelRes Resource used for the visible label, so the radio group
 *   follows the user's locale.
 */
enum class ThemeMode(
    val storedValue: String,
    @StringRes val labelRes: Int
) {
    LIGHT("light", R.string.settings_theme_light),
    DARK("dark", R.string.settings_theme_dark),
    SYSTEM("system", R.string.settings_theme_system);

    /**
     * Resolves this choice against the device's own appearance.
     */
    fun isDark(systemInDarkTheme: Boolean): Boolean = when (this) {
        LIGHT -> false
        DARK -> true
        SYSTEM -> systemInDarkTheme
    }

    companion object {
        /** Applied when nothing has been chosen yet. */
        val DEFAULT: ThemeMode = SYSTEM

        /**
         * Resolves a stored value, falling back to [DEFAULT] when the value is
         * missing or unrecognised.
         */
        fun fromStored(value: String?): ThemeMode =
            entries.firstOrNull { it.storedValue == value } ?: DEFAULT
    }
}
