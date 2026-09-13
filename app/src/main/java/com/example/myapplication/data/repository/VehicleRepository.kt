package com.example.myapplication.data.repository

import androidx.room.withTransaction
import com.example.myapplication.data.local.MileLiteDatabase
import com.example.myapplication.data.local.Vehicle
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing [Vehicle] data operations.
 */
interface VehicleRepository {

    /**
     * Observes every vehicle, active first, then by name.
     */
    fun getAllVehiclesFlow(): Flow<List<Vehicle>>

    /**
     * Observes the single active vehicle, or null when none is selected.
     */
    fun getActiveVehicleFlow(): Flow<Vehicle?>

    /**
     * Retrieves every vehicle, active first, then by name.
     */
    suspend fun getAllVehicles(): List<Vehicle>

    /**
     * Retrieves the single active vehicle, or null when none is selected.
     */
    suspend fun getActiveVehicle(): Vehicle?

    /**
     * Retrieves a vehicle by ID.
     */
    suspend fun getVehicleById(id: Long): Vehicle?

    /**
     * Inserts a new vehicle, returning the auto-generated ID.
     */
    suspend fun insertVehicle(vehicle: Vehicle): Long

    /**
     * Updates an existing vehicle.
     */
    suspend fun updateVehicle(vehicle: Vehicle)

    /**
     * Deletes a vehicle without touching its fill-ups.
     */
    suspend fun deleteVehicle(vehicle: Vehicle)

    /**
     * Selects exactly one active vehicle.
     */
    suspend fun setActiveVehicle(id: Long)

    /**
     * Deletes a vehicle together with every fill-up logged against it, then
     * promotes another vehicle to active when the deleted one was selected.
     */
    suspend fun deleteVehicleWithEntries(vehicleId: Long)
}

/**
 * Offline implementation of [VehicleRepository] backed by the Room database.
 */
class OfflineVehicleRepository(
    private val database: MileLiteDatabase
) : VehicleRepository {

    private val vehicleDao = database.vehicleDao()
    private val fuelEntryDao = database.fuelEntryDao()

    override fun getAllVehiclesFlow(): Flow<List<Vehicle>> = vehicleDao.getAllFlow()

    override fun getActiveVehicleFlow(): Flow<Vehicle?> = vehicleDao.getActiveFlow()

    override suspend fun getAllVehicles(): List<Vehicle> = vehicleDao.getAll()

    override suspend fun getActiveVehicle(): Vehicle? = vehicleDao.getActive()

    override suspend fun getVehicleById(id: Long): Vehicle? = vehicleDao.getById(id)

    override suspend fun insertVehicle(vehicle: Vehicle): Long = vehicleDao.insert(vehicle)

    override suspend fun updateVehicle(vehicle: Vehicle) = vehicleDao.update(vehicle)

    override suspend fun deleteVehicle(vehicle: Vehicle) = vehicleDao.delete(vehicle)

    override suspend fun setActiveVehicle(id: Long) = vehicleDao.setActive(id)

    /**
     * Runs as one transaction so the entries, the vehicle and the promoted
     * active vehicle cannot end up half-applied. Without it, a crash between
     * the deletes would leave orphaned fill-ups or no active vehicle.
     */
    override suspend fun deleteVehicleWithEntries(vehicleId: Long) {
        database.withTransaction {
            val target = vehicleDao.getById(vehicleId)
            if (target != null) {
                // The foreign key cascades, but deleting explicitly keeps the
                // intent obvious and works regardless of FK enforcement.
                fuelEntryDao.deleteByVehicle(vehicleId)
                vehicleDao.delete(target)
            }
            if (vehicleDao.getActive() == null) {
                vehicleDao.getAll().firstOrNull()?.let { vehicleDao.setActive(it.id) }
            }
        }
    }
}
