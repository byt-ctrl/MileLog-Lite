package com.example.myapplication.domain.conversion

import androidx.annotation.StringRes
import com.example.myapplication.R

/**
 * Unit every distance is printed in.
 *
 * Storage stays in kilometres whatever this is; the unit only decides what the
 * screens show.
 *
 * @param storedValue Value written to preferences.
 * @param labelRes Resource holding the unit's symbol, for example `km`.
 */
enum class DistanceUnit(
    val storedValue: String,
    @StringRes val labelRes: Int
) {
    KILOMETERS("km", R.string.unit_kilometres),
    MILES("mi", R.string.unit_miles);

    companion object {
        /** Applied when nothing has been chosen yet. */
        val DEFAULT: DistanceUnit = KILOMETERS

        /**
         * Resolves a stored value, falling back to [DEFAULT] when the value is
         * missing or unrecognised.
         */
        fun fromStored(value: String?): DistanceUnit =
            entries.firstOrNull { it.storedValue == value } ?: DEFAULT
    }
}
