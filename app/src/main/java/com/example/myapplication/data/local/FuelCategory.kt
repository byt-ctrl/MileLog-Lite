package com.example.myapplication.data.local

import androidx.annotation.StringRes
import com.example.myapplication.R

/**
 * Fuel type category for a [FuelEntry] fill-up.
 *
 * @param displayName English display name used for persistence and lookups
 *   against pre-localization records. New UI must resolve [labelRes] via
 *   `stringResource()` so the displayed name follows the user's locale.
 */
enum class FuelCategory(
    val displayName: String,
    @StringRes val labelRes: Int
) {
    PETROL("Petrol", R.string.fuel_category_petrol),
    DIESEL("Diesel", R.string.fuel_category_diesel),
    CNG("CNG", R.string.fuel_category_cng);

    companion object {
        /**
         * Default category assigned to legacy entries and to new entries
         * when the user has not made a selection.
         */
        val DEFAULT: FuelCategory = PETROL

        /**
         * Resolves a [FuelCategory] from its persisted [displayName], falling
         * back to [DEFAULT] for unknown or blank values.
         */
        fun fromDisplayName(value: String?): FuelCategory =
            entries.firstOrNull { it.displayName == value } ?: DEFAULT
    }
}