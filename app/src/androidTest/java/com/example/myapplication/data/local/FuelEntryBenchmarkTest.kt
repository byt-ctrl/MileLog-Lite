package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

/**
 * High-volume performance and load test evaluating database index efficiency
 * and query response times with thousands of entries.
 */
@RunWith(AndroidJUnit4::class)
class FuelEntryBenchmarkTest {

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
    fun benchmark_highVolumeInsertsAndQueryPerformance() = runBlocking {
        val count = 5000
        val baseDate = 1700000000000L
        val entries = (1..count).map { i ->
            FuelEntry(
                date = baseDate + (i * 86400000L), // 1 day interval
                odometer = 10000 + (i * 300),      // 300 km interval
                liters = 25.0 + (i % 15),
                cost = 2500.0 + (i % 500)
            )
        }

        // 1. Benchmark bulk insert
        val insertDurationMs = measureTimeMillis {
            dao.insertAll(entries)
        }
        println("BENCHMARK: Bulk inserted $count entries in ${insertDurationMs}ms")
        assertTrue("Bulk insertion should complete within reasonable time", insertDurationMs < 10000)

        // 2. Benchmark getAll() with indexed multi-column order (date DESC, odometer DESC, id DESC)
        val getAllDurationMs = measureTimeMillis {
            val all = dao.getAll()
            assertEquals(count, all.size)
            // Verify ordering
            assertTrue(all.first().date >= all.last().date)
        }
        println("BENCHMARK: getAll() on $count entries took ${getAllDurationMs}ms")
        assertTrue("getAll() with composite index should be fast (< 500ms)", getAllDurationMs < 500)

        // 3. Benchmark getLatest() with indexed odometer query
        val getLatestDurationMs = measureTimeMillis {
            repeat(20) {
                val latest = dao.getLatest()
                assertNotNull(latest)
                assertEquals(10000 + (count * 300), latest!!.odometer)
            }
        }
        println("BENCHMARK: 20x getLatest() calls took ${getLatestDurationMs}ms (avg: ${getLatestDurationMs / 20.0}ms)")
        assertTrue("Indexed getLatest() should be nearly instantaneous (< 100ms total for 20 calls)", getLatestDurationMs < 100)

        // 4. Benchmark point lookups by ID
        val getByIdDurationMs = measureTimeMillis {
            repeat(50) { id ->
                val entry = dao.getById((id + 1).toLong())
                assertNotNull(entry)
            }
        }
        println("BENCHMARK: 50x getById() lookups took ${getByIdDurationMs}ms (avg: ${getByIdDurationMs / 50.0}ms)")
        assertTrue("Point lookups should be ultra-fast (< 100ms total for 50 calls)", getByIdDurationMs < 100)
    }
}
