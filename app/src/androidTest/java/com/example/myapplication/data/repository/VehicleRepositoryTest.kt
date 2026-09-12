package com.example.myapplication.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.data.local.FuelEntry
import com.example.myapplication.data.local.MileLiteDatabase
import com.example.myapplication.data.local.Vehicle
import com.example.myapplication.domain.demo.DemoDataGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the vehicle repositories: switching the active
 * vehicle, cascading a delete, and keeping each vehicle's fill-ups isolated.
 */
@RunWith(AndroidJUnit4::class)
class VehicleRepositoryTest {

    private lateinit var database: MileLiteDatabase
    private lateinit var vehicleRepository: VehicleRepository
    private lateinit var entryRepository: FuelEntryRepository

    @Before
    fun createRepositories() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MileLiteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        vehicleRepository = OfflineVehicleRepository(database.vehicleDao(), database.fuelEntryDao())
        entryRepository = OfflineFuelEntryRepository(database.fuelEntryDao())
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private suspend fun addVehicle(name: String, fuelType: String = "Diesel"): Long =
        vehicleRepository.insertVehicle(
            Vehicle(name = name, make = "Test", model = name, fuelType = fuelType)
        )

    private suspend fun addEntry(vehicleId: Long, odometer: Int, date: Long = 1_700_000_000_000L) =
        entryRepository.insertEntry(
            FuelEntry(
                vehicleId = vehicleId,
                date = date,
                odometer = odometer,
                liters = 30.0,
                cost = 2_700.0,
                fuelCategory = "Diesel"
            )
        )

    @Test
    fun switchingActiveVehicle_changesTheObservedHistory() = runBlocking {
        val diesel = addVehicle("Kia Seltos Diesel")
        val cng = addVehicle("Kia Seltos CNG", fuelType = "CNG")
        addEntry(diesel, odometer = 1_000)
        addEntry(diesel, odometer = 1_500, date = 1_700_100_000_000L)
        addEntry(cng, odometer = 8_000)

        vehicleRepository.setActiveVehicle(diesel)
        assertEquals(diesel, vehicleRepository.getActiveVehicle()?.id)
        val dieselId = vehicleRepository.getActiveVehicle()!!.id
        assertEquals(2, entryRepository.getAllEntriesFlowForVehicle(dieselId).first().size)

        vehicleRepository.setActiveVehicle(cng)
        assertEquals(cng, vehicleRepository.getActiveVehicle()?.id)
        val cngId = vehicleRepository.getActiveVehicle()!!.id
        assertEquals(1, entryRepository.getAllEntriesFlowForVehicle(cngId).first().size)
    }

    @Test
    fun setActiveVehicle_neverLeavesTwoVehiclesActive() = runBlocking {
        val first = addVehicle("Hyundai Creta Diesel")
        val second = addVehicle("Hyundai Creta CNG", fuelType = "CNG")

        vehicleRepository.setActiveVehicle(first)
        vehicleRepository.setActiveVehicle(second)

        val vehicles = vehicleRepository.getAllVehicles()
        assertEquals(1, vehicles.count { it.isActive })
        assertEquals(second, vehicles.first { it.isActive }.id)
    }

    @Test
    fun deleteVehicleWithEntries_removesItsFillUpsAndPromotesAnother() = runBlocking {
        val active = addVehicle("Tata Harrier Diesel")
        val other = addVehicle("Tata Harrier CNG", fuelType = "CNG")
        addEntry(active, odometer = 1_000)
        addEntry(active, odometer = 1_500, date = 1_700_100_000_000L)
        addEntry(other, odometer = 9_000)
        vehicleRepository.setActiveVehicle(active)

        vehicleRepository.deleteVehicleWithEntries(active)

        assertTrue(entryRepository.getAllEntriesForVehicle(active).isEmpty())
        assertEquals(1, entryRepository.getAllEntriesForVehicle(other).size)
        assertEquals(other, vehicleRepository.getActiveVehicle()?.id)
        assertFalse(vehicleRepository.getAllVehicles().any { it.id == active })
    }

    @Test
    fun demoProfiles_seedIsolatedHistoriesPerVehicle() = runBlocking {
        val fixedNow = 1_760_000_000_000L

        DemoDataGenerator.profiles.forEach { profile ->
            val vehicleId = addVehicle(profile.name, profile.fuelCategory.displayName)
            val rows = DemoDataGenerator.generateForVehicle(
                profile = profile,
                vehicleId = vehicleId,
                existing = emptyList(),
                now = fixedNow
            )
            entryRepository.insertEntries(rows)
        }

        val vehicles = vehicleRepository.getAllVehicles()
        assertEquals(DemoDataGenerator.profiles.size, vehicles.size)

        vehicles.forEach { vehicle ->
            val entries = entryRepository.getAllEntriesForVehicle(vehicle.id)
            assertEquals(DemoDataGenerator.DEFAULT_ENTRY_COUNT, entries.size)
            assertTrue(entries.all { it.vehicleId == vehicle.id })
        }

        val active = vehicles.firstOrNull { it.isActive }
        assertEquals(null, active)
        assertNotNull(vehicleRepository.getAllVehicles().firstOrNull())
    }
}
