package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
 * Instrumented tests for [FuelEntryDao] using an in-memory Room database.
 */
@RunWith(AndroidJUnit4::class)
class FuelEntryDaoTest {

    private lateinit var database: MileLiteDatabase
    private lateinit var dao: FuelEntryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MileLiteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.fuelEntryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insert_returnsPositiveId_andEntryIsRetrievable() = runBlocking {
        val entry = fuelEntry(odometer = 10000, liters = 30.0, cost = 3000.0)
        val id = dao.insert(entry)

        assertTrue(id > 0)
        val loaded = dao.getById(id)
        assertNotNull(loaded)
        assertEquals(10000, loaded!!.odometer)
        assertEquals(30.0, loaded.liters, 0.0001)
        assertEquals(3000.0, loaded.cost, 0.0001)
    }

    @Test
    fun getAll_returnsEntriesMostRecentFirst() = runBlocking {
        dao.insert(fuelEntry(date = 1000L, odometer = 10000))
        dao.insert(fuelEntry(date = 3000L, odometer = 12000))
        dao.insert(fuelEntry(date = 2000L, odometer = 11000))

        val entries = dao.getAll()
        assertEquals(3, entries.size)
        // date DESC
        assertEquals(3000L, entries[0].date)
        assertEquals(2000L, entries[1].date)
        assertEquals(1000L, entries[2].date)
    }

    @Test
    fun getAllFlow_emitsUpdatedListAfterInsertAndDelete() = runBlocking {
        val entry = fuelEntry(odometer = 10000)
        dao.insert(entry)
        val afterInsert = dao.getAllFlow().first()
        assertEquals(1, afterInsert.size)

        dao.delete(afterInsert[0])
        val afterDelete = dao.getAllFlow().first()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun getLatest_returnsHighestOdometerEntry() = runBlocking {
        dao.insert(fuelEntry(date = 1000L, odometer = 10000))
        dao.insert(fuelEntry(date = 3000L, odometer = 14000))
        dao.insert(fuelEntry(date = 2000L, odometer = 12000))

        val latest = dao.getLatest()
        assertNotNull(latest)
        assertEquals(14000, latest!!.odometer)
    }

    @Test
    fun getLatest_returnsNullWhenEmpty() = runBlocking {
        assertNull(dao.getLatest())
    }

    @Test
    fun update_changesExistingEntry() = runBlocking {
        val id = dao.insert(fuelEntry(odometer = 10000, liters = 30.0, cost = 3000.0))

        dao.update(
            FuelEntry(
                id = id,
                date = 5000L,
                odometer = 10500,
                liters = 35.0,
                cost = 3500.0
            )
        )

        val updated = dao.getById(id)
        assertNotNull(updated)
        assertEquals(5000L, updated!!.date)
        assertEquals(10500, updated.odometer)
        assertEquals(35.0, updated.liters, 0.0001)
        assertEquals(3500.0, updated.cost, 0.0001)
    }

    @Test
    fun delete_removesEntryFromDatabase() = runBlocking {
        val id = dao.insert(fuelEntry(odometer = 10000))
        dao.delete(dao.getById(id)!!)
        assertNull(dao.getById(id))
    }

    @Test
    fun entriesPersistAcrossDatabaseReopen(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "persistence-test.db"
        context.deleteDatabase(dbName)

        // Write to a file-backed database and close it.
        val firstDb = Room.databaseBuilder(context, MileLiteDatabase::class.java, dbName).build()
        val insertedId = firstDb.fuelEntryDao().insert(fuelEntry(odometer = 10000))
        firstDb.close()

        // Reopen the same database file; the entry must still be there (force-close resilience).
        val secondDb = Room.databaseBuilder(context, MileLiteDatabase::class.java, dbName).build()
        val loaded = secondDb.fuelEntryDao().getById(insertedId)
        assertNotNull(loaded)
        assertEquals(10000, loaded!!.odometer)
        secondDb.close()

        context.deleteDatabase(dbName)
    }

    private fun fuelEntry(
        date: Long = 1000L,
        odometer: Int = 10000,
        liters: Double = 30.0,
        cost: Double = 3000.0
    ) = FuelEntry(
        date = date,
        odometer = odometer,
        liters = liters,
        cost = cost
    )
}
