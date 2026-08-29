package com.example.myapplication.data.local

import com.example.myapplication.data.repository.FuelEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [FuelCategory] and the category-filtering surface of
 * [FuelEntryRepository].
 *
 * Scope notes (second-pass verification of the request vs the codebase):
 *  - The project enum is PETROL / DIESEL / CNG. The Sprint 5.1 deliverable
 *    explicitly fixed this set; Electric / Hybrid / LPG are not part of the
 *    current sprint. Tests assert the actual values.
 *  - Filtering is single-select by design: the repository's
 *    `getAllEntries(category: FuelCategory?)` accepts one nullable category.
 *    Multi-category OR filtering is exercised at the call-site level (the
 *    same pattern the History screen uses internally), without inventing
 *    new public API.
 */
class FuelCategoryTest {

    // ---------- Enum integrity ----------

    @Test
    fun `enum has exactly three values`() {
        // Guards against accidental additions/removals of enum entries.
        assertEquals(3, FuelCategory.entries.size)
    }

    @Test
    fun `enum contains petrol diesel and cng`() {
        val names = FuelCategory.entries.map { it.name }.toSet()
        assertEquals(setOf("PETROL", "DIESEL", "CNG"), names)
    }

    @Test
    fun `display names match expected user-facing strings`() {
        assertEquals("Petrol", FuelCategory.PETROL.displayName)
        assertEquals("Diesel", FuelCategory.DIESEL.displayName)
        assertEquals("CNG", FuelCategory.CNG.displayName)
    }

    @Test
    fun `default category is petrol`() {
        // The entity uses this as its default value; a regression here would
        // silently change the default for every newly inserted entry.
        assertSame(FuelCategory.PETROL, FuelCategory.DEFAULT)
        assertEquals("Petrol", FuelCategory.DEFAULT.displayName)
    }

    // ---------- fromDisplayName resolution ----------

    @Test
    fun `fromDisplayName resolves each canonical display name`() {
        assertSame(FuelCategory.PETROL, FuelCategory.fromDisplayName("Petrol"))
        assertSame(FuelCategory.DIESEL, FuelCategory.fromDisplayName("Diesel"))
        assertSame(FuelCategory.CNG, FuelCategory.fromDisplayName("CNG"))
    }

    @Test
    fun `fromDisplayName returns default for null and blank input`() {
        assertSame(FuelCategory.DEFAULT, FuelCategory.fromDisplayName(null))
        assertSame(FuelCategory.DEFAULT, FuelCategory.fromDisplayName(""))
    }

    @Test
    fun `fromDisplayName returns default for unknown values`() {
        assertSame(FuelCategory.DEFAULT, FuelCategory.fromDisplayName("Electric"))
        assertSame(FuelCategory.DEFAULT, FuelCategory.fromDisplayName("petrol")) // case-sensitive
        assertSame(FuelCategory.DEFAULT, FuelCategory.fromDisplayName("PETROL")) // enum name, not displayName
        assertSame(FuelCategory.DEFAULT, FuelCategory.fromDisplayName("LPG"))
    }

    @Test
    fun `fromDisplayName is case sensitive on the display name`() {
        // Documents the contract: the persisted string must be the displayName,
        // not the enum constant name or a different casing.
        assertSame(FuelCategory.DEFAULT, FuelCategory.fromDisplayName("petrol"))
        assertSame(FuelCategory.DEFAULT, FuelCategory.fromDisplayName("DIESEL"))
    }

    // ---------- Filtering logic (single selection via repository contract) ----------

    /**
     * A tiny in-memory repository that mirrors [OfflineFuelEntryRepository]'s
     * category-filtering contract. We avoid Room/coroutines in unit tests by
     * doing the same `displayName == value` comparison the DAO uses.
     */
    private class InMemoryRepository(seed: List<FuelEntry>) : FuelEntryRepository {
        private val all = seed

        override fun getAllEntriesFlow(): Flow<List<FuelEntry>> = flowOf(all)

        override fun getAllEntriesFlow(category: FuelCategory?): Flow<List<FuelEntry>> {
            val filtered = if (category == null) all
            else all.filter { it.fuelCategory == category.displayName }
            return flowOf(filtered)
        }

        override suspend fun getAllEntries(): List<FuelEntry> = all

        override suspend fun getAllEntries(category: FuelCategory?): List<FuelEntry> {
            return if (category == null) all
            else all.filter { it.fuelCategory == category.displayName }
        }

        override suspend fun getEntryById(id: Long): FuelEntry? = all.firstOrNull { it.id == id }

        override suspend fun getLatestEntry(): FuelEntry? = all.maxByOrNull { it.odometer }

        override suspend fun getLatestEntryByCategory(category: FuelCategory): FuelEntry? =
            all.filter { it.fuelCategory == category.displayName }
                .maxByOrNull { it.odometer }

        override suspend fun insertEntry(entry: FuelEntry): Long = error("not used in tests")
        override suspend fun insertEntries(entries: List<FuelEntry>): List<Long> = error("not used")
        override suspend fun updateEntry(entry: FuelEntry) = error("not used")
        override suspend fun deleteEntry(entry: FuelEntry) = error("not used")
    }

    private fun entry(id: Long, category: String, odometer: Int = 1000): FuelEntry =
        FuelEntry(id = id, date = id * 1000L, odometer = odometer, liters = 10.0, cost = 100.0, fuelCategory = category)

    @Test
    fun `single selection returns only matching items`() = runBlocking {
        val repo = InMemoryRepository(
            listOf(
                entry(1, "Petrol"),
                entry(2, "Diesel"),
                entry(3, "Petrol"),
                entry(4, "CNG")
            )
        )

        val petrol = repo.getAllEntries(FuelCategory.PETROL)
        assertEquals(listOf(1L, 3L), petrol.map { it.id })

        val diesel = repo.getAllEntries(FuelCategory.DIESEL)
        assertEquals(listOf(2L), diesel.map { it.id })

        val cng = repo.getAllEntries(FuelCategory.CNG)
        assertEquals(listOf(4L), cng.map { it.id })
    }

    @Test
    fun `single selection returns empty when no entries match`() = runBlocking {
        val repo = InMemoryRepository(listOf(entry(1, "Petrol"), entry(2, "Petrol")))
        val cng = repo.getAllEntries(FuelCategory.CNG)
        assertTrue(cng.isEmpty())
    }

    @Test
    fun `empty selection (null category) returns all entries`() = runBlocking {
        // Contract: null means "no filter" — caller intent is "show everything".
        val seed = listOf(entry(1, "Petrol"), entry(2, "Diesel"), entry(3, "CNG"))
        val repo = InMemoryRepository(seed)
        val all = repo.getAllEntries(category = null)
        assertEquals(seed.map { it.id }, all.map { it.id })
    }

    @Test
    fun `multi selection (OR logic) at call site returns union of categories`() = runBlocking {
        // The project deliberately has no multi-select API; the OR case is
        // expressed by calling the repository per-category and concatenating,
        // which is what the test simulates.
        val seed = listOf(
            entry(1, "Petrol"),
            entry(2, "Diesel"),
            entry(3, "Petrol"),
            entry(4, "CNG"),
            entry(5, "Diesel")
        )
        val repo = InMemoryRepository(seed)
        val selected = listOf(FuelCategory.PETROL, FuelCategory.DIESEL)

        val perCategory: List<List<FuelEntry>> = selected.map { repo.getAllEntries(it) }
        val union = perCategory.flatten().distinctBy { it.id }

        // Expected ids: every Petrol entry first, then every Diesel entry
        // (insertion order within each category is preserved by the
        // repository's filter). CNG is excluded by the selection.
        assertEquals(listOf(1L, 3L, 2L, 5L), union.map { it.id })

        // CNG is part of the seed but not in the selected list.
        assertTrue(union.none { it.fuelCategory == "CNG" })
    }

    @Test
    fun `filter against unknown category in data returns empty`() = runBlocking {
        // An entry persisted with an unrecognized category string (e.g. from
        // a future schema migration) won't match any current category. The
        // filter must not crash and must return an empty list.
        val repo = InMemoryRepository(
            listOf(entry(1, "Petrol"), entry(2, "Hydrogen"), entry(3, "Diesel"))
        )
        val petrol = repo.getAllEntries(FuelCategory.PETROL)
        assertEquals(listOf(1L), petrol.map { it.id })

        val cng = repo.getAllEntries(FuelCategory.CNG)
        assertTrue(cng.isEmpty())
    }

    @Test
    fun `filter against empty dataset returns empty list`() = runBlocking {
        val repo = InMemoryRepository(emptyList())
        assertTrue(repo.getAllEntries().isEmpty())
        assertTrue(repo.getAllEntries(FuelCategory.PETROL).isEmpty())
        assertTrue(repo.getAllEntries(null).isEmpty())
    }

    @Test
    fun `latest entry by category respects filter`() = runBlocking {
        val repo = InMemoryRepository(
            listOf(
                entry(1, "Petrol", odometer = 1000),
                entry(2, "Diesel", odometer = 2000),
                entry(3, "Petrol", odometer = 3000), // latest petrol
                entry(4, "Diesel", odometer = 1500)
            )
        )
        val latestPetrol = repo.getLatestEntryByCategory(FuelCategory.PETROL)
        assertNotNull(latestPetrol)
        assertEquals(3L, latestPetrol!!.id)

        val latestDiesel = repo.getLatestEntryByCategory(FuelCategory.DIESEL)
        assertNotNull(latestDiesel)
        assertEquals(2L, latestDiesel!!.id)

        val latestCng = repo.getLatestEntryByCategory(FuelCategory.CNG)
        assertEquals(null, latestCng) // no CNG entries
    }

    // ---------- Boundary conditions ----------

    @Test
    fun `filter handles large dataset efficiently and correctly`() = runBlocking {
        val n = 10_000
        val seed = (1..n).map { i ->
            val name = when (i % 3) {
                0 -> "Petrol"
                1 -> "Diesel"
                else -> "CNG"
            }
            entry(i.toLong(), name)
        }
        val repo = InMemoryRepository(seed)

        // Exact expected counts: 0..(n-1) mod 3 == 0 -> n/3 of them
        val expectedPetrol = seed.count { it.fuelCategory == "Petrol" }
        val expectedDiesel = seed.count { it.fuelCategory == "Diesel" }
        val expectedCng = seed.count { it.fuelCategory == "CNG" }

        val startNanos = System.nanoTime()
        val petrol = repo.getAllEntries(FuelCategory.PETROL)
        val diesel = repo.getAllEntries(FuelCategory.DIESEL)
        val cng = repo.getAllEntries(FuelCategory.CNG)
        val elapsedMs = (System.nanoTime() - startNanos) / 1_000_000

        assertEquals(expectedPetrol, petrol.size)
        assertEquals(expectedDiesel, diesel.size)
        assertEquals(expectedCng, cng.size)
        // Sanity: pure-Kotlin linear filter over 10k rows must be sub-second.
        // 1s is a generous ceiling that catches accidental O(n^2) regressions
        // without being flaky on slow CI.
        assertTrue(
            "filter on 10k entries took ${elapsedMs}ms (expected < 1000ms)",
            elapsedMs < 1_000
        )
    }

    @Test
    fun `filter is case sensitive on the persisted category string`() = runBlocking {
        // Documents and pins the case-sensitivity contract: only the canonical
        // displayName matches. Lowercase or uppercase persisted strings fall
        // through and are not surfaced by the filter.
        val repo = InMemoryRepository(
            listOf(
                entry(1, "Petrol"),    // matches PETROL
                entry(2, "petrol"),    // does NOT match
                entry(3, "PETROL")     // does NOT match (enum name, not displayName)
            )
        )
        val petrol = repo.getAllEntries(FuelCategory.PETROL)
        assertEquals(listOf(1L), petrol.map { it.id })
    }
}
