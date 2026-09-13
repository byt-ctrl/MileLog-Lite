package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for [Vehicle] database operations.
 */
@Dao
interface VehicleDao {

    /**
     * Observes every vehicle, active first, then by name.
     */
    @Query("SELECT * FROM vehicles ORDER BY isActive DESC, name ASC")
    fun getAllFlow(): Flow<List<Vehicle>>

    /**
     * Retrieves every vehicle, active first, then by name.
     */
    @Query("SELECT * FROM vehicles ORDER BY isActive DESC, name ASC")
    suspend fun getAll(): List<Vehicle>

    /**
     * Observes the single active vehicle, or null when none is selected.
     */
    @Query("SELECT * FROM vehicles WHERE isActive = 1 LIMIT 1")
    fun getActiveFlow(): Flow<Vehicle?>

    /**
     * Retrieves the single active vehicle, or null when none is selected.
     */
    @Query("SELECT * FROM vehicles WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): Vehicle?

    /**
     * Retrieves a vehicle by its unique ID.
     */
    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Vehicle?

    /**
     * Retrieves a vehicle by its unique display name.
     */
    @Query("SELECT * FROM vehicles WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Vehicle?

    /**
     * Inserts a new vehicle, returning the auto-generated ID.
     *
     * The conflict strategy is `ABORT`, so a duplicate name fails loudly
     * instead of silently replacing the existing vehicle (and dropping its
     * active flag). `VehicleValidator` rejects duplicates before this is called.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vehicle: Vehicle): Long

    /**
     * Updates an existing vehicle.
     */
    @Update
    suspend fun update(vehicle: Vehicle)

    /**
     * Deletes a vehicle.
     */
    @Delete
    suspend fun delete(vehicle: Vehicle)

    /**
     * Clears the active flag from every vehicle.
     */
    @Query("UPDATE vehicles SET isActive = 0")
    suspend fun clearActive()

    /**
     * Marks a single vehicle as active.
     */
    @Query("UPDATE vehicles SET isActive = 1 WHERE id = :id")
    suspend fun markActive(id: Long)

    /**
     * Selects exactly one active vehicle, clearing the flag from the rest.
     */
    @Transaction
    suspend fun setActive(id: Long) {
        clearActive()
        markActive(id)
    }
}
