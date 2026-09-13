package com.example.myapplication.data.repository

import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.data.local.FuelEntry
import com.example.myapplication.data.local.FuelEntryDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing [FuelEntry] data operations.
 *
 * The unscoped methods are legacy: every screen reads through the vehicle-scoped
 * variants. They remain only for tests and are deprecated so new production code
 * does not use them by mistake.
 */
interface FuelEntryRepository {

    /**
     * Observes all fuel entries ordered most-recent first.
     */
    @Deprecated("Unscoped: use getAllEntriesFlowForVehicle(vehicleId). Retained for tests.")
    fun getAllEntriesFlow(): Flow<List<FuelEntry>>

    /**
     * Observes fuel entries ordered most-recent first, optionally filtered by [category].
     */
    @Deprecated("Unscoped: use getAllEntriesFlowForVehicle(vehicleId, category). Retained for tests.")
    fun getAllEntriesFlow(category: FuelCategory?): Flow<List<FuelEntry>>

    /**
     * Retrieves all fuel entries.
     */
    @Deprecated("Unscoped: use getAllEntriesForVehicle(vehicleId). Retained for tests.")
    suspend fun getAllEntries(): List<FuelEntry>

    /**
     * Retrieves fuel entries, optionally filtered by [category].
     */
    @Deprecated("Unscoped: use getAllEntriesForVehicle(vehicleId). Retained for tests.")
    suspend fun getAllEntries(category: FuelCategory?): List<FuelEntry>

    /**
     * Retrieves a single fuel entry by ID.
     */
    suspend fun getEntryById(id: Long): FuelEntry?

    /**
     * Retrieves the fuel entry with the latest (highest) odometer reading.
     */
    @Deprecated("Unscoped: use getLatestEntryForVehicle(vehicleId). Retained for tests.")
    suspend fun getLatestEntry(): FuelEntry?

    /**
     * Retrieves the latest fuel entry within a specific [category].
     */
    @Deprecated("Unscoped: use getLatestEntryForVehicle(vehicleId). Retained for tests.")
    suspend fun getLatestEntryByCategory(category: FuelCategory): FuelEntry?

    /**
     * Observes every fuel entry belonging to [vehicleId], most-recent first.
     */
    fun getAllEntriesFlowForVehicle(vehicleId: Long): Flow<List<FuelEntry>>

    /**
     * Observes fuel entries for [vehicleId], optionally filtered by [category].
     */
    fun getAllEntriesFlowForVehicle(vehicleId: Long, category: FuelCategory?): Flow<List<FuelEntry>>

    /**
     * Retrieves every fuel entry belonging to [vehicleId].
     */
    suspend fun getAllEntriesForVehicle(vehicleId: Long): List<FuelEntry>

    /**
     * Retrieves the highest-odometer entry for [vehicleId].
     */
    suspend fun getLatestEntryForVehicle(vehicleId: Long): FuelEntry?

    /**
     * Deletes every fuel entry belonging to [vehicleId] in one statement.
     */
    suspend fun deleteEntriesForVehicle(vehicleId: Long)

    /**
     * Inserts a new fuel entry into the database.
     */
    suspend fun insertEntry(entry: FuelEntry): Long

    /**
     * Inserts multiple fuel entries in bulk.
     */
    suspend fun insertEntries(entries: List<FuelEntry>): List<Long>

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
@Suppress("OVERRIDE_DEPRECATION")
class OfflineFuelEntryRepository(
    private val fuelEntryDao: FuelEntryDao
) : FuelEntryRepository {

    override fun getAllEntriesFlow(): Flow<List<FuelEntry>> = fuelEntryDao.getAllFlow()

    override fun getAllEntriesFlow(category: FuelCategory?): Flow<List<FuelEntry>> =
        fuelEntryDao.getAllFlow(category)

    override suspend fun getAllEntries(): List<FuelEntry> = fuelEntryDao.getAll()

    override suspend fun getAllEntries(category: FuelCategory?): List<FuelEntry> =
        fuelEntryDao.getAll(category)

    override suspend fun getEntryById(id: Long): FuelEntry? = fuelEntryDao.getById(id)

    override suspend fun getLatestEntry(): FuelEntry? = fuelEntryDao.getLatest()

    override suspend fun getLatestEntryByCategory(category: FuelCategory): FuelEntry? =
        fuelEntryDao.getLatestByCategory(category)

    override fun getAllEntriesFlowForVehicle(vehicleId: Long): Flow<List<FuelEntry>> =
        fuelEntryDao.getAllFlowForVehicle(vehicleId)

    override fun getAllEntriesFlowForVehicle(
        vehicleId: Long,
        category: FuelCategory?
    ): Flow<List<FuelEntry>> = fuelEntryDao.getAllFlowForVehicle(vehicleId, category)

    override suspend fun getAllEntriesForVehicle(vehicleId: Long): List<FuelEntry> =
        fuelEntryDao.getAllForVehicle(vehicleId)

    override suspend fun getLatestEntryForVehicle(vehicleId: Long): FuelEntry? =
        fuelEntryDao.getLatestForVehicle(vehicleId)

    override suspend fun deleteEntriesForVehicle(vehicleId: Long) =
        fuelEntryDao.deleteByVehicle(vehicleId)

    override suspend fun insertEntry(entry: FuelEntry): Long = fuelEntryDao.insert(entry)

    override suspend fun insertEntries(entries: List<FuelEntry>): List<Long> = fuelEntryDao.insertAll(entries)

    override suspend fun updateEntry(entry: FuelEntry) = fuelEntryDao.update(entry)

    override suspend fun deleteEntry(entry: FuelEntry) = fuelEntryDao.delete(entry)
}
