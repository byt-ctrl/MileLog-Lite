package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for [FuelEntry] database operations.
 *
 * The unscoped (non-vehicle) queries are legacy and have no production caller:
 * every screen reads through the `*ForVehicle` variants. They are retained for
 * the DAO tests and are marked deprecated on the repository surface that app
 * code actually uses.
 */
@Dao
interface FuelEntryDao {

    /**
     * Observes all fuel entries ordered most-recent first (date DESC, odometer DESC, id DESC).
     */
    @Query("SELECT * FROM fuel_entries ORDER BY date DESC, odometer DESC, id DESC")
    fun getAllFlow(): Flow<List<FuelEntry>>

    /**
     * Observes fuel entries ordered most-recent first, optionally filtered by [FuelCategory].
     * Pass `null` to observe all entries regardless of category.
     */
    @Query(
        "SELECT * FROM fuel_entries " +
            "WHERE (:category IS NULL OR fuelCategory = :category) " +
            "ORDER BY date DESC, odometer DESC, id DESC"
    )
    fun getAllFlow(category: FuelCategory?): Flow<List<FuelEntry>>

    /**
     * Retrieves all fuel entries ordered most-recent first.
     */
    @Query("SELECT * FROM fuel_entries ORDER BY date DESC, odometer DESC, id DESC")
    suspend fun getAll(): List<FuelEntry>

    /**
     * Retrieves fuel entries ordered most-recent first, optionally filtered by [FuelCategory].
     * Pass `null` to retrieve all entries regardless of category.
     */
    @Query(
        "SELECT * FROM fuel_entries " +
            "WHERE (:category IS NULL OR fuelCategory = :category) " +
            "ORDER BY date DESC, odometer DESC, id DESC"
    )
    suspend fun getAll(category: FuelCategory?): List<FuelEntry>

    /**
     * Retrieves a single fuel entry by its unique ID.
     */
    @Query("SELECT * FROM fuel_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FuelEntry?

    /**
     * Retrieves the fuel entry with the highest odometer reading.
     */
    @Query("SELECT * FROM fuel_entries ORDER BY odometer DESC, id DESC LIMIT 1")
    suspend fun getLatest(): FuelEntry?

    /**
     * Retrieves the fuel entry with the highest odometer reading within a specific [FuelCategory].
     */
    @Query(
        "SELECT * FROM fuel_entries WHERE fuelCategory = :category " +
            "ORDER BY odometer DESC, id DESC LIMIT 1"
    )
    suspend fun getLatestByCategory(category: FuelCategory): FuelEntry?

    /**
     * Observes every fuel entry belonging to [vehicleId], most-recent first.
     */
    @Query(
        "SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId " +
            "ORDER BY date DESC, odometer DESC, id DESC"
    )
    fun getAllFlowForVehicle(vehicleId: Long): Flow<List<FuelEntry>>

    /**
     * Observes fuel entries for [vehicleId], optionally filtered by [category].
     * Pass `null` to observe every category for the vehicle.
     */
    @Query(
        "SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId " +
            "AND (:category IS NULL OR fuelCategory = :category) " +
            "ORDER BY date DESC, odometer DESC, id DESC"
    )
    fun getAllFlowForVehicle(vehicleId: Long, category: FuelCategory?): Flow<List<FuelEntry>>

    /**
     * Retrieves every fuel entry belonging to [vehicleId], most-recent first.
     */
    @Query(
        "SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId " +
            "ORDER BY date DESC, odometer DESC, id DESC"
    )
    suspend fun getAllForVehicle(vehicleId: Long): List<FuelEntry>

    /**
     * Retrieves the highest-odometer entry for [vehicleId], the monotonic baseline.
     */
    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY odometer DESC, id DESC LIMIT 1")
    suspend fun getLatestForVehicle(vehicleId: Long): FuelEntry?

    /**
     * Deletes every fuel entry belonging to [vehicleId] in one statement.
     */
    @Query("DELETE FROM fuel_entries WHERE vehicleId = :vehicleId")
    suspend fun deleteByVehicle(vehicleId: Long)

    /**
     * Inserts a new fuel entry, returning the auto-generated ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FuelEntry): Long

    /**
     * Inserts multiple fuel entries in a single transaction, returning their auto-generated IDs.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<FuelEntry>): List<Long>

    /**
     * Updates an existing fuel entry.
     */
    @Update
    suspend fun update(entry: FuelEntry)

    /**
     * Deletes a fuel entry.
     */
    @Delete
    suspend fun delete(entry: FuelEntry)
}
