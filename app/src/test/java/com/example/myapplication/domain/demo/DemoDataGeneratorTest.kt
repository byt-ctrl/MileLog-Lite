package com.example.myapplication.domain.demo

import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.data.local.FuelEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoDataGeneratorTest {

    private val fixedNow = 1_760_000_000_000L
    private val vehicleId = 7L

    private val dieselProfile = DemoDataGenerator.profiles.first {
        it.fuelCategory == FuelCategory.DIESEL
    }

    private val existingLog = listOf(
        FuelEntry(
            id = 1,
            vehicleId = vehicleId,
            date = fixedNow - 200L * DAY,
            odometer = 30_000,
            liters = 30.0,
            cost = 3_000.0
        )
    )

    @Test
    fun `six demo profiles cover three models in two fuel variants`() {
        val profiles = DemoDataGenerator.profiles

        assertEquals(6, profiles.size)
        assertEquals(profiles.size, profiles.map { it.name }.distinct().size)

        val models = profiles.map { it.model }.distinct()
        assertEquals(3, models.size)
        models.forEach { model ->
            val variants = profiles.filter { it.model == model }.map { it.fuelCategory }.toSet()
            assertEquals(setOf(FuelCategory.DIESEL, FuelCategory.CNG), variants)
        }
    }

    @Test
    fun `generates the requested number of rows`() {
        val rows = DemoDataGenerator.generateForVehicle(
            dieselProfile, vehicleId, emptyList(), count = 4, now = fixedNow
        )
        assertEquals(4, rows.size)
    }

    @Test
    fun `default count is used when none is given`() {
        val rows = DemoDataGenerator.generateForVehicle(
            dieselProfile, vehicleId, emptyList(), now = fixedNow
        )
        assertEquals(DemoDataGenerator.DEFAULT_ENTRY_COUNT, rows.size)
    }

    @Test
    fun `rejects a non positive count`() {
        val failure = runCatching {
            DemoDataGenerator.generateForVehicle(
                dieselProfile, vehicleId, emptyList(), count = 0
            )
        }
        assertTrue(failure.isFailure)
    }

    @Test
    fun `rows carry the vehicle id and no row id`() {
        val rows = DemoDataGenerator.generateForVehicle(
            dieselProfile, vehicleId, existingLog, now = fixedNow
        )
        assertTrue(rows.all { it.id == 0L })
        assertTrue(rows.all { it.vehicleId == vehicleId })
    }

    @Test
    fun `odometer increases and continues past the existing log`() {
        val rows = DemoDataGenerator.generateForVehicle(
            dieselProfile, vehicleId, existingLog, now = fixedNow
        )

        assertTrue(rows.first().odometer > existingLog.first().odometer)
        assertTrue(rows.zipWithNext().all { (older, newer) -> newer.odometer > older.odometer })
    }

    @Test
    fun `dates increase and do not run past now`() {
        val rows = DemoDataGenerator.generateForVehicle(
            dieselProfile, vehicleId, emptyList(), now = fixedNow
        )

        assertTrue(rows.zipWithNext().all { (older, newer) -> newer.date > older.date })
        assertTrue(rows.last().date <= fixedNow)
    }

    @Test
    fun `dates continue after an existing reading`() {
        val rows = DemoDataGenerator.generateForVehicle(
            dieselProfile, vehicleId, existingLog, now = fixedNow
        )
        assertTrue(rows.first().date > existingLog.first().date)
    }

    @Test
    fun `every row carries the profile fuel category`() {
        DemoDataGenerator.profiles.forEach { profile ->
            val rows = DemoDataGenerator.generateForVehicle(
                profile, vehicleId, emptyList(), now = fixedNow
            )
            assertTrue(
                rows.all { it.fuelCategory == profile.fuelCategory.displayName }
            )
        }
    }

    @Test
    fun `fuel and cost are positive and priced within the profile band`() {
        val rows = DemoDataGenerator.generateForVehicle(
            dieselProfile, vehicleId, existingLog, now = fixedNow
        )

        assertTrue(rows.all { it.liters > 0.0 })
        assertTrue(rows.all { it.cost > 0.0 })

        val pricePerUnit = rows.map { it.cost / it.liters }
        assertTrue(
            pricePerUnit.all {
                it >= dieselProfile.minPricePerUnit - 0.5 &&
                    it <= dieselProfile.maxPricePerUnit + 0.5
            }
        )
    }

    @Test
    fun `mileage between fill-ups lands in the profile band`() {
        DemoDataGenerator.profiles.forEach { profile ->
            val rows = DemoDataGenerator.generateForVehicle(
                profile, vehicleId, emptyList(), now = fixedNow
            )
            val mileages = rows.zipWithNext { older, newer ->
                (newer.odometer - older.odometer).toDouble() / newer.liters
            }
            assertTrue(
                "$profile must stay within its mileage band: $mileages",
                mileages.all {
                    it >= profile.minMileage - 0.5 && it <= profile.maxMileage + 0.5
                }
            )
        }
    }

    @Test
    fun `the same profile log and clock produce the same rows`() {
        val first = DemoDataGenerator.generateForVehicle(
            dieselProfile, vehicleId, existingLog, now = fixedNow
        )
        val second = DemoDataGenerator.generateForVehicle(
            dieselProfile, vehicleId, existingLog, now = fixedNow
        )
        assertEquals(first, second)
    }

    @Test
    fun `different profiles produce different histories`() {
        val cretaDiesel = DemoDataGenerator.profiles.first {
            it.model == "Creta" && it.fuelCategory == FuelCategory.DIESEL
        }
        val cretaCng = DemoDataGenerator.profiles.first {
            it.model == "Creta" && it.fuelCategory == FuelCategory.CNG
        }

        val dieselRows = DemoDataGenerator.generateForVehicle(
            cretaDiesel, 1L, emptyList(), now = fixedNow
        )
        val cngRows = DemoDataGenerator.generateForVehicle(
            cretaCng, 2L, emptyList(), now = fixedNow
        )

        assertTrue(dieselRows.first().odometer != cngRows.first().odometer)
    }

    private companion object {
        const val DAY = 24L * 60L * 60L * 1000L
    }
}
