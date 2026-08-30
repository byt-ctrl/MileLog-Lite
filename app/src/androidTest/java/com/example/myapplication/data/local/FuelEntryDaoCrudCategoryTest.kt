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
import kotlin.system.measureTimeMillis

/**
 * Instrumented tests for DAO CRUD operations with fuel category field.
 *
 * Covers I-CRUD-01 through I-CRUD-10 from the Sprint 5 Testing Plan.
 */
@RunWith(AndroidJUnit4::class)
class FuelEntryDaoCrudCategoryTest {

    private lateinit var database: MileLiteDatabase
    private lateinit var dao: FuelEntryDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MileLiteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.fuelEntryDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun entry(
        id: Long = 0,
        date: Long = 1000L,
        odometer: Int = 1000,
        liters: Double = 10.0,
        cost: Double = 100.0,
        fuelCategory: String = FuelCategory.PETROL.displayName
    ): FuelEntry = FuelEntry(
        id = id, date = date, odometer = odometer,
        liters = liters, cost = cost, fuelCategory = fuelCategory
    )

    // ---------- I-CRUD-01: Insert entries with category, read back correct category ----------

    @Test
    fun insert_entryWithCategory_preservesCategoryField() = runBlocking {
        val petrolEntry = entry(date = 1000L, odometer = 1000, fuelCategory = "Petrol")
        val dieselEntry = entry(date = 2000L, odometer = 1500, fuelCategory = "Diesel")
        val cngEntry = entry(date = 3000L, odometer = 2000, fuelCategory = "CNG")

        val id1 = dao.insert(petrolEntry)
        val id2 = dao.insert(dieselEntry)
        val id3 = dao.insert(cngEntry)

        assertEquals("Petrol", dao.getById(id1)!!.fuelCategory)
        assertEquals("Diesel", dao.getById(id2)!!.fuelCategory)
        assertEquals("CNG", dao.getById(id3)!!.fuelCategory)
    }

    // ---------- I-CRUD-02: Default category stores as Petrol ----------

    @Test
    fun insert_entryWithDefaultCategory_storesAsPetrol() = runBlocking {
        val entry = entry(date = 1000L, odometer = 1000) // no fuelCategory param → default "Petrol"
        val id = dao.insert(entry)

        val loaded = dao.getById(id)
        assertNotNull(loaded)
        assertEquals("Petrol", loaded!!.fuelCategory)
    }

    // ---------- I-CRUD-03: Update entry category change reflects in query results ----------

    @Test
    fun update_entryCategoryChange_reflectsInQueryResults() = runBlocking {
        val id = dao.insert(entry(date = 1000L, odometer = 1000, fuelCategory = "Petrol"))

        // Verify initially in Petrol filter
        val petrolBefore = dao.getAll(FuelCategory.PETROL)
        assertEquals(1, petrolBefore.size)

        // Update to Diesel
        val loaded = dao.getById(id)!!
        dao.update(loaded.copy(fuelCategory = "Diesel"))

        // Should no longer appear in Petrol filter
        val petrolAfter = dao.getAll(FuelCategory.PETROL)
        assertEquals(0, petrolAfter.size)

        // Should now appear in Diesel filter
        val dieselAfter = dao.getAll(FuelCategory.DIESEL)
        assertEquals(1, dieselAfter.size)
        assertEquals("Diesel", dieselAfter[0].fuelCategory)
    }

    // ---------- I-CRUD-04: Delete entry with specific category removes from filter results ----------

    @Test
    fun delete_entryWithSpecificCategory_removesFromFilterResults() = runBlocking {
        val id1 = dao.insert(entry(date = 1000L, odometer = 1000, fuelCategory = "Diesel"))
        val id2 = dao.insert(entry(date = 2000L, odometer = 1500, fuelCategory = "Diesel"))
        val id3 = dao.insert(entry(date = 3000L, odometer = 2000, fuelCategory = "Petrol"))

        // Verify 2 Diesel entries
        assertEquals(2, dao.getAll(FuelCategory.DIESEL).size)

        // Delete one Diesel entry
        val toDelete = dao.getById(id1)!!
        dao.delete(toDelete)

        // Should now have 1 Diesel entry
        val dieselAfter = dao.getAll(FuelCategory.DIESEL)
        assertEquals(1, dieselAfter.size)
        assertEquals(id2, dieselAfter[0].id)

        // Petrol should be unaffected
        assertEquals(1, dao.getAll(FuelCategory.PETROL).size)
    }

    // ---------- I-CRUD-05: Category filter respects date ordering ----------

    @Test
    fun getAll_categoryFilter_respectsDateOrdering() = runBlocking {
        // Insert Petrol entries with dates out of order
        dao.insert(entry(date = 1000L, odometer = 1000, fuelCategory = "Petrol"))
        dao.insert(entry(date = 3000L, odometer = 2000, fuelCategory = "Petrol"))
        dao.insert(entry(date = 2000L, odometer = 1500, fuelCategory = "Petrol"))
        // Add a Diesel entry to ensure it's excluded
        dao.insert(entry(date = 4000L, odometer = 3000, fuelCategory = "Diesel"))

        val petrol = dao.getAll(FuelCategory.PETROL)

        assertEquals(3, petrol.size)
        // Should be ordered: date DESC, odometer DESC, id DESC
        assertTrue(petrol[0].date >= petrol[1].date)
        assertTrue(petrol[1].date >= petrol[2].date)
    }

    // ---------- I-CRUD-06: getAllFlow category filter emits reactive updates on insert ----------

    @Test
    fun getAllFlow_categoryFilter_emitsReactiveUpdatesOnInsert() = runBlocking {
        // Start with one Petrol entry
        dao.insert(entry(date = 1000L, odometer = 1000, fuelCategory = "Petrol"))

        val flow = dao.getAllFlow(FuelCategory.PETROL)
        val initial = flow.first()
        assertEquals(1, initial.size)

        // Insert another Petrol entry
        dao.insert(entry(date = 2000L, odometer = 1500, fuelCategory = "Petrol"))

        val afterInsert = flow.first()
        assertEquals(2, afterInsert.size)
        assertTrue(afterInsert.all { it.fuelCategory == "Petrol" })
    }

    // ---------- I-CRUD-07: getAllFlow category filter emits reactive updates on delete ----------

    @Test
    fun getAllFlow_categoryFilter_emitsReactiveUpdatesOnDelete() = runBlocking {
        val id1 = dao.insert(entry(date = 1000L, odometer = 1000, fuelCategory = "Petrol"))
        val id2 = dao.insert(entry(date = 2000L, odometer = 1500, fuelCategory = "Petrol"))

        val flow = dao.getAllFlow(FuelCategory.PETROL)
        val initial = flow.first()
        assertEquals(2, initial.size)

        // Delete one entry
        val toDelete = dao.getById(id1)!!
        dao.delete(toDelete)

        val afterDelete = flow.first()
        assertEquals(1, afterDelete.size)
        assertEquals(id2, afterDelete[0].id)
    }

    // ---------- I-CRUD-08: getLatestByCategory after update returns new highest odometer ----------

    @Test
    fun getLatestByCategory_afterUpdate_returnsNewHighestOdometer() = runBlocking {
        val id1 = dao.insert(entry(date = 1000L, odometer = 1000, fuelCategory = "Petrol"))
        val id2 = dao.insert(entry(date = 2000L, odometer = 2000, fuelCategory = "Petrol"))

        // Initially id2 is latest
        val latest1 = dao.getLatestByCategory(FuelCategory.PETROL)
        assertEquals(2000, latest1!!.odometer)

        // Update id1 to have higher odometer
        val entry1 = dao.getById(id1)!!
        dao.update(entry1.copy(odometer = 3000))

        // Now id1 should be latest
        val latest2 = dao.getLatestByCategory(FuelCategory.PETROL)
        assertEquals(3000, latest2!!.odometer)
        assertEquals(id1, latest2.id)
    }

    // ---------- I-CRUD-09: Bulk insert with mixed categories, all retrievable by filter ----------

    @Test
    fun insertAll_bulkInsertWithMixedCategories_allRetrievableByFilter() = runBlocking {
        val entries = listOf(
            entry(date = 1000L, odometer = 1000, fuelCategory = "Petrol"),
            entry(date = 2000L, odometer = 1500, fuelCategory = "Diesel"),
            entry(date = 3000L, odometer = 2000, fuelCategory = "CNG"),
            entry(date = 4000L, odometer = 2500, fuelCategory = "Petrol"),
            entry(date = 5000L, odometer = 3000, fuelCategory = "Diesel"),
            entry(date = 6000L, odometer = 3500, fuelCategory = "CNG")
        )

        dao.insertAll(entries)

        assertEquals(2, dao.getAll(FuelCategory.PETROL).size)
        assertEquals(2, dao.getAll(FuelCategory.DIESEL).size)
        assertEquals(2, dao.getAll(FuelCategory.CNG).size)
        assertEquals(6, dao.getAll(null).size)
    }

    // ---------- I-CRUD-10: Category filter with index performs efficiently ----------

    @Test
    fun getAll_categoryFilter_withIndexPerformsEfficiently() = runBlocking {
        // Seed 500 rows across all three categories
        val n = 500
        val rows = (1..n).map { i ->
            val name = when (i % 3) {
                0 -> "Petrol"
                1 -> "Diesel"
                else -> "CNG"
            }
            entry(date = i.toLong(), odometer = 1000 + i, fuelCategory = name)
        }
        dao.insertAll(rows)

        val startNanos = System.nanoTime()
        val petrol = dao.getAll(FuelCategory.PETROL)
        val diesel = dao.getAll(FuelCategory.DIESEL)
        val cng = dao.getAll(FuelCategory.CNG)
        val all = dao.getAll(null)
        val elapsedMs = (System.nanoTime() - startNanos) / 1_000_000

        assertEquals(n / 3, petrol.size)
        assertEquals(n / 3, diesel.size)
        assertEquals(n - 2 * (n / 3), cng.size)
        assertEquals(n, all.size)

        // Sub-second on 500 rows with index
        assertTrue(
            "4 queries on $n rows took ${elapsedMs}ms (expected < 1000ms)",
            elapsedMs < 1_000
        )
    }
}
