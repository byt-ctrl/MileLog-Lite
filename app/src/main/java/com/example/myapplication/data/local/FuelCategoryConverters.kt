package com.example.myapplication.data.local

import androidx.room.TypeConverter

/**
 * Room converters for [FuelCategory].
 *
 * The `fuelCategory` column stores the enum's [FuelCategory.displayName]
 * (for example `"Petrol"`), while category-filtered DAO queries bind a
 * [FuelCategory] parameter. Without an explicit converter Room falls back to
 * the enum's `name` (`"PETROL"`), so those queries never matched a stored row.
 * These converters keep both directions on the display name that is persisted.
 */
class FuelCategoryConverters {

    @TypeConverter
    fun fromFuelCategory(category: FuelCategory): String = category.displayName

    @TypeConverter
    fun toFuelCategory(value: String?): FuelCategory = FuelCategory.fromDisplayName(value)
}
