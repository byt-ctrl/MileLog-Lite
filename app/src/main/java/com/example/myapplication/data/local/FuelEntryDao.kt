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
    @Query("SELECT * FROM fuel_entries ORDER BY odometer DESC LIMIT 1")
    suspend fun getLatest(): FuelEntry?

    /**
     * Retrieves the fuel entry with the highest odometer reading within a specific [FuelCategory].
     */
    @Query(
        "SELECT * FROM fuel_entries WHERE fuelCategory = :category " +
            "ORDER BY odometer DESC LIMIT 1"
    )
    suspend fun getLatestByCategory(category: FuelCategory): FuelEntry?

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
