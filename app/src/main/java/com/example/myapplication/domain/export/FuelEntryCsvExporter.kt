package com.example.myapplication.domain.export

import com.example.myapplication.data.local.FuelEntry

/**
 * Builds a simple CSV representation of fuel entries for export.
 *
 * Pure Kotlin (no Android dependencies) so the formatting logic is unit-testable.
 * Values are plain numbers, so no quoting/escaping is required.
 */
object FuelEntryCsvExporter {

    private const val HEADER = "id,date,odometer,liters,cost"
    private const val LINE_SEPARATOR = "\n"

    /**
     * Builds the CSV text for the given entries, preserving their input order.
     *
     * @param entries Fuel entries to export (any order; caller decides).
     * @return CSV text with a header row and one row per entry. An empty list
     *   produces a header-only file. No trailing newline.
     */
    fun buildCsv(entries: List<FuelEntry>): String {
        if (entries.isEmpty()) return HEADER

        val rows = entries.map { entry ->
            listOf(
                entry.id.toString(),
                entry.date.toString(),
                entry.odometer.toString(),
                entry.liters.toString(),
                entry.cost.toString()
            ).joinToString(",")
        }

        return (listOf(HEADER) + rows).joinToString(LINE_SEPARATOR)
    }
}
