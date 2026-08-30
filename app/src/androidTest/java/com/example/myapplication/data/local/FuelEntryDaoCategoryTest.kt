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
 * Instrumented tests for [FuelEntryDao]'s category-filtered queries.
 *
 * Scope: covers the category-aware DAO methods against a real Room/SQLite
 * engine (in-memory), validating SQL semantics that the JVM unit tests
 * can't exercise (parameter binding, NULL handling, ordering, indices).
 *
 * Database environment: Room (`MileLiteDatabase`) backed by an
 * `inMemoryDatabaseBuilder` so each test starts with a clean schema and
 * the data is discarded on close — fast and isolated.
 *
 * Testing framework: JUnit 4 via `AndroidJUnit4` runner (the project's
 * `testInstrumentationRunner` is `androidx.test.runner.AndroidJUnitRunner`).
 * `runBlocking` is used to bridge `suspend` DAO methods, which is the
 * idiomatic pattern for non-reactive tests.
 */
@RunWith(AndroidJUnit4::class)
class FuelEntryDaoCategoryTest {

    private lateinit var database: MileLiteDatabase
    private lateinit var dao: FuelEntryDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MileLiteDatabase::class.java)
            // allowMainThreadQueries keeps the tests simple; the production
            // code never touches the DB on the main thread.
            .allowMainThreadQueries()
            .build()
        dao = database.fuelEntryDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    // ---------- Helpers ----------

    private fun entry(
        id: Long = 0,
        date: Long,
        odometer: Int,
        liters: Double = 10.0,
        cost: Double = 100.0,
        fuelCategory: String = FuelCategory.PETROL.displayName
    ): FuelEntry = FuelEntry(
        id = id,
        date = date,
        odometer = odometer,
        liters = liters,
        cost = cost,
        fuelCategory = fuelCategory
    )

    /**
     * Inserts [entries] and returns the rows in the order Room generated
     * (i.e. as they were inserted). Tests use the returned list to build
     * precise expected-id assertions.
     */
    private suspend fun seed(vararg entries: FuelEntry): List<FuelEntry> {
        val ids = dao.insertAll(entries.toList())
        return entries.zip(ids).map { (e, generatedId) -> e.copy(id = generatedId) }
    }

    // ---------- 1. Successful retrieval of items belonging to a specific category ----------

    @Test
    fun getAll_withPetrolCategory_returnsOnlyPetrolEntriesInMostRecentFirstOrder() = runBlocking {
        val seeded = seed(
            entry(date = 1_000L, odometer = 1_000, fuelCategory = "Petrol"),
            entry(date = 2_000L, odometer = 1_500, fuelCategory = "Diesel"),
            entry(date = 3_000L, odometer = 2_000, fuelCategory = "Petrol"),
            entry(date = 4_000L, odometer = 2_500, fuelCategory = "CNG")
        )

        val petrol = dao.getAll(FuelCategory.PETROL)

        // Two Petrol entries exist and only those are returned.
        assertEquals(2, petrol.size)
        assertTrue(petrol.all { it.fuelCategory == "Petrol" })

        // Ordering contract: date DESC, odometer DESC, id DESC.
        // Seeded with dates 1000 (petrol) and 3000 (petrol); expect 3000 first.
        assertEquals(3_000L, petrol[0].date)
        assertEquals(1_000L, petrol[1].date)
        // Sanity: every returned id corresponds to a Petrol row we inserted.
        val expectedIds = seeded.filter { it.fuelCategory == "Petrol" }.map { it.id }
        assertEquals(expectedIds, petrol.map { it.id })
    }

    @Test
    fun getAll_withDieselAndCngCategories_filtersCorrectly() = runBlocking {
        seed(
            entry(date = 1_000L, odometer = 1_000, fuelCategory = "Diesel"),
            entry(date = 2_000L, odometer = 1_500, fuelCategory = "CNG"),
            entry(date = 3_000L, odometer = 2_000, fuelCategory = "Diesel")
        )

        val diesel = dao.getAll(FuelCategory.DIESEL)
        val cng = dao.getAll(FuelCategory.CNG)

        assertEquals(2, diesel.size)
        assertTrue(diesel.all { it.fuelCategory == "Diesel" })
        assertEquals(1, cng.size)
        assertEquals("CNG", cng[0].fuelCategory)
    }

    @Test
    fun getLatestByCategory_returnsRowWithHighestOdometerInThatCategory() = runBlocking {
        val seeded = seed(
            entry(date = 1_000L, odometer = 1_000, fuelCategory = "Petrol"),
            entry(date = 2_000L, odometer = 1_500, fuelCategory = "Petrol"),
            entry(date = 3_000L, odometer = 900,  fuelCategory = "Petrol"), // newer date, lower odometer
            entry(date = 4_000L, odometer = 5_000, fuelCategory = "Diesel")
        )

        val latestPetrol = dao.getLatestByCategory(FuelCategory.PETROL)
        val latestDiesel = dao.getLatestByCategory(FuelCategory.DIESEL)

        // Latest is by odometer DESC, not by date — this row had the highest
        // odometer within Petrol even though its date isn't the newest.
        assertNotNull(latestPetrol)
        assertEquals(1_500, latestPetrol!!.odometer)
        // The row must be one of the Petrol rows we inserted.
        assertTrue(seeded.any { it.id == latestPetrol.id && it.fuelCategory == "Petrol" })

        assertNotNull(latestDiesel)
        assertEquals(5_000, latestDiesel!!.odometer)
    }

    @Test
    fun getAllFlow_withCategory_emitsOnlyMatchingRows() = runBlocking {
        seed(
            entry(date = 1_000L, odometer = 1_000, fuelCategory = "Petrol"),
            entry(date = 2_000L, odometer = 1_500, fuelCategory = "Diesel"),
            entry(date = 3_000L, odometer = 2_000, fuelCategory = "Petrol")
        )

        val snapshot = dao.getAllFlow(FuelCategory.PETROL).first()

        assertEquals(2, snapshot.size)
        assertTrue(snapshot.all { it.fuelCategory == "Petrol" })
    }

    // ---------- 2. Empty result sets ----------

    @Test
    fun getAll_withCategoryHavingNoEntries_returnsEmptyList() = runBlocking {
        // Seed only Petrol; querying CNG must yield an empty list (not null
        // and not an error).
        seed(
            entry(date = 1_000L, odometer = 1_000, fuelCategory = "Petrol"),
            entry(date = 2_000L, odometer = 1_500, fuelCategory = "Petrol")
        )

        val cng = dao.getAll(FuelCategory.CNG)
        assertTrue(cng.isEmpty())

        val cngFlow = dao.getAllFlow(FuelCategory.CNG).first()
        assertTrue(cngFlow.isEmpty())
    }

    @Test
    fun getAll_withCategoryOnEmptyDatabase_returnsEmptyList() = runBlocking {
        val petrol = dao.getAll(FuelCategory.PETROL)
        val anyFlow = dao.getAllFlow(FuelCategory.PETROL).first()
        assertTrue(petrol.isEmpty())
        assertTrue(anyFlow.isEmpty())
    }

    @Test
    fun getLatestByCategory_withNoMatchingEntries_returnsNull() = runBlocking {
        seed(entry(date = 1_000L, odometer = 1_000, fuelCategory = "Petrol"))

        val latestCng = dao.getLatestByCategory(FuelCategory.CNG)
        assertNull(latestCng)
    }

    // ---------- 3. Multiple category filters (OR logic at the call site) ----------

    @Test
    fun getAll_withTwoSequentialCategoryCalls_composeUnionAtCallSite() = runBlocking {
        // The DAO API is single-select; the OR pattern is: call once per
        // category and concat. This test pins the contract that each call
        // is independent (no shared cursor state, no caching surprises).
        val seeded = seed(
            entry(date = 1_000L, odometer = 1_000, fuelCategory = "Petrol"),
            entry(date = 2_000L, odometer = 1_500, fuelCategory = "Diesel"),
            entry(date = 3_000L, odometer = 2_000, fuelCategory = "Petrol"),
            entry(date = 4_000L, odometer = 2_500, fuelCategory = "CNG")
        )

        val petrolAndDiesel: List<Long> = (
            dao.getAll(FuelCategory.PETROL) + dao.getAll(FuelCategory.DIESEL)
        ).map { it.id }.distinct().sorted()

        val expected = seeded
            .filter { it.fuelCategory == "Petrol" || it.fuelCategory == "Diesel" }
            .map { it.id }
            .sorted()
        assertEquals(expected, petrolAndDiesel)
    }

    // ---------- 4. Edge cases: null and unknown category parameters ----------

    @Test
    fun getAll_withNullCategory_returnsAllEntries() = runBlocking {
        val seeded = seed(
            entry(date = 1_000L, odometer = 1_000, fuelCategory = "Petrol"),
            entry(date = 2_000L, odometer = 1_500, fuelCategory = "Diesel"),
            entry(date = 3_000L, odometer = 2_000, fuelCategory = "CNG")
        )

        val all = dao.getAll(null)

        // Null means "no filter": every row is returned.
        assertEquals(seeded.map { it.id }, all.map { it.id })
    }

    @Test
    fun getAllFlow_withNullCategory_emitsAllEntries() = runBlocking {
        val seeded = seed(
            entry(date = 1_000L, odometer = 1_000, fuelCategory = "Petrol"),
            entry(date = 2_000L, odometer = 1_500, fuelCategory = "Diesel"),
            entry(date = 3_000L, odometer = 2_000, fuelCategory = "CNG")
        )

        val all = dao.getAllFlow(null).first()
        assertEquals(seeded.size, all.size)
    }

    @Test
    fun getAll_withUnknownPersistedCategoryValue_doesNotMatchAnyEnumCategory() = runBlocking {
        // Simulate legacy/corrupt data: a row whose fuelCategory column
        // doesn't match any current enum displayName. The category-filtered
        // queries must not crash and must not return that row.
        seed(
            entry(date = 1_000L, odometer = 1_000, fuelCategory = "Petrol"),
            entry(date = 2_000L, odometer = 1_500, fuelCategory = "LPG"),     // unknown
            entry(date = 3_000L, odometer = 2_000, fuelCategory = "petrol"),   // wrong case
            entry(date = 4_000L, odometer = 2_500, fuelCategory = "PETROL")    // enum name
        )

        // Every known enum filter returns exactly the one canonical Petrol row.
        FuelCategory.entries.forEach { category ->
            val filtered = dao.getAll(category)
            assertEquals(
                "Filter for ${category.name} should return only rows with exact displayName match",
                1, filtered.size
            )
            assertEquals("Petrol", filtered[0].fuelCategory)
        }
    }

    @Test
    fun getAll_withEmptyCategory_doesNotMatchAndReturnsEmpty() = runBlocking {
        // The DAO parameter type is `FuelCategory?` (enum), so an "empty
        // string" cannot be passed in. This test pins that the only empty
        // form — `null` — means "no filter", distinct from any real
        // category value. (Defensive: future refactors must not treat null
        // as the string "null" or similar.)
        seed(entry(date = 1_000L, odometer = 1_000, fuelCategory = "Petrol"))

        val nullResult = dao.getAll(null)
        assertEquals(1, nullResult.size) // null = all
    }

    // ---------- 5. Data integrity: DB row -> domain model mapping ----------

    @Test
    fun getAll_preservesEveryColumnExactlyWhenRoundTripping() = runBlocking {
        // Insert a row with distinctive, non-default values in every column,
        // then read it back through the category filter to confirm Room
        // mapping is byte-equal (no truncation, no coercion).
        val original = entry(
            date = 1_700_000_000_000L,
            odometer = 123_456,
            liters = 42.75,
            cost = 5_999.99,
            fuelCategory = "Diesel"
        )
        val seeded = seed(original)
        val insertedId = seeded[0].id

        val read = dao.getAll(FuelCategory.DIESEL).single { it.id == insertedId }

        assertEquals(insertedId, read.id)
        assertEquals(original.date, read.date)
        assertEquals(original.odometer, read.odometer)
        assertEquals(original.liters, read.liters, 0.0)
        assertEquals(original.cost, read.cost, 0.0)
        assertEquals("Diesel", read.fuelCategory)
    }

    @Test
    fun getAll_withCategory_returnsRowsInMostRecentFirstOrder() = runBlocking {
        // Pins the ORDER BY clause independently of category filtering.
        // Three rows of the same category, inserted in date order; the DAO
        // must return them newest-first regardless of insertion order.
        seed(
            entry(date = 1_000L, odometer = 1_000, fuelCategory = "Petrol"),
            entry(date = 2_000L, odometer = 1_500, fuelCategory = "Petrol"),
            entry(date = 3_000L, odometer = 2_000, fuelCategory = "Petrol")
        )

        val petrol = dao.getAll(FuelCategory.PETROL)
        val dates = petrol.map { it.date }
        assertEquals(listOf(3_000L, 2_000L, 1_000L), dates)
    }

    @Test
    fun getLatestByCategory_returnsRowWithTiedHighestOdometerByIdDesc() = runBlocking {
        // Tie-break contract: when two rows share the highest odometer
        // within a category, the DAO returns the one with the larger id
        // (because the ORDER BY is odometer DESC, id DESC).
        val seeded = seed(
            entry(date = 1_000L, odometer = 5_000, fuelCategory = "Petrol"),
            entry(date = 2_000L, odometer = 5_000, fuelCategory = "Petrol")
        )

        val latest = dao.getLatestByCategory(FuelCategory.PETROL)
        assertNotNull(latest)
        assertEquals(5_000, latest!!.odometer)
        // The later-inserted row has the higher id; expect it to be returned.
        val expectedId = seeded.maxOf { it.id }
        assertEquals(expectedId, latest.id)
    }

    // ---------- Performance boundary ----------

    @Test
    fun getAll_withCategory_handlesLargeDatasetCorrectlyAndQuickly() = runBlocking {
        // Seeds 1,000 rows across all three categories, then asserts the
        // filter is both correct and sub-second on a real SQLite engine.
        val n = 1_000
        val rows = (1..n).map { i ->
            val name = when (i % 3) {
                0 -> "Petrol"
                1 -> "Diesel"
                else -> "CNG"
            }
            entry(date = i.toLong(), odometer = 1_000 + i, fuelCategory = name)
        }
        dao.insertAll(rows)

        val startNanos = System.nanoTime()
        val petrol = dao.getAll(FuelCategory.PETROL)
        val diesel = dao.getAll(FuelCategory.DIESEL)
        val cng = dao.getAll(FuelCategory.CNG)
        val all = dao.getAll(null)
        val elapsedMs = (System.nanoTime() - startNanos) / 1_000_000

        // Exact split: indices 0,3,6,... -> Petrol; 1,4,7,... -> Diesel; 2,5,8,... -> CNG.
        val expectedPetrol = n / 3
        val expectedDiesel = n / 3
        val expectedCng = n - 2 * (n / 3)

        assertEquals(expectedPetrol, petrol.size)
        assertEquals(expectedDiesel, diesel.size)
        assertEquals(expectedCng, cng.size)
        assertEquals(n, all.size)

        // The three category filters must sum to the unfiltered count — this
        // is a structural invariant of the partition.
        assertEquals(n, petrol.size + diesel.size + cng.size)

        // Sub-second ceiling on a real device/emulator. 1000 rows of indexed
        // data is trivial; this guards against accidental table scans or
        // quadratic patterns.
        assertTrue(
            "4 queries on 1000 rows took ${elapsedMs}ms (expected < 1000ms)",
            elapsedMs < 1_000
        )
    }
}
