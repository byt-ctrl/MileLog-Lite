package com.example.myapplication.domain.calculation

import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.data.local.FuelEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DashboardStats(
    val latestOdometer: Int?,
    val totalDistance: Int,
    val totalFuel: Double,
    val totalCost: Double,
    val averageMileage: Double?,  // km/L
    val costPerKm: Double?
)

data class FillupMileage(
    val entry: FuelEntry,
    val mileageKmPerL: Double?  // null for the first entry
)

data class MonthlyFuelSpend(
    val year: Int,
    val month: Int,        // 0-11 as per Calendar.MONTH
    val label: String,     // e.g. "Aug 26"
    val totalCost: Double,
    val totalLiters: Double,
    val entryCount: Int
)

/**
 * A category-filtered slice of [FillupMileage] used to render a per-category
 * overlay line on the mileage trend chart.
 */
data class CategoryMileageSeries(
    val category: FuelCategory,
    val fillups: List<FillupMileage>
)

/**
 * One bar series for a single fuel category in the monthly spend chart.
 * [monthlyIndex] maps each entry to the canonical month index used by the
 * surrounding [MonthlyFuelSpend] list (0-based, chronological).
 */
data class CategoryMonthlySpendSeries(
    val category: FuelCategory,
    val values: DoubleArray  // aligned to MonthlyFuelSpend by index; 0.0 for no entries
)

object MileageCalculator {
    fun calculateDashboardStats(entries: List<FuelEntry>): DashboardStats {
        if (entries.isEmpty()) {
            return DashboardStats(
                latestOdometer = null,
                totalDistance = 0,
                totalFuel = 0.0,
                totalCost = 0.0,
                averageMileage = null,
                costPerKm = null
            )
        }

        val sortedEntries = entries.sortedBy { it.odometer }

        val latestOdometer = sortedEntries.last().odometer
        val firstOdometer = sortedEntries.first().odometer
        val totalDistance = if (sortedEntries.size >= 2) latestOdometer - firstOdometer else 0

        val totalFuel = sortedEntries.map { it.liters }.sum()
        val totalCost = sortedEntries.map { it.cost }.sum()

        val totalFuelForMileage: Double = if (sortedEntries.size >= 2) {
            sortedEntries.drop(1).map { it.liters }.sum()
        } else {
            0.0
        }

        val averageMileage: Double? = if (totalDistance > 0 && totalFuelForMileage > 0.0) {
            totalDistance.toDouble() / totalFuelForMileage
        } else {
            null
        }

        val costPerKm = if (totalDistance > 0) {
            totalCost / totalDistance.toDouble()
        } else {
            null
        }

        return DashboardStats(
            latestOdometer = latestOdometer,
            totalDistance = totalDistance,
            totalFuel = totalFuel,
            totalCost = totalCost,
            averageMileage = averageMileage,
            costPerKm = costPerKm
        )
    }

    fun calculatePerFillupMileage(entries: List<FuelEntry>): List<FillupMileage> {
        if (entries.isEmpty()) return emptyList()

        val sortedEntries = entries.sortedBy { it.odometer }
        val result = mutableListOf<FillupMileage>()

        result.add(FillupMileage(sortedEntries.first(), null))

        for (i in 1 until sortedEntries.size) {
            val current = sortedEntries[i]
            val previous = sortedEntries[i - 1]

            val distance = current.odometer - previous.odometer
            val mileage = if (current.liters > 0) {
                distance.toDouble() / current.liters
            } else {
                null
            }

            result.add(FillupMileage(current, mileage))
        }

        return result
    }

    /**
     * Computes a per-category mileage series. Within each category, mileage is
     * computed against the previous same-category entry (by odometer). The
     * first entry of each category has null mileage.
     */
    fun calculatePerCategoryMileageSeries(
        entries: List<FuelEntry>
    ): List<CategoryMileageSeries> {
        if (entries.isEmpty()) return emptyList()

        return FuelCategory.entries
            .map { category ->
                val categoryEntries = entries
                    .filter { it.fuelCategory == category.displayName }
                    .sortedBy { it.odometer }
                val fillups = if (categoryEntries.isEmpty()) {
                    emptyList()
                } else {
                    val list = mutableListOf<FillupMileage>()
                    list.add(FillupMileage(categoryEntries.first(), null))
                    for (i in 1 until categoryEntries.size) {
                        val current = categoryEntries[i]
                        val previous = categoryEntries[i - 1]
                        val distance = current.odometer - previous.odometer
                        val mileage = if (current.liters > 0) {
                            distance.toDouble() / current.liters
                        } else {
                            null
                        }
                        list.add(FillupMileage(current, mileage))
                    }
                    list
                }
                CategoryMileageSeries(category = category, fillups = fillups)
            }
            .filter { it.fillups.isNotEmpty() }
    }

    /**
     * Groups entries by calendar month (chronologically ascending) and calculates
     * total fuel spend and liters filled for each month.
     */
    fun calculateMonthlySpend(entries: List<FuelEntry>): List<MonthlyFuelSpend> {
        if (entries.isEmpty()) return emptyList()

        val calendar = Calendar.getInstance()
        val labelFormat = SimpleDateFormat("MMM yy", Locale.getDefault())

        // Group by Pair(year, month)
        val grouped = entries.groupBy { entry ->
            calendar.timeInMillis = entry.date
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            Pair(year, month)
        }

        return grouped.entries
            .sortedWith(compareBy({ it.key.first }, { it.key.second }))
            .map { (yearMonth, monthEntries) ->
                val (year, month) = yearMonth
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, 1)

                val label = labelFormat.format(calendar.time)
                val totalCost = monthEntries.map { it.cost }.sum()
                val totalLiters = monthEntries.map { it.liters }.sum()

                MonthlyFuelSpend(
                    year = year,
                    month = month,
                    label = label,
                    totalCost = totalCost,
                    totalLiters = totalLiters,
                    entryCount = monthEntries.size
                )
            }
    }

    /**
     * Builds one [CategoryMonthlySpendSeries] per [FuelCategory] that has any
     * entries. Each series has one value per month in [monthlySpends] (0.0 when
     * the category has no entries in that month).
     */
    fun calculatePerCategoryMonthlySpend(
        entries: List<FuelEntry>,
        monthlySpends: List<MonthlyFuelSpend>
    ): List<CategoryMonthlySpendSeries> {
        if (monthlySpends.isEmpty() || entries.isEmpty()) return emptyList()

        val calendar = Calendar.getInstance()

        return FuelCategory.entries
            .map { category ->
                val values = DoubleArray(monthlySpends.size)
                entries
                    .filter { it.fuelCategory == category.displayName }
                    .forEach { entry ->
                        calendar.timeInMillis = entry.date
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val idx = monthlySpends.indexOfFirst {
                            it.year == year && it.month == month
                        }
                        if (idx >= 0) values[idx] += entry.cost
                    }
                CategoryMonthlySpendSeries(category = category, values = values)
            }
            .filter { series -> series.values.any { it > 0.0 } }
    }
}
