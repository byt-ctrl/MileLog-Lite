package com.example.myapplication.domain.calculation

import com.example.myapplication.data.local.FuelEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MileageCalculatorTest {

    @Test
    fun testEmptyList() {
        val stats = MileageCalculator.calculateDashboardStats(emptyList())
        assertNull(stats.latestOdometer)
        assertEquals(0, stats.totalDistance)
        assertEquals(0.0, stats.totalFuel, 0.001)
        assertEquals(0.0, stats.totalCost, 0.001)
        assertNull(stats.averageMileage)
        assertNull(stats.costPerKm)

        val fillups = MileageCalculator.calculatePerFillupMileage(emptyList())
        assertEquals(0, fillups.size)
    }

    @Test
    fun testSingleEntry() {
        val entry = FuelEntry(id = 1, date = 1000L, odometer = 1000, liters = 50.0, cost = 100.0)
        val stats = MileageCalculator.calculateDashboardStats(listOf(entry))
        
        assertEquals(1000, stats.latestOdometer)
        assertEquals(0, stats.totalDistance)
        assertEquals(50.0, stats.totalFuel, 0.001)
        assertEquals(100.0, stats.totalCost, 0.001)
        assertNull(stats.averageMileage)
        assertNull(stats.costPerKm)

        val fillups = MileageCalculator.calculatePerFillupMileage(listOf(entry))
        assertEquals(1, fillups.size)
        assertEquals(entry, fillups[0].entry)
        assertNull(fillups[0].mileageKmPerL)
    }

    @Test
    fun testTwoEntries() {
        val entry1 = FuelEntry(id = 1, date = 1000L, odometer = 1000, liters = 50.0, cost = 100.0)
        val entry2 = FuelEntry(id = 2, date = 2000L, odometer = 1500, liters = 40.0, cost = 80.0)
        
        // Passing in reverse order to ensure sorting works
        val stats = MileageCalculator.calculateDashboardStats(listOf(entry2, entry1)) 
        
        assertEquals(1500, stats.latestOdometer)
        assertEquals(500, stats.totalDistance)
        assertEquals(90.0, stats.totalFuel, 0.001)
        assertEquals(180.0, stats.totalCost, 0.001)
        
        // Average mileage: 500 / 40.0 (second entry fuel)
        assertEquals(12.5, stats.averageMileage!!, 0.001)
        
        // Cost per km: 180.0 / 500
        assertEquals(0.36, stats.costPerKm!!, 0.001)

        val fillups = MileageCalculator.calculatePerFillupMileage(listOf(entry2, entry1))
        assertEquals(2, fillups.size)
        assertNull(fillups[0].mileageKmPerL)
        assertEquals(12.5, fillups[1].mileageKmPerL!!, 0.001)
    }

    @Test
    fun testThreeEntries() {
        val entry1 = FuelEntry(id = 1, date = 1000L, odometer = 1000, liters = 50.0, cost = 100.0)
        val entry2 = FuelEntry(id = 2, date = 2000L, odometer = 1500, liters = 40.0, cost = 80.0)
        val entry3 = FuelEntry(id = 3, date = 3000L, odometer = 1900, liters = 40.0, cost = 80.0)
        
        val stats = MileageCalculator.calculateDashboardStats(listOf(entry1, entry3, entry2))
        
        assertEquals(1900, stats.latestOdometer)
        assertEquals(900, stats.totalDistance)
        assertEquals(130.0, stats.totalFuel, 0.001)
        assertEquals(260.0, stats.totalCost, 0.001)
        // Average mileage: 900 / 80.0 (second + third entry fuel)
        assertEquals(11.25, stats.averageMileage!!, 0.001)
        assertEquals(260.0 / 900.0, stats.costPerKm!!, 0.001)

        val fillups = MileageCalculator.calculatePerFillupMileage(listOf(entry1, entry2, entry3))
        assertEquals(3, fillups.size)
        assertNull(fillups[0].mileageKmPerL)
        assertEquals(12.5, fillups[1].mileageKmPerL!!, 0.001)
        assertEquals(10.0, fillups[2].mileageKmPerL!!, 0.001)
    }

    @Test
    fun testSameOdometerReadingEdgeCase() {
        val entry1 = FuelEntry(id = 1, date = 1000L, odometer = 1000, liters = 50.0, cost = 100.0)
        val entry2 = FuelEntry(id = 2, date = 2000L, odometer = 1000, liters = 40.0, cost = 80.0)
        
        val stats = MileageCalculator.calculateDashboardStats(listOf(entry1, entry2))
        
        assertEquals(1000, stats.latestOdometer)
        assertEquals(0, stats.totalDistance)
        assertNull(stats.averageMileage)
        assertNull(stats.costPerKm)

        val fillups = MileageCalculator.calculatePerFillupMileage(listOf(entry1, entry2))
        assertEquals(2, fillups.size)
        assertNull(fillups[0].mileageKmPerL)
        assertEquals(0.0, fillups[1].mileageKmPerL!!, 0.001)
    }

    @Test
    fun testMonthlySpendEmpty() {
        val monthlySpends = MileageCalculator.calculateMonthlySpend(emptyList())
        assertEquals(0, monthlySpends.size)
    }

    @Test
    fun testMonthlySpendGrouping() {
        val cal = java.util.Calendar.getInstance()

        // Entry 1 in Jan 2026
        cal.set(2026, java.util.Calendar.JANUARY, 10)
        val dateJan1 = cal.timeInMillis
        val entry1 = FuelEntry(id = 1, date = dateJan1, odometer = 1000, liters = 30.0, cost = 3000.0)

        // Entry 2 in Jan 2026
        cal.set(2026, java.util.Calendar.JANUARY, 25)
        val dateJan2 = cal.timeInMillis
        val entry2 = FuelEntry(id = 2, date = dateJan2, odometer = 1400, liters = 25.0, cost = 2500.0)

        // Entry 3 in Feb 2026
        cal.set(2026, java.util.Calendar.FEBRUARY, 15)
        val dateFeb = cal.timeInMillis
        val entry3 = FuelEntry(id = 3, date = dateFeb, odometer = 1800, liters = 35.0, cost = 3500.0)

        // Entry 4 in Apr 2026 (March skipped)
        cal.set(2026, java.util.Calendar.APRIL, 5)
        val dateApr = cal.timeInMillis
        val entry4 = FuelEntry(id = 4, date = dateApr, odometer = 2200, liters = 40.0, cost = 4000.0)

        // Pass entries in shuffled order
        val monthlySpends = MileageCalculator.calculateMonthlySpend(listOf(entry4, entry1, entry3, entry2))

        assertEquals(3, monthlySpends.size)

        // Jan 2026
        assertEquals(2026, monthlySpends[0].year)
        assertEquals(java.util.Calendar.JANUARY, monthlySpends[0].month)
        assertEquals(5500.0, monthlySpends[0].totalCost, 0.001)
        assertEquals(55.0, monthlySpends[0].totalLiters, 0.001)
        assertEquals(2, monthlySpends[0].entryCount)

        // Feb 2026
        assertEquals(2026, monthlySpends[1].year)
        assertEquals(java.util.Calendar.FEBRUARY, monthlySpends[1].month)
        assertEquals(3500.0, monthlySpends[1].totalCost, 0.001)
        assertEquals(35.0, monthlySpends[1].totalLiters, 0.001)
        assertEquals(1, monthlySpends[1].entryCount)

        // Apr 2026
        assertEquals(2026, monthlySpends[2].year)
        assertEquals(java.util.Calendar.APRIL, monthlySpends[2].month)
        assertEquals(4000.0, monthlySpends[2].totalCost, 0.001)
        assertEquals(40.0, monthlySpends[2].totalLiters, 0.001)
        assertEquals(1, monthlySpends[2].entryCount)
    }
}
