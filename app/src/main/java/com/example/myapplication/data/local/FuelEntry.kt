package com.example.myapplication.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a single fuel fill-up log.
 *
 * @property id Auto-generated unique primary key.
 * @property vehicleId Owning [Vehicle] ID. Defaults to 0 for rows predating
 *   multi-vehicle support; the schema migration reassigns those to a default vehicle.
 * @property date Epoch timestamp in milliseconds when the fuel fill-up occurred.
 * @property odometer Vehicle odometer reading in kilometers at fill-up.
 * @property liters Volume of fuel filled in liters.
 * @property cost Total cost of the fuel fill-up.
 * @property fuelCategory Fuel type category display name (e.g. "Petrol", "Diesel", "CNG").
 */
@Entity(
    tableName = "fuel_entries",
    indices = [
        Index(value = ["date"]),
        Index(value = ["odometer"]),
        Index(value = ["date", "odometer"]),
        Index(value = ["fuelCategory"]),
        Index(value = ["vehicleId"]),
        Index(value = ["vehicleId", "fuelCategory"])
    ]
)
data class FuelEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val vehicleId: Long = 0,
    val date: Long,
    val odometer: Int,
    val liters: Double,
    val cost: Double,
    val fuelCategory: String = "Petrol"
)
