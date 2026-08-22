package com.example.myapplication.data.repository

import com.example.myapplication.data.local.FuelEntry
import com.example.myapplication.data.local.FuelEntryDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing [FuelEntry] data operations.
 */
interface FuelEntryRepository {

    /**
     * Observes all fuel entries ordered most-recent first.
     */
    fun getAllEntriesFlow(): Flow<List<FuelEntry>>

    /**
     * Retrieves all fuel entries.
     */
    suspend fun getAllEntries(): List<FuelEntry>

    /**
     * Retrieves a single fuel entry by ID.
     */
    suspend fun getEntryById(id: Long): FuelEntry?

    /**
     * Retrieves the fuel entry with the latest (highest) odometer reading.
     */
    suspend fun getLatestEntry(): FuelEntry?

    /**
     * Inserts a new fuel entry into the database.
     */
    suspend fun insertEntry(entry: FuelEntry): Long

    /**
     * Updates an existing fuel entry in the database.
     */
    suspend fun updateEntry(entry: FuelEntry)

    /**
     * Deletes a fuel entry from the database.
     */
    suspend fun deleteEntry(entry: FuelEntry)
}

/**
 * Offline implementation of [FuelEntryRepository] backing by [FuelEntryDao].
 */
class OfflineFuelEntryRepository(
    private val fuelEntryDao: FuelEntryDao
) : FuelEntryRepository {

    override fun getAllEntriesFlow(): Flow<List<FuelEntry>> = fuelEntryDao.getAllFlow()

    override suspend fun getAllEntries(): List<FuelEntry> = fuelEntryDao.getAll()

    override suspend fun getEntryById(id: Long): FuelEntry? = fuelEntryDao.getById(id)

    override suspend fun getLatestEntry(): FuelEntry? = fuelEntryDao.getLatest()

    override suspend fun insertEntry(entry: FuelEntry): Long = fuelEntryDao.insert(entry)

    override suspend fun updateEntry(entry: FuelEntry) = fuelEntryDao.update(entry)

    override suspend fun deleteEntry(entry: FuelEntry) = fuelEntryDao.delete(entry)
}
