package com.example.myapplication.domain.export

import com.example.myapplication.data.local.FuelEntry
import com.example.myapplication.domain.calculation.MileageCalculator
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Builds the CSV representation of fuel entries for export.
 *
 * Pure Kotlin (no Android dependencies) so the formatting is unit-testable.
 *
 * Rows are emitted chronologically (odometer ascending) so the per-fill-up
 * mileage can be measured row by row, and that mileage comes from the same
 * [MileageCalculator] the dashboard uses, so the exported figure matches what
 * the app shows. Numbers are written with a fixed precision and a dot decimal
 * separator (never the device locale), and dates as ISO-8601 `yyyy-MM-dd`, so
 * the file opens cleanly in a spreadsheet instead of showing raw epoch values
 * and locale-formatted numbers.
 */
object FuelEntryCsvExporter {

    private const val HEADER =
        "id,date,vehicle,odometer,liters,cost,mileage,fuel_category"

    /** RFC 4180 line ending; what spreadsheet applications expect. */
    private const val LINE_SEPARATOR = "\r\n"

    private const val UTF8_BOM = "\uFEFF"

    private val DATE_FORMAT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    /**
     * Builds the CSV text for [entries].
     *
     * @param entries fuel entries to export; any order, they are sorted
     *   chronologically for output.
     * @param vehicleName name of the vehicle the entries belong to, written on
     *   every row so the file is self-describing.
     * @param zoneId zone used to turn epoch millis into a calendar date,
     *   injectable so the output is reproducible in tests.
     * @return CSV text with a header row and one row per entry. An empty list
     *   produces a header-only file. No trailing newline.
     */
    fun buildCsv(
        entries: List<FuelEntry>,
        vehicleName: String = "",
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        if (entries.isEmpty()) return HEADER

        val fillups = MileageCalculator.calculatePerFillupMileage(entries)
        val rows = fillups.map { fillup ->
            val entry = fillup.entry
            listOf(
                entry.id.toString(),
                formatDate(entry.date, zoneId),
                escape(vehicleName),
                entry.odometer.toString(),
                formatDecimal(entry.liters, 2),
                formatDecimal(entry.cost, 2),
                fillup.mileageKmPerL?.let { formatDecimal(it, 1) }.orEmpty(),
                escape(entry.fuelCategory)
            ).joinToString(",")
        }

        return (listOf(HEADER) + rows).joinToString(LINE_SEPARATOR)
    }

    /**
     * Encodes [csv] as UTF-8 with a byte-order mark. The BOM is what makes
     * Excel read non-ASCII vehicle names correctly instead of mojibake.
     */
    fun encode(csv: String): ByteArray = (UTF8_BOM + csv).toByteArray(Charsets.UTF_8)

    private fun formatDate(epochMillis: Long, zoneId: ZoneId): String =
        Instant.ofEpochMilli(epochMillis)
            .atZone(zoneId)
            .toLocalDate()
            .format(DATE_FORMAT)

    private fun formatDecimal(value: Double, decimals: Int): String =
        String.format(Locale.US, "%.${decimals}f", value)

    /**
     * Quotes a field when it contains a separator, quote or line break, doubling
     * any embedded quotes (RFC 4180).
     */
    private fun escape(value: String): String {
        val needsQuoting = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (needsQuoting) "\"" + value.replace("\"", "\"\"") + "\"" else value
    }
}
