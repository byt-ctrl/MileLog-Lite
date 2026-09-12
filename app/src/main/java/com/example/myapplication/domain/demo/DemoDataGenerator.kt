package com.example.myapplication.domain.demo

import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.data.local.FuelEntry
import kotlin.random.Random

/**
 * A distinct fill-up history shape for one vehicle variant.
 *
 * @property name Vehicle display name, also the identity used to avoid creating
 *   the same demo vehicle twice.
 * @property make Manufacturer.
 * @property model Model line.
 * @property fuelCategory Fuel the variant burns; every generated row carries it.
 * @property startOdometer Odometer the first generated fill-up continues from.
 * @property minMileage Lower bound of the distance-per-unit band.
 * @property maxMileage Upper bound of the distance-per-unit band.
 * @property minPricePerUnit Lower bound of the fuel price band.
 * @property maxPricePerUnit Upper bound of the fuel price band.
 */
data class DemoVehicleProfile(
    val name: String,
    val make: String,
    val model: String,
    val fuelCategory: FuelCategory,
    val startOdometer: Int,
    val minMileage: Double,
    val maxMileage: Double,
    val minPricePerUnit: Double,
    val maxPricePerUnit: Double
)

/**
 * Builds plausible fill-up histories so a fresh install can show its dashboard
 * and charts for more than one vehicle.
 *
 * Every profile produces its own run of rows: dates and odometers continue after
 * that vehicle's newest real reading, so a demo run never collides with logged
 * data and the mileage maths stays monotonic. Output is deterministic for a given
 * profile, log and clock, so the same demo appears every time instead of
 * reshuffling on each tap.
 */
object DemoDataGenerator {

    const val DEFAULT_ENTRY_COUNT = 7

    private const val MIN_DISTANCE = 380
    private const val MAX_DISTANCE = 620
    private const val MIN_GAP_DAYS = 7
    private const val MAX_GAP_DAYS = 13
    private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

    /**
     * Six distinct demo vehicles: Creta, Seltos and Harrier, each with a Diesel
     * and a CNG profile. Bands are per-variant so switching vehicles visibly
     * changes the dashboard and charts.
     */
    val profiles: List<DemoVehicleProfile> = listOf(
        DemoVehicleProfile(
            name = "Hyundai Creta Diesel",
            make = "Hyundai",
            model = "Creta",
            fuelCategory = FuelCategory.DIESEL,
            startOdometer = 42_000,
            minMileage = 15.5,
            maxMileage = 18.0,
            minPricePerUnit = 89.0,
            maxPricePerUnit = 92.0
        ),
        DemoVehicleProfile(
            name = "Hyundai Creta CNG",
            make = "Hyundai",
            model = "Creta",
            fuelCategory = FuelCategory.CNG,
            startOdometer = 38_500,
            minMileage = 20.0,
            maxMileage = 24.0,
            minPricePerUnit = 74.0,
            maxPricePerUnit = 78.0
        ),
        DemoVehicleProfile(
            name = "Kia Seltos Diesel",
            make = "Kia",
            model = "Seltos",
            fuelCategory = FuelCategory.DIESEL,
            startOdometer = 51_000,
            minMileage = 14.5,
            maxMileage = 17.0,
            minPricePerUnit = 89.0,
            maxPricePerUnit = 92.0
        ),
        DemoVehicleProfile(
            name = "Kia Seltos CNG",
            make = "Kia",
            model = "Seltos",
            fuelCategory = FuelCategory.CNG,
            startOdometer = 46_000,
            minMileage = 19.0,
            maxMileage = 23.0,
            minPricePerUnit = 74.0,
            maxPricePerUnit = 78.0
        ),
        DemoVehicleProfile(
            name = "Tata Harrier Diesel",
            make = "Tata",
            model = "Harrier",
            fuelCategory = FuelCategory.DIESEL,
            startOdometer = 33_000,
            minMileage = 13.5,
            maxMileage = 16.0,
            minPricePerUnit = 89.0,
            maxPricePerUnit = 92.0
        ),
        DemoVehicleProfile(
            name = "Tata Harrier CNG",
            make = "Tata",
            model = "Harrier",
            fuelCategory = FuelCategory.CNG,
            startOdometer = 29_500,
            minMileage = 18.0,
            maxMileage = 22.0,
            minPricePerUnit = 74.0,
            maxPricePerUnit = 78.0
        )
    )

    /**
     * @param profile the variant whose bands and fuel type shape the rows.
     * @param vehicleId the vehicle the generated rows belong to.
     * @param existing that vehicle's current log, used to continue after its
     *   newest reading. Pass an empty list for a fresh vehicle.
     * @param count how many entries to build.
     * @param now the reference instant, injected so the output is reproducible.
     */
    fun generateForVehicle(
        profile: DemoVehicleProfile,
        vehicleId: Long,
        existing: List<FuelEntry>,
        count: Int = DEFAULT_ENTRY_COUNT,
        now: Long = System.currentTimeMillis()
    ): List<FuelEntry> {
        require(count > 0) { "count must be positive" }

        val random = Random(profile.name.hashCode() * 31 + existing.size * 31 + count)

        val gaps = List(count) { random.nextInt(MIN_GAP_DAYS, MAX_GAP_DAYS + 1) }
        val totalGap = gaps.sum().toLong() * MILLIS_PER_DAY

        val latestDate = existing.maxOfOrNull { it.date }
        var date = when {
            latestDate == null -> now - totalGap
            latestDate + totalGap <= now -> now - totalGap
            // Not enough room before today; keep going forward from the log.
            else -> latestDate
        }
        var odometer = existing.maxOfOrNull { it.odometer } ?: profile.startOdometer

        val rows = mutableListOf<FuelEntry>()
        gaps.forEach { gapDays ->
            date += gapDays.toLong() * MILLIS_PER_DAY
            val distance = random.nextInt(MIN_DISTANCE, MAX_DISTANCE + 1)
            odometer += distance
            val mileage = profile.minMileage + random.nextDouble() * (profile.maxMileage - profile.minMileage)
            val units = distance / mileage
            val pricePerUnit = profile.minPricePerUnit +
                random.nextDouble() * (profile.maxPricePerUnit - profile.minPricePerUnit)

            rows += FuelEntry(
                vehicleId = vehicleId,
                date = date,
                odometer = odometer,
                liters = round2(units),
                cost = round2(units * pricePerUnit),
                fuelCategory = profile.fuelCategory.displayName
            )
        }

        return rows
    }

    private fun round2(value: Double): Double = Math.round(value * 100.0) / 100.0
}
