package com.example.myapplication.domain.demo

import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.data.local.FuelEntry
import kotlin.random.Random

/**
 * Builds a plausible fill-up history so a fresh install can show its charts.
 *
 * Rows are appended after the newest real reading in both date and odometer
 * order, so a demo run never collides with a real log and the mileage maths
 * stays monotonic. Dates land near "now" whenever the existing log leaves room
 * for that.
 *
 * Deterministic for a given log and clock, so the same demo appears every time
 * instead of reshuffling on each tap.
 *
 * Everything is petrol: the app tracks one vehicle, and mixing km/L with the
 * km/kg a CNG fill-up returns would corrupt the single mileage trend rather
 * than enrich it.
 */
object DemoDataGenerator {

    const val DEFAULT_ENTRY_COUNT = 7

    private const val START_ODOMETER = 18_400
    private const val MIN_DISTANCE = 380
    private const val MAX_DISTANCE = 620
    private const val MIN_MILEAGE = 14.5
    private const val MAX_MILEAGE = 19.5
    private const val MIN_PRICE_PER_LITRE = 96.0
    private const val MAX_PRICE_PER_LITRE = 110.0
    private const val MIN_GAP_DAYS = 7
    private const val MAX_GAP_DAYS = 13
    private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

    /**
     * @param existing the current log, used to continue after its newest
     *   reading. Pass an empty list for a fresh install.
     * @param count how many entries to build.
     * @param now the reference instant, injected so the output is reproducible.
     */
    fun generate(
        existing: List<FuelEntry>,
        count: Int = DEFAULT_ENTRY_COUNT,
        now: Long = System.currentTimeMillis()
    ): List<FuelEntry> {
        require(count > 0) { "count must be positive" }

        val random = Random(existing.size * 31 + count)

        val gaps = List(count) { random.nextInt(MIN_GAP_DAYS, MAX_GAP_DAYS + 1) }
        val totalGap = gaps.sum().toLong() * MILLIS_PER_DAY

        val latestDate = existing.maxOfOrNull { it.date }
        var date = when {
            latestDate == null -> now - totalGap
            latestDate + totalGap <= now -> now - totalGap
            // Not enough room before today; keep going forward from the log.
            else -> latestDate
        }
        var odometer = existing.maxOfOrNull { it.odometer } ?: START_ODOMETER

        val rows = mutableListOf<FuelEntry>()
        gaps.forEach { gapDays ->
            date += gapDays.toLong() * MILLIS_PER_DAY
            val distance = random.nextInt(MIN_DISTANCE, MAX_DISTANCE + 1)
            odometer += distance
            val mileage = MIN_MILEAGE + random.nextDouble() * (MAX_MILEAGE - MIN_MILEAGE)
            val liters = distance / mileage
            val pricePerLitre = MIN_PRICE_PER_LITRE +
                random.nextDouble() * (MAX_PRICE_PER_LITRE - MIN_PRICE_PER_LITRE)

            rows += FuelEntry(
                date = date,
                odometer = odometer,
                liters = round2(liters),
                cost = round2(liters * pricePerLitre),
                fuelCategory = FuelCategory.PETROL.displayName
            )
        }

        return rows
    }

    private fun round2(value: Double): Double = Math.round(value * 100.0) / 100.0
}
