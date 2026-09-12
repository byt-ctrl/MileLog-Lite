package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a vehicle the user logs fill-ups against.
 *
 * @property id Auto-generated unique primary key.
 * @property name Display name, for example "Hyundai Creta". Unique across vehicles.
 * @property make Manufacturer, for example "Hyundai".
 * @property model Model line, for example "Creta".
 * @property registrationNumber Optional registration plate.
 * @property fuelType Default fuel category display name (e.g. "Diesel", "CNG").
 * @property isActive True for the single vehicle currently selected in the app.
 */
@Entity(
    tableName = "vehicles",
    indices = [Index(value = ["name"], unique = true)]
)
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val make: String = "",
    val model: String = "",
    val registrationNumber: String = "",
    val fuelType: String = FuelCategory.DEFAULT.displayName,
    val isActive: Boolean = false
)
