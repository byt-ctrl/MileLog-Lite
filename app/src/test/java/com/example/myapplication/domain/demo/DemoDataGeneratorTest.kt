package com.example.myapplication.domain.demo

import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.data.local.FuelEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoDataGeneratorTest {

    private val fixedNow = 1_760_000_000_000L

    private val existingLog = listOf(
        FuelEntry(id = 1, date = fixedNow - 200L * DAY, odometer = 30_000, liters = 30.0, cost = 3_000.0)
    )

    @Test
    fun `generates the requested number of rows`() {
        val rows = DemoDataGenerator.generate(emptyList(), count = 4, now = fixedNow)
        assertEquals(4, rows.size)
    }

    @Test
    fun `default count is used when none is given`() {
        val rows = DemoDataGenerator.generate(emptyList(), now = fixedNow)
        assertEquals(DemoDataGenerator.DEFAULT_ENTRY_COUNT, rows.size)
    }

    @Test
    fun `rejects a non positive count`() {
        val failure = runCatching { DemoDataGenerator.generate(emptyList(), count = 0) }
        assertTrue(failure.isFailure)
    }

    @Test
    fun `rows carry no id so Room assigns them`() {
        val rows = DemoDataGenerator.generate(existingLog, now = fixedNow)
        assertTrue(rows.all { it.id == 0L })
    }

    @Test
    fun `odometer increases and continues past the existing log`() {
        val rows = DemoDataGenerator.generate(existingLog, now = fixedNow)

        assertTrue(rows.first().odometer > existingLog.first().odometer)
        assertTrue(rows.zipWithNext().all { (older, newer) -> newer.odometer > older.odometer })
    }

    @Test
    fun `dates increase and do not run past now`() {
        val rows = DemoDataGenerator.generate(emptyList(), now = fixedNow)

        assertTrue(rows.zipWithNext().all { (older, newer) -> newer.date > older.date })
        assertTrue(rows.last().date <= fixedNow)
    }

    @Test
    fun `dates continue after an existing reading`() {
        val rows = DemoDataGenerator.generate(existingLog, now = fixedNow)
        assertTrue(rows.first().date > existingLog.first().date)
    }

    @Test
    fun `fuel and cost are positive and priced plausibly`() {
        val rows = DemoDataGenerator.generate(existingLog, now = fixedNow)

        assertTrue(rows.all { it.liters > 0.0 })
        assertTrue(rows.all { it.cost > 0.0 })

        val pricePerLitre = rows.map { it.cost / it.liters }
        assertTrue(pricePerLitre.all { it in 90.0..115.0 })
    }

    @Test
    fun `mileage between fill-ups lands in a plausible band`() {
        val rows = DemoDataGenerator.generate(existingLog, now = fixedNow)

        val mileages = rows.zipWithNext { older, newer ->
            (newer.odometer - older.odometer).toDouble() / newer.liters
        }
        assertTrue(mileages.all { it in 13.0..21.0 })
    }

    @Test
    fun `every demo row is petrol`() {
        val rows = DemoDataGenerator.generate(emptyList(), now = fixedNow)
        assertTrue(rows.all { it.fuelCategory == FuelCategory.PETROL.displayName })
    }

    @Test
    fun `the same log and clock produce the same rows`() {
        val first = DemoDataGenerator.generate(existingLog, now = fixedNow)
        val second = DemoDataGenerator.generate(existingLog, now = fixedNow)
        assertEquals(first, second)
    }

    private companion object {
        const val DAY = 24L * 60L * 60L * 1000L
    }
}
