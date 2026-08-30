package com.example.myapplication.domain.calculation

import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.data.local.FuelEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    // ========================================================================
    // Per-Category Mileage Series Tests (U-CALC-01 through U-CALC-05, U-CALC-11, U-CALC-12)
    // ========================================================================

    @Test
    fun testPerCategoryMileageSeries_emptyInput() {
        // U-CALC-01: Empty list → empty result
        val result = MileageCalculator.calculatePerCategoryMileageSeries(emptyList())
        assertEquals(0, result.size)
    }

    @Test
    fun testPerCategoryMileageSeries_groupsByCategory() {
        // U-CALC-02: Petrol entries produce one series, Diesel another
        val entries = listOf(
            FuelEntry(id = 1, date = 1000L, odometer = 1000, liters = 30.0, cost = 300.0, fuelCategory = "Petrol"),
            FuelEntry(id = 2, date = 2000L, odometer = 1500, liters = 25.0, cost = 250.0, fuelCategory = "Petrol"),
            FuelEntry(id = 3, date = 3000L, odometer = 2000, liters = 20.0, cost = 200.0, fuelCategory = "Diesel")
        )

        val result = MileageCalculator.calculatePerCategoryMileageSeries(entries)

        assertEquals(2, result.size)
        val categories = result.map { it.category }.toSet()
        assertTrue(categories.contains(FuelCategory.PETROL))
        assertTrue(categories.contains(FuelCategory.DIESEL))
    }

    @Test
    fun testPerCategoryMileageSeries_computesMileageWithinCategoryIndependently() {
        // U-CALC-03: Mileage is calculated against previous same-category entry, not global
        // Petrol: 1000→1500 (500km / 25L = 20 km/L), 1500→2000 (500km / 25L = 20 km/L), 2000→2500 (500km / 25L = 20 km/L)
        // Diesel: 1600→2200 (600km / 30L = 20 km/L)
        val entries = listOf(
            FuelEntry(id = 1, date = 1000L, odometer = 1000, liters = 30.0, cost = 300.0, fuelCategory = "Petrol"),
            FuelEntry(id = 2, date = 2000L, odometer = 1500, liters = 25.0, cost = 250.0, fuelCategory = "Petrol"),
            FuelEntry(id = 3, date = 1500L, odometer = 1600, liters = 30.0, cost = 300.0, fuelCategory = "Diesel"),
            FuelEntry(id = 4, date = 3000L, odometer = 2000, liters = 25.0, cost = 250.0, fuelCategory = "Petrol"),
            FuelEntry(id = 5, date = 2500L, odometer = 2200, liters = 30.0, cost = 300.0, fuelCategory = "Diesel"),
            FuelEntry(id = 6, date = 4000L, odometer = 2500, liters = 25.0, cost = 250.0, fuelCategory = "Petrol")
        )

        val result = MileageCalculator.calculatePerCategoryMileageSeries(entries)

        val petrolSeries = result.first { it.category == FuelCategory.PETROL }
        val dieselSeries = result.first { it.category == FuelCategory.DIESEL }

        // Petrol: 4 entries, 3 fillups with mileage
        assertEquals(4, petrolSeries.fillups.size)
        assertNull(petrolSeries.fillups[0].mileageKmPerL) // first entry
        assertEquals(20.0, petrolSeries.fillups[1].mileageKmPerL!!, 0.001) // (1500-1000)/25
        assertEquals(20.0, petrolSeries.fillups[2].mileageKmPerL!!, 0.001) // (2000-1500)/25
        assertEquals(20.0, petrolSeries.fillups[3].mileageKmPerL!!, 0.001) // (2500-2000)/25

        // Diesel: 2 entries, 1 fillup with mileage
        assertEquals(2, dieselSeries.fillups.size)
        assertNull(dieselSeries.fillups[0].mileageKmPerL) // first entry
        assertEquals(20.0, dieselSeries.fillups[1].mileageKmPerL!!, 0.001) // (2200-1600)/30
    }

    @Test
    fun testPerCategoryMileageSeries_firstEntryPerCategoryHasNullMileage() {
        // U-CALC-04: First entry in each category → null mileage
        val entries = listOf(
            FuelEntry(id = 1, date = 1000L, odometer = 1000, liters = 30.0, cost = 300.0, fuelCategory = "Petrol"),
            FuelEntry(id = 2, date = 2000L, odometer = 1500, liters = 25.0, cost = 250.0, fuelCategory = "Diesel"),
            FuelEntry(id = 3, date = 3000L, odometer = 2000, liters = 20.0, cost = 200.0, fuelCategory = "CNG")
        )

        val result = MileageCalculator.calculatePerCategoryMileageSeries(entries)

        assertEquals(3, result.size)
        result.forEach { series ->
            assertEquals(1, series.fillups.size)
            assertNull("First entry of ${series.category.name} should have null mileage", series.fillups[0].mileageKmPerL)
        }
    }

    @Test
    fun testPerCategoryMileageSeries_excludesCategoriesWithNoEntries() {
        // U-CALC-05: Categories with 0 entries not in result list
        val entries = listOf(
            FuelEntry(id = 1, date = 1000L, odometer = 1000, liters = 30.0, cost = 300.0, fuelCategory = "Petrol"),
            FuelEntry(id = 2, date = 2000L, odometer = 1500, liters = 25.0, cost = 250.0, fuelCategory = "Petrol")
        )

        val result = MileageCalculator.calculatePerCategoryMileageSeries(entries)

        assertEquals(1, result.size)
        assertEquals(FuelCategory.PETROL, result[0].category)
    }

    @Test
    fun testPerCategoryMileageSeries_mixedCategoriesInSameMonth() {
        // U-CALC-11: Petrol and Diesel entries in same month are separated correctly
        val cal = java.util.Calendar.getInstance()
        cal.set(2026, java.util.Calendar.JANUARY, 10)
        val date1 = cal.timeInMillis
        cal.set(2026, java.util.Calendar.JANUARY, 20)
        val date2 = cal.timeInMillis

        val entries = listOf(
            FuelEntry(id = 1, date = date1, odometer = 1000, liters = 30.0, cost = 300.0, fuelCategory = "Petrol"),
            FuelEntry(id = 2, date = date2, odometer = 1500, liters = 25.0, cost = 250.0, fuelCategory = "Diesel")
        )

        val result = MileageCalculator.calculatePerCategoryMileageSeries(entries)

        assertEquals(2, result.size)
        val petrolSeries = result.first { it.category == FuelCategory.PETROL }
        val dieselSeries = result.first { it.category == FuelCategory.DIESEL }

        assertEquals(1, petrolSeries.fillups.size)
        assertEquals(1, dieselSeries.fillups.size)
        assertEquals(1000, petrolSeries.fillups[0].entry.odometer)
        assertEquals(1500, dieselSeries.fillups[0].entry.odometer)
    }

    @Test
    fun testPerCategoryMileageSeries_preservesChronologicalOrderWithinCategory() {
        // U-CALC-12: Entries sorted by odometer within each category
        val entries = listOf(
            FuelEntry(id = 3, date = 3000L, odometer = 3000, liters = 25.0, cost = 250.0, fuelCategory = "Petrol"),
            FuelEntry(id = 1, date = 1000L, odometer = 1000, liters = 30.0, cost = 300.0, fuelCategory = "Petrol"),
            FuelEntry(id = 2, date = 2000L, odometer = 2000, liters = 20.0, cost = 200.0, fuelCategory = "Petrol")
        )

        val result = MileageCalculator.calculatePerCategoryMileageSeries(entries)

        val petrolSeries = result.first { it.category == FuelCategory.PETROL }
        assertEquals(3, petrolSeries.fillups.size)
        // Should be sorted by odometer: 1000, 2000, 3000
        assertEquals(1000, petrolSeries.fillups[0].entry.odometer)
        assertEquals(2000, petrolSeries.fillups[1].entry.odometer)
        assertEquals(3000, petrolSeries.fillups[2].entry.odometer)
    }

    // ========================================================================
    // Per-Category Monthly Spend Tests (U-CALC-06 through U-CALC-10)
    // ========================================================================

    @Test
    fun testPerCategoryMonthlySpend_emptyInput() {
        // U-CALC-06: Empty list → empty result
        val monthlySpends = MileageCalculator.calculateMonthlySpend(emptyList())
        val result = MileageCalculator.calculatePerCategoryMonthlySpend(emptyList(), monthlySpends)
        assertEquals(0, result.size)
    }

    @Test
    fun testPerCategoryMonthlySpend_groupsCostByCategoryAndMonth() {
        // U-CALC-07: Correct cost per category per month
        val cal = java.util.Calendar.getInstance()

        cal.set(2026, java.util.Calendar.JANUARY, 10)
        val dateJan1 = cal.timeInMillis
        cal.set(2026, java.util.Calendar.JANUARY, 20)
        val dateJan2 = cal.timeInMillis
        cal.set(2026, java.util.Calendar.FEBRUARY, 15)
        val dateFeb = cal.timeInMillis

        val entries = listOf(
            FuelEntry(id = 1, date = dateJan1, odometer = 1000, liters = 30.0, cost = 3000.0, fuelCategory = "Petrol"),
            FuelEntry(id = 2, date = dateJan2, odometer = 1500, liters = 25.0, cost = 2500.0, fuelCategory = "Diesel"),
            FuelEntry(id = 3, date = dateFeb, odometer = 2000, liters = 35.0, cost = 3500.0, fuelCategory = "Petrol")
        )

        val monthlySpends = MileageCalculator.calculateMonthlySpend(entries)
        val result = MileageCalculator.calculatePerCategoryMonthlySpend(entries, monthlySpends)

        // Two categories have entries: Petrol and Diesel
        assertEquals(2, result.size)

        val petrolSeries = result.first { it.category == FuelCategory.PETROL }
        val dieselSeries = result.first { it.category == FuelCategory.DIESEL }

        // Petrol: Jan=3000, Feb=3500
        assertEquals(3000.0, petrolSeries.values[0], 0.001)
        assertEquals(3500.0, petrolSeries.values[1], 0.001)

        // Diesel: Jan=2500, Feb=0
        assertEquals(2500.0, dieselSeries.values[0], 0.001)
        assertEquals(0.0, dieselSeries.values[1], 0.001)
    }

    @Test
    fun testPerCategoryMonthlySpend_valuesArrayAlignedToMonthlySpendsIndices() {
        // U-CALC-08: Each index corresponds to the correct month
        val cal = java.util.Calendar.getInstance()

        cal.set(2026, java.util.Calendar.JANUARY, 10)
        val dateJan = cal.timeInMillis
        cal.set(2026, java.util.Calendar.MARCH, 15)
        val dateMar = cal.timeInMillis
        cal.set(2026, java.util.Calendar.JUNE, 5)
        val dateJun = cal.timeInMillis

        val entries = listOf(
            FuelEntry(id = 1, date = dateJan, odometer = 1000, liters = 30.0, cost = 3000.0, fuelCategory = "Petrol"),
            FuelEntry(id = 2, date = dateMar, odometer = 1500, liters = 25.0, cost = 2500.0, fuelCategory = "Petrol"),
            FuelEntry(id = 3, date = dateJun, odometer = 2000, liters = 35.0, cost = 3500.0, fuelCategory = "Petrol")
        )

        val monthlySpends = MileageCalculator.calculateMonthlySpend(entries)
        // monthlySpends has 3 entries: Jan (index 0), Mar (index 1), Jun (index 2)
        assertEquals(3, monthlySpends.size)
        assertEquals(java.util.Calendar.JANUARY, monthlySpends[0].month)
        assertEquals(java.util.Calendar.MARCH, monthlySpends[1].month)
        assertEquals(java.util.Calendar.JUNE, monthlySpends[2].month)

        val result = MileageCalculator.calculatePerCategoryMonthlySpend(entries, monthlySpends)

        val petrolSeries = result.first { it.category == FuelCategory.PETROL }
        assertEquals(3, petrolSeries.values.size)
        assertEquals(3000.0, petrolSeries.values[0], 0.001) // Jan
        assertEquals(2500.0, petrolSeries.values[1], 0.001) // Mar
        assertEquals(3500.0, petrolSeries.values[2], 0.001) // Jun
    }

    @Test
    fun testPerCategoryMonthlySpend_excludesCategoriesWithZeroTotalSpend() {
        // U-CALC-09: Categories with 0.0 across all months not in result
        val cal = java.util.Calendar.getInstance()
        cal.set(2026, java.util.Calendar.JANUARY, 10)
        val dateJan = cal.timeInMillis

        val entries = listOf(
            FuelEntry(id = 1, date = dateJan, odometer = 1000, liters = 30.0, cost = 3000.0, fuelCategory = "Petrol")
        )

        val monthlySpends = MileageCalculator.calculateMonthlySpend(entries)
        val result = MileageCalculator.calculatePerCategoryMonthlySpend(entries, monthlySpends)

        // Only Petrol has entries; Diesel and CNG should be excluded
        assertEquals(1, result.size)
        assertEquals(FuelCategory.PETROL, result[0].category)
    }

    @Test
    fun testPerCategoryMonthlySpend_handlesSingleCategoryDataset() {
        // U-CALC-10: Only one fuel type present → one series returned
        val cal = java.util.Calendar.getInstance()
        cal.set(2026, java.util.Calendar.JANUARY, 10)
        val dateJan1 = cal.timeInMillis
        cal.set(2026, java.util.Calendar.JANUARY, 20)
        val dateJan2 = cal.timeInMillis

        val entries = listOf(
            FuelEntry(id = 1, date = dateJan1, odometer = 1000, liters = 30.0, cost = 3000.0, fuelCategory = "Diesel"),
            FuelEntry(id = 2, date = dateJan2, odometer = 1500, liters = 25.0, cost = 2500.0, fuelCategory = "Diesel")
        )

        val monthlySpends = MileageCalculator.calculateMonthlySpend(entries)
        val result = MileageCalculator.calculatePerCategoryMonthlySpend(entries, monthlySpends)

        assertEquals(1, result.size)
        assertEquals(FuelCategory.DIESEL, result[0].category)
        assertEquals(5500.0, result[0].values[0], 0.001) // Jan: 3000 + 2500
    }
}
