package com.example.myapplication.domain.export

import com.example.myapplication.data.local.FuelEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset

class FuelEntryCsvExporterTest {

    private val utc = ZoneOffset.UTC
    private val header = "id,date,vehicle,odometer,liters,cost,mileage,fuel_category"

    private val first = FuelEntry(
        id = 1,
        date = 0L,
        odometer = 1000,
        liters = 50.0,
        cost = 100.5,
        fuelCategory = "Petrol"
    )
    private val second = FuelEntry(
        id = 2,
        date = 86_400_000L,
        odometer = 1500,
        liters = 40.0,
        cost = 80.0,
        fuelCategory = "Diesel"
    )
    private val third = FuelEntry(
        id = 3,
        date = 172_800_000L,
        odometer = 1900,
        liters = 40.0,
        cost = 80.0,
        fuelCategory = "CNG"
    )

    @Test
    fun emptyListProducesHeaderOnly() {
        val csv = FuelEntryCsvExporter.buildCsv(emptyList(), "Test Car", utc)

        assertEquals(header, csv)
    }

    @Test
    fun headerUsesConsistentSnakeCaseColumns() {
        val csv = FuelEntryCsvExporter.buildCsv(listOf(first), "Test Car", utc)

        assertEquals(header, csv.split("\r\n").first())
    }

    @Test
    fun singleEntryHasHeaderAndBlankMileageForTheBaselineFillUp() {
        val csv = FuelEntryCsvExporter.buildCsv(listOf(first), "Test Car", utc)

        val lines = csv.split("\r\n")
        assertEquals(2, lines.size)
        assertEquals(header, lines[0])
        assertEquals("1,1970-01-01,Test Car,1000,50.00,100.50,,Petrol", lines[1])
    }

    @Test
    fun dateIsWrittenAsIsoDateNotEpochMillis() {
        val csv = FuelEntryCsvExporter.buildCsv(listOf(second), "Test Car", utc)

        val row = csv.split("\r\n")[1]
        assertEquals("1970-01-02", row.split(",")[1])
        assertFalse(row.contains("86400000"))
    }

    @Test
    fun rowsAreEmittedChronologicallyRegardlessOfInputOrder() {
        // History passes most-recent-first; the export must read like a logbook.
        val csv = FuelEntryCsvExporter.buildCsv(listOf(third, first, second), "Test Car", utc)

        val odometers = csv.split("\r\n").drop(1).map { it.split(",")[3] }
        assertEquals(listOf("1000", "1500", "1900"), odometers)
    }

    @Test
    fun mileageIsComputedForEveryFillUpAfterTheFirst() {
        val csv = FuelEntryCsvExporter.buildCsv(listOf(first, second, third), "Test Car", utc)

        val lines = csv.split("\r\n")
        // Mileage is the last-but-one column: (odometer delta) / litres.
        assertEquals("", lines[1].split(",")[6])
        assertEquals("12.5", lines[2].split(",")[6])
        assertEquals("10.0", lines[3].split(",")[6])
    }

    @Test
    fun numbersUseFixedPrecisionAndADotDecimalSeparator() {
        val entry = FuelEntry(
            id = 9,
            date = 0L,
            odometer = 1234,
            liters = 33.333333,
            cost = 1234.5,
            fuelCategory = "Petrol"
        )

        val csv = FuelEntryCsvExporter.buildCsv(listOf(entry), "Test Car", utc)
        val row = csv.split("\r\n")[1]

        assertEquals("33.33", row.split(",")[4])
        assertEquals("1234.50", row.split(",")[5])
        assertFalse(row.contains(';'))
    }

    @Test
    fun vehicleNameContainingASeparatorIsQuoted() {
        val csv = FuelEntryCsvExporter.buildCsv(listOf(first), "Creta, Diesel", utc)

        val row = csv.split("\r\n")[1]
        assertTrue(row.contains("\"Creta, Diesel\""))
    }

    @Test
    fun quotesInsideTheVehicleNameAreDoubled() {
        val csv = FuelEntryCsvExporter.buildCsv(listOf(first), "Seltos \"X\"", utc)

        val row = csv.split("\r\n")[1]
        assertTrue(row.contains("\"Seltos \"\"X\"\"\""))
    }

    @Test
    fun linesUseCrlfAndTheFileHasNoTrailingNewline() {
        val csv = FuelEntryCsvExporter.buildCsv(listOf(first, second), "Test Car", utc)

        assertEquals(false, csv.endsWith("\n"))
        assertEquals(2, csv.split("\r\n").size - 1)
        // Every newline is part of a CRLF pair.
        assertFalse(csv.replace("\r\n", "").contains("\n"))
    }

    @Test
    fun encodePrefixesAUtf8ByteOrderMark() {
        val bytes = FuelEntryCsvExporter.encode("id,date")

        assertEquals(0xEF, bytes[0].toInt() and 0xFF)
        assertEquals(0xBB, bytes[1].toInt() and 0xFF)
        assertEquals(0xBF, bytes[2].toInt() and 0xFF)
        assertEquals("id,date", bytes.copyOfRange(3, bytes.size).toString(Charsets.UTF_8))
    }

    @Test
    fun fuelCategoryIsTheLastColumnOnEveryRow() {
        val csv = FuelEntryCsvExporter.buildCsv(listOf(first, second, third), "Test Car", utc)

        val lines = csv.split("\r\n")
        assertEquals("Petrol", lines[1].substringAfterLast(","))
        assertEquals("Diesel", lines[2].substringAfterLast(","))
        assertEquals("CNG", lines[3].substringAfterLast(","))
    }

    @Test
    fun entriesWithDefaultCategoryExportAsPetrol() {
        val entryDefault = FuelEntry(date = 0L, odometer = 1000, liters = 50.0, cost = 100.0)

        val csv = FuelEntryCsvExporter.buildCsv(listOf(entryDefault), "Test Car", utc)

        assertEquals("Petrol", csv.split("\r\n")[1].substringAfterLast(","))
    }

    @Test
    fun exportPreservesCategoryAfterEdit() {
        val entry = FuelEntry(
            id = 1,
            date = 0L,
            odometer = 1000,
            liters = 50.0,
            cost = 100.0,
            fuelCategory = "Petrol"
        )
        val edited = entry.copy(fuelCategory = "CNG")

        val csv = FuelEntryCsvExporter.buildCsv(listOf(edited), "Test Car", utc)

        assertEquals("CNG", csv.split("\r\n")[1].substringAfterLast(","))
    }
}
