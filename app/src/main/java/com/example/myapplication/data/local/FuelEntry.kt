package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a single fuel fill-up log.
 *
 * @property id Auto-generated unique primary key.
 * @property vehicleId Owning [Vehicle] ID, or null when the row is not
 *   attributed to a vehicle. Enforced with `ON DELETE CASCADE`, so removing a
 *   vehicle removes its fill-ups.
 * @property date Epoch timestamp in milliseconds when the fuel fill-up occurred.
 * @property odometer Vehicle odometer reading in kilometers at fill-up.
 * @property liters Volume of fuel filled in liters.
 * @property cost Total cost of the fuel fill-up.
 * @property fuelCategory Fuel type category display name (e.g. "Petrol", "Diesel", "CNG").
 */
@Entity(
    tableName = "fuel_entries",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
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
    val vehicleId: Long? = null,
    val date: Long,
    val odometer: Int,
    val liters: Double,
    val cost: Double,
    val fuelCategory: String = "Petrol"
)
