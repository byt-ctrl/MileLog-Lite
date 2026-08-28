package com.example.myapplication.data.local

/**
 * Fuel type category for a [FuelEntry] fill-up.
 */
enum class FuelCategory(val displayName: String) {
    PETROL("Petrol"),
    DIESEL("Diesel"),
    CNG("CNG");

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
