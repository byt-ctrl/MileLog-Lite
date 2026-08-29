package com.example.myapplication.domain.export

import com.example.myapplication.data.local.FuelEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class FuelEntryCsvExporterTest {

    @Test
    fun emptyListProducesHeaderOnly() {
        val csv = FuelEntryCsvExporter.buildCsv(emptyList())
        assertEquals("id,date,odometer,liters,cost,Fuel Category", csv)
    }

    @Test
    fun singleEntryProducesHeaderAndOneRow() {
        val entry = FuelEntry(id = 7, date = 1000L, odometer = 1000, liters = 50.0, cost = 100.5, fuelCategory = "Diesel")
        val csv = FuelEntryCsvExporter.buildCsv(listOf(entry))

        val lines = csv.split("\n")
        assertEquals(2, lines.size)
        assertEquals("id,date,odometer,liters,cost,Fuel Category", lines[0])
        assertEquals("7,1000,1000,50.0,100.5,Diesel", lines[1])
    }

    @Test
    fun multipleEntriesPreserveOrderWithOneRowEach() {
        val entry1 = FuelEntry(id = 1, date = 1000L, odometer = 1000, liters = 50.0, cost = 100.0, fuelCategory = "Petrol")
        val entry2 = FuelEntry(id = 2, date = 2000L, odometer = 1500, liters = 40.0, cost = 80.0, fuelCategory = "CNG")
        val entry3 = FuelEntry(id = 3, date = 3000L, odometer = 1900, liters = 40.0, cost = 80.0, fuelCategory = "Diesel")

        // Pass in reverse order to ensure input order is preserved (History is most-recent-first).
        val csv = FuelEntryCsvExporter.buildCsv(listOf(entry3, entry2, entry1))

        val lines = csv.split("\n")
        assertEquals(4, lines.size)
        assertEquals("id,date,odometer,liters,cost,Fuel Category", lines[0])
        assertEquals("3,3000,1900,40.0,80.0,Diesel", lines[1])
        assertEquals("2,2000,1500,40.0,80.0,CNG", lines[2])
        assertEquals("1,1000,1000,50.0,100.0,Petrol", lines[3])
    }

    @Test
    fun noTrailingNewlineAndNoCommasOrQuotesInData() {
        val entry1 = FuelEntry(id = 1, date = 1000L, odometer = 1000, liters = 50.5, cost = 1234.75, fuelCategory = "Petrol")
        val entry2 = FuelEntry(id = 2, date = 2000L, odometer = 1500, liters = 40.25, cost = 987.5, fuelCategory = "Diesel")

        val csv = FuelEntryCsvExporter.buildCsv(listOf(entry1, entry2))

        assertEquals(false, csv.endsWith("\n"))
        // Each data cell is a plain number or enum display name: no embedded commas or quotes beyond the field separators.
        assertEquals(false, csv.contains("\""))
        // Exactly 2 line separators (3 lines) and 5 commas per line (now 6 columns).
        assertEquals(2, csv.count { it == '\n' })
        csv.split("\n").forEach { line ->
            assertEquals(5, line.count { it == ',' })
        }
    }

    @Test
    fun fuelCategoryColumnPopulatedPerEntry() {
        val entryPetrol = FuelEntry(id = 1, date = 1000L, odometer = 1000, liters = 50.0, cost = 100.0, fuelCategory = "Petrol")
        val entryDiesel = FuelEntry(id = 2, date = 2000L, odometer = 1500, liters = 40.0, cost = 80.0, fuelCategory = "Diesel")
        val entryCng = FuelEntry(id = 3, date = 3000L, odometer = 1900, liters = 20.0, cost = 60.0, fuelCategory = "CNG")

        val csv = FuelEntryCsvExporter.buildCsv(listOf(entryPetrol, entryDiesel, entryCng))

        val lines = csv.split("\n")
        assertEquals("id,date,odometer,liters,cost,Fuel Category", lines[0])
        // Fuel Category is the 6th (last) column on each data row.
        assertEquals("Petrol", lines[1].substringAfterLast(","))
        assertEquals("Diesel", lines[2].substringAfterLast(","))
        assertEquals("CNG", lines[3].substringAfterLast(","))
    }
}
