package com.example.myapplication.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [VehicleDao] and the vehicle-scoped [FuelEntryDao]
 * queries against a real Room/SQLite engine (in-memory).
 */
@RunWith(AndroidJUnit4::class)
class VehicleDaoTest {

    private lateinit var database: MileLiteDatabase
    private lateinit var vehicleDao: VehicleDao
    private lateinit var fuelEntryDao: FuelEntryDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MileLiteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        vehicleDao = database.vehicleDao()
        fuelEntryDao = database.fuelEntryDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun vehicle(
        name: String,
        make: String = "Hyundai",
        model: String = "Creta",
        fuelType: String = "Diesel",
        isActive: Boolean = false
    ) = Vehicle(
        name = name,
        make = make,
        model = model,
        registrationNumber = "",
        fuelType = fuelType,
        isActive = isActive
    )

    private fun entry(vehicleId: Long, odometer: Int, date: Long = 1_700_000_000_000L) = FuelEntry(
        vehicleId = vehicleId,
        date = date,
        odometer = odometer,
        liters = 30.0,
        cost = 2_700.0,
        fuelCategory = "Diesel"
    )

    @Test
    fun insert_returnsPositiveIdAndRoundTripsEveryColumn() = runBlocking {
        val inserted = vehicle(
            name = "Hyundai Creta Diesel",
            make = "Hyundai",
            model = "Creta",
            fuelType = "Diesel"
        )
        val id = vehicleDao.insert(inserted)
        assertTrue(id > 0L)

        val read = vehicleDao.getById(id)
        assertNotNull(read)
        assertEquals("Hyundai Creta Diesel", read!!.name)
        assertEquals("Hyundai", read.make)
        assertEquals("Creta", read.model)
        assertEquals("Diesel", read.fuelType)
        assertEquals(false, read.isActive)
    }

    @Test
    fun getAll_ordersActiveVehicleFirstThenByName() = runBlocking {
        vehicleDao.insert(vehicle(name = "Zeta"))
        vehicleDao.insert(vehicle(name = "Alpha", isActive = true))

        val names = vehicleDao.getAll().map { it.name }

        assertEquals(listOf("Alpha", "Zeta"), names)
    }

    @Test
    fun getActive_isNullWhenNoVehicleIsSelected() = runBlocking {
        vehicleDao.insert(vehicle(name = "Kia Seltos"));

        assertNull(vehicleDao.getActive())
        assertNull(vehicleDao.getActiveFlow().first())
    }

    @Test
    fun setActive_marksExactlyOneVehicleActive() = runBlocking {
        val first = vehicleDao.insert(vehicle(name = "Kia Seltos", isActive = true))
        val second = vehicleDao.insert(vehicle(name = "Tata Harrier"))

        vehicleDao.setActive(second)

        assertEquals(second, vehicleDao.getActive()?.id)
        assertEquals(1, vehicleDao.getAll().count { it.isActive })
        assertEquals(false, vehicleDao.getById(first)!!.isActive)
    }

    @Test
    fun getByName_findsTheVehicle() = runBlocking {
        vehicleDao.insert(vehicle(name = "Kia Seltos CNG", fuelType = "CNG"))

        assertEquals("Kia Seltos CNG", vehicleDao.getByName("Kia Seltos CNG")?.name)
        assertNull(vehicleDao.getByName("Missing"))
    }

    @Test
    fun insertingAnExistingName_replacesRatherThanDuplicates() = runBlocking {
        vehicleDao.insert(vehicle(name = "Tata Harrier"))
        vehicleDao.insert(vehicle(name = "Tata Harrier", make = "Tata"))

        val all = vehicleDao.getAll()
        assertEquals(1, all.size)
        assertEquals("Tata", all.first().make)
    }

    @Test
    fun update_persistsChanges() = runBlocking {
        val id = vehicleDao.insert(vehicle(name = "Kia Seltos"))
        val stored = vehicleDao.getById(id)!!

        vehicleDao.update(stored.copy(name = "Kia Seltos Diesel", model = "Seltos"))

        val updated = vehicleDao.getById(id)!!
        assertEquals("Kia Seltos Diesel", updated.name)
        assertEquals("Seltos", updated.model)
    }

    @Test
    fun delete_removesTheRow() = runBlocking {
        val id = vehicleDao.insert(vehicle(name = "Tata Harrier CNG"))
        val stored = vehicleDao.getById(id)!!

        vehicleDao.delete(stored)

        assertNull(vehicleDao.getById(id))
    }

    @Test
    fun deleteByVehicle_removesOnlyThatVehiclesEntries() = runBlocking {
        val first = vehicleDao.insert(vehicle(name = "Kia Seltos"))
        val second = vehicleDao.insert(vehicle(name = "Tata Harrier"))
        fuelEntryDao.insert(entry(first, odometer = 1_000))
        fuelEntryDao.insert(entry(first, odometer = 1_500, date = 1_700_100_000_000L))
        fuelEntryDao.insert(entry(second, odometer = 9_000))

        fuelEntryDao.deleteByVehicle(first)

        assertTrue(fuelEntryDao.getAllForVehicle(first).isEmpty())
        assertEquals(1, fuelEntryDao.getAllForVehicle(second).size)
    }

    @Test
    fun vehicleScopedQueries_isolateEachVehiclesHistory() = runBlocking {
        val first = vehicleDao.insert(vehicle(name = "Kia Seltos Diesel"))
        val second = vehicleDao.insert(vehicle(name = "Kia Seltos CNG", fuelType = "CNG"))
        fuelEntryDao.insert(entry(first, odometer = 1_000))
        fuelEntryDao.insert(entry(first, odometer = 1_500, date = 1_700_100_000_000L))
        fuelEntryDao.insert(entry(second, odometer = 8_000))

        assertEquals(2, fuelEntryDao.getAllForVehicle(first).size)
        assertEquals(1, fuelEntryDao.getAllForVehicle(second).size)
        assertEquals(1_500, fuelEntryDao.getLatestForVehicle(first)?.odometer)
        assertEquals(8_000, fuelEntryDao.getLatestForVehicle(second)?.odometer)
        assertEquals(2, fuelEntryDao.getAllFlowForVehicle(first).first().size)
    }
}
