package com.example.myapplication

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.local.FuelEntry
import com.example.myapplication.data.local.FuelEntryDao
import com.example.myapplication.data.local.MileLiteDatabase
import com.example.myapplication.data.repository.FuelEntryRepository
import com.example.myapplication.data.repository.OfflineFuelEntryRepository
import com.example.myapplication.domain.calculation.MileageCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

/**
 * Full regression pass: add → edit → delete → dashboard reflects change → charts reflect change.
 *
 * Exercises the complete data pipeline (DAO → Repository → MileageCalculator)
 * without UI or ViewModel complexity, ensuring every CRUD operation propagates
 * correctly to dashboard KPIs and chart data series.
 */
@RunWith(AndroidJUnit4::class)
class FullRegressionTest {

    private lateinit var database: MileLiteDatabase
    private lateinit var dao: FuelEntryDao
    private lateinit var repository: FuelEntryRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MileLiteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.fuelEntryDao()
        repository = OfflineFuelEntryRepository(dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun fullRegression_addThenEditThenDelete() = runBlocking {
        // ── Starting state: empty ──────────────────────────────────
        val entries0 = repository.getAllEntries()
        assertEquals(0, entries0.size)

        val dash0 = MileageCalculator.calculateDashboardStats(entries0)
        assertNull(dash0.latestOdometer)
        assertNull(dash0.averageMileage)
        assertNull(dash0.costPerKm)
        assertEquals(0.0, dash0.totalCost, 0.001)

        val chartFillups0 = MileageCalculator.calculatePerFillupMileage(entries0)
        assertEquals(0, chartFillups0.size)
        val chartMonthly0 = MileageCalculator.calculateMonthlySpend(entries0)
        assertEquals(0, chartMonthly0.size)

        // ── STEP 1: Add first entry ────────────────────────────────
        val date1 = Calendar.getInstance().apply { set(2026, Calendar.JANUARY, 15) }.timeInMillis
        val entry1 = FuelEntry(date = date1, odometer = 10000, liters = 30.0, cost = 3000.0)
        val id1 = repository.insertEntry(entry1)
        assertTrue(id1 > 0)

        val entries1 = repository.getAllEntries()
        assertEquals(1, entries1.size)
        assertEquals(10000, entries1[0].odometer)

        // Dashboard: single entry → mileage/costPerKm null, totals correct
        val dash1 = MileageCalculator.calculateDashboardStats(entries1)
        assertEquals(10000, dash1.latestOdometer)
        assertEquals(0, dash1.totalDistance)
        assertEquals(30.0, dash1.totalFuel, 0.001)
        assertEquals(3000.0, dash1.totalCost, 0.001)
        assertNull(dash1.averageMileage)
        assertNull(dash1.costPerKm)

        // Charts: 1 fillup (no mileage), 1 monthly bar
        val chartFillups1 = MileageCalculator.calculatePerFillupMileage(entries1)
        assertEquals(1, chartFillups1.size)
        assertNull(chartFillups1[0].mileageKmPerL)
        val chartMonthly1 = MileageCalculator.calculateMonthlySpend(entries1)
        assertEquals(1, chartMonthly1.size)
        assertEquals(3000.0, chartMonthly1[0].totalCost, 0.001)

        // ── STEP 2: Add second entry ───────────────────────────────
        val date2 = Calendar.getInstance().apply { set(2026, Calendar.FEBRUARY, 10) }.timeInMillis
        val entry2 = FuelEntry(date = date2, odometer = 10500, liters = 25.0, cost = 2500.0)
        val id2 = repository.insertEntry(entry2)
        assertTrue(id2 > 0)

        val entries2 = repository.getAllEntries()
        assertEquals(2, entries2.size)

        // Dashboard: two entries → mileage and costPerKm computed
        val dash2 = MileageCalculator.calculateDashboardStats(entries2)
        assertEquals(10500, dash2.latestOdometer)
        assertEquals(500, dash2.totalDistance)
        assertEquals(55.0, dash2.totalFuel, 0.001)
        assertEquals(5500.0, dash2.totalCost, 0.001)
        // averageMileage = 500 / 25.0 (2nd entry fuel) = 20.0
        assertEquals(20.0, dash2.averageMileage!!, 0.001)
        // costPerKm = 5500 / 500 = 11.0
        assertEquals(11.0, dash2.costPerKm!!, 0.001)

        // Charts: 2 fillups (first null, second 20.0 km/L), 2 monthly bars
        val chartFillups2 = MileageCalculator.calculatePerFillupMileage(entries2)
        assertEquals(2, chartFillups2.size)
        assertNull(chartFillups2[0].mileageKmPerL)
        assertEquals(20.0, chartFillups2[1].mileageKmPerL!!, 0.001)
        val chartMonthly2 = MileageCalculator.calculateMonthlySpend(entries2)
        assertEquals(2, chartMonthly2.size)
        assertEquals(3000.0, chartMonthly2[0].totalCost, 0.001)
        assertEquals(2500.0, chartMonthly2[1].totalCost, 0.001)

        // ── STEP 3: Add third entry ────────────────────────────────
        val date3 = Calendar.getInstance().apply { set(2026, Calendar.MARCH, 5) }.timeInMillis
        val entry3 = FuelEntry(date = date3, odometer = 11000, liters = 28.0, cost = 2800.0)
        val id3 = repository.insertEntry(entry3)
        assertTrue(id3 > 0)

        val entries3 = repository.getAllEntries()
        assertEquals(3, entries3.size)

        val dash3 = MileageCalculator.calculateDashboardStats(entries3)
        assertEquals(11000, dash3.latestOdometer)
        assertEquals(1000, dash3.totalDistance)
        assertEquals(83.0, dash3.totalFuel, 0.001)
        assertEquals(8300.0, dash3.totalCost, 0.001)
        // averageMileage = 1000 / (25 + 28) = 1000 / 53
        assertEquals(1000.0 / 53.0, dash3.averageMileage!!, 0.001)
        assertEquals(8300.0 / 1000.0, dash3.costPerKm!!, 0.001)

        val chartFillups3 = MileageCalculator.calculatePerFillupMileage(entries3)
        assertEquals(3, chartFillups3.size)
        assertNull(chartFillups3[0].mileageKmPerL)
        assertEquals(20.0, chartFillups3[1].mileageKmPerL!!, 0.001)
        // 3rd fillup: (11000 - 10500) / 28 = 500 / 28 ≈ 17.857
        assertEquals(500.0 / 28.0, chartFillups3[2].mileageKmPerL!!, 0.001)
        val chartMonthly3 = MileageCalculator.calculateMonthlySpend(entries3)
        assertEquals(3, chartMonthly3.size)

        // ── STEP 4: Edit second entry (odometer, liters, cost) ─────
        val loadedEntry2 = repository.getEntryById(id2)
        assertNotNull(loadedEntry2)
        val editedEntry2 = loadedEntry2!!.copy(odometer = 10600, liters = 26.0, cost = 2600.0)
        repository.updateEntry(editedEntry2)

        val entriesAfterEdit = repository.getAllEntries()
        assertEquals(3, entriesAfterEdit.size)

        // Dashboard recalculates with updated values
        val dashAfterEdit = MileageCalculator.calculateDashboardStats(entriesAfterEdit)
        assertEquals(11000, dashAfterEdit.latestOdometer)
        assertEquals(1000, dashAfterEdit.totalDistance)
        assertEquals(84.0, dashAfterEdit.totalFuel, 0.001)  // 30 + 26 + 28
        assertEquals(8400.0, dashAfterEdit.totalCost, 0.001) // 3000 + 2600 + 2800
        // averageMileage = 1000 / (26 + 28) = 1000 / 54
        assertEquals(1000.0 / 54.0, dashAfterEdit.averageMileage!!, 0.001)
        assertEquals(8400.0 / 1000.0, dashAfterEdit.costPerKm!!, 0.001)

        // Charts recalculated with updated mileage per fill-up
        val chartFillupsAfterEdit = MileageCalculator.calculatePerFillupMileage(entriesAfterEdit)
        assertEquals(3, chartFillupsAfterEdit.size)
        assertNull(chartFillupsAfterEdit[0].mileageKmPerL)
        // 2nd fillup: (10600 - 10000) / 26 = 600 / 26 ≈ 23.077
        assertEquals(600.0 / 26.0, chartFillupsAfterEdit[1].mileageKmPerL!!, 0.001)
        // 3rd fillup: (11000 - 10600) / 28 = 400 / 28 ≈ 14.286
        assertEquals(400.0 / 28.0, chartFillupsAfterEdit[2].mileageKmPerL!!, 0.001)

        // Monthly spend updated for Feb
        val chartMonthlyAfterEdit = MileageCalculator.calculateMonthlySpend(entriesAfterEdit)
        assertEquals(3, chartMonthlyAfterEdit.size)
        assertEquals(2600.0, chartMonthlyAfterEdit[1].totalCost, 0.001)  // Feb updated

        // ── STEP 5: Delete the third entry ─────────────────────────
        val entry3ToDelete = entriesAfterEdit.first { it.id == id3 }
        repository.deleteEntry(entry3ToDelete)

        val entriesAfterDelete = repository.getAllEntries()
        assertEquals(2, entriesAfterDelete.size)

        // Dashboard: back to two entries, recalculates with updated entry2
        val dashAfterDelete = MileageCalculator.calculateDashboardStats(entriesAfterDelete)
        assertEquals(10600, dashAfterDelete.latestOdometer)
        assertEquals(600, dashAfterDelete.totalDistance)
        assertEquals(56.0, dashAfterDelete.totalFuel, 0.001)  // 30 + 26
        assertEquals(5600.0, dashAfterDelete.totalCost, 0.001) // 3000 + 2600
        // averageMileage = 600 / 26
        assertEquals(600.0 / 26.0, dashAfterDelete.averageMileage!!, 0.001)
        assertEquals(5600.0 / 600.0, dashAfterDelete.costPerKm!!, 0.001)

        // Charts: 2 fillups, 2 monthly bars (Jan + Feb only)
        val chartFillupsAfterDelete = MileageCalculator.calculatePerFillupMileage(entriesAfterDelete)
        assertEquals(2, chartFillupsAfterDelete.size)
        assertNull(chartFillupsAfterDelete[0].mileageKmPerL)
        assertEquals(600.0 / 26.0, chartFillupsAfterDelete[1].mileageKmPerL!!, 0.001)
        val chartMonthlyAfterDelete = MileageCalculator.calculateMonthlySpend(entriesAfterDelete)
        assertEquals(2, chartMonthlyAfterDelete.size)
        assertEquals("Jan", chartMonthlyAfterDelete[0].label.take(3))
        assertEquals("Feb", chartMonthlyAfterDelete[1].label.take(3))

        // ── STEP 6: Delete the second entry ────────────────────────
        val entry2ToDelete = entriesAfterDelete.first { it.id == id2 }
        repository.deleteEntry(entry2ToDelete)

        val entriesFinal = repository.getAllEntries()
        assertEquals(1, entriesFinal.size)
        assertEquals(id1, entriesFinal[0].id)

        // Dashboard: single entry → back to nulls
        val dashFinal = MileageCalculator.calculateDashboardStats(entriesFinal)
        assertEquals(10000, dashFinal.latestOdometer)
        assertEquals(0, dashFinal.totalDistance)
        assertEquals(30.0, dashFinal.totalFuel, 0.001)
        assertEquals(3000.0, dashFinal.totalCost, 0.001)
        assertNull(dashFinal.averageMileage)
        assertNull(dashFinal.costPerKm)

        // Charts: 1 fillup, 1 monthly bar
        val chartFillupsFinal = MileageCalculator.calculatePerFillupMileage(entriesFinal)
        assertEquals(1, chartFillupsFinal.size)
        assertNull(chartFillupsFinal[0].mileageKmPerL)
        val chartMonthlyFinal = MileageCalculator.calculateMonthlySpend(entriesFinal)
        assertEquals(1, chartMonthlyFinal.size)
        assertEquals(3000.0, chartMonthlyFinal[0].totalCost, 0.001)

        // ── STEP 7: Delete the last entry ──────────────────────────
        repository.deleteEntry(entriesFinal[0])

        val entriesEmpty = repository.getAllEntries()
        assertEquals(0, entriesEmpty.size)

        val dashEmpty = MileageCalculator.calculateDashboardStats(entriesEmpty)
        assertNull(dashEmpty.latestOdometer)
        assertEquals(0, dashEmpty.totalDistance)
        assertEquals(0.0, dashEmpty.totalFuel, 0.001)
        assertEquals(0.0, dashEmpty.totalCost, 0.001)
        assertNull(dashEmpty.averageMileage)
        assertNull(dashEmpty.costPerKm)

        val chartFillupsEmpty = MileageCalculator.calculatePerFillupMileage(entriesEmpty)
        assertEquals(0, chartFillupsEmpty.size)
        val chartMonthlyEmpty = MileageCalculator.calculateMonthlySpend(entriesEmpty)
        assertEquals(0, chartMonthlyEmpty.size)

        // ── STEP 8: Re-add entry after full clear ──────────────────
        val reEntry = FuelEntry(date = date1, odometer = 20000, liters = 40.0, cost = 4000.0)
        val reId = repository.insertEntry(reEntry)
        assertTrue(reId > 0)

        val entriesReadd = repository.getAllEntries()
        assertEquals(1, entriesReadd.size)

        val dashReadd = MileageCalculator.calculateDashboardStats(entriesReadd)
        assertEquals(20000, dashReadd.latestOdometer)
        assertEquals(0, dashReadd.totalDistance)
        assertEquals(40.0, dashReadd.totalFuel, 0.001)
        assertEquals(4000.0, dashReadd.totalCost, 0.001)
        assertNull(dashReadd.averageMileage)
        assertNull(dashReadd.costPerKm)

        // Verify Flow also reflects the current state
        val flowEntries = repository.getAllEntriesFlow().first()
        assertEquals(1, flowEntries.size)
        assertEquals(20000, flowEntries[0].odometer)
    }

    @Test
    fun fullRegression_editDoesNotBreakDashboardOrCharts() = runBlocking {
        val date1 = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 1) }.timeInMillis
        val date2 = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 15) }.timeInMillis
        val date3 = Calendar.getInstance().apply { set(2026, Calendar.JULY, 1) }.timeInMillis

        val id1 = repository.insertEntry(FuelEntry(date = date1, odometer = 5000, liters = 20.0, cost = 2000.0))
        val id2 = repository.insertEntry(FuelEntry(date = date2, odometer = 5400, liters = 22.0, cost = 2200.0))
        val id3 = repository.insertEntry(FuelEntry(date = date3, odometer = 5800, liters = 24.0, cost = 2400.0))

        // Baseline
        val entries0 = repository.getAllEntries()
        val dash0 = MileageCalculator.calculateDashboardStats(entries0)
        val fillups0 = MileageCalculator.calculatePerFillupMileage(entries0)
        val monthly0 = MileageCalculator.calculateMonthlySpend(entries0)
        assertEquals(3, entries0.size)
        assertEquals(3, fillups0.size)
        assertEquals(2, monthly0.size) // Jun + Jul

        // Edit entry2: increase odometer and cost
        val e2 = repository.getEntryById(id2)!!.copy(odometer = 5500, liters = 23.0, cost = 2300.0)
        repository.updateEntry(e2)

        val entries1 = repository.getAllEntries()
        val dash1 = MileageCalculator.calculateDashboardStats(entries1)
        assertEquals(5800, dash1.latestOdometer)
        assertEquals(800, dash1.totalDistance)  // 5800 - 5000
        assertEquals(67.0, dash1.totalFuel, 0.001) // 20 + 23 + 24
        assertEquals(6700.0, dash1.totalCost, 0.001) // 2000 + 2300 + 2400
        assertEquals(800.0 / (23.0 + 24.0), dash1.averageMileage!!, 0.001)
        assertEquals(6700.0 / 800.0, dash1.costPerKm!!, 0.001)

        val fillups1 = MileageCalculator.calculatePerFillupMileage(entries1)
        assertEquals(3, fillups1.size)
        assertNull(fillups1[0].mileageKmPerL)
        assertEquals(500.0 / 23.0, fillups1[1].mileageKmPerL!!, 0.001) // (5500-5000)/23
        assertEquals(300.0 / 24.0, fillups1[2].mileageKmPerL!!, 0.001) // (5800-5500)/24

        val monthly1 = MileageCalculator.calculateMonthlySpend(entries1)
        assertEquals(2, monthly1.size)
        assertEquals(4300.0, monthly1[0].totalCost, 0.001) // Jun: 2000 + 2300
        assertEquals(2400.0, monthly1[1].totalCost, 0.001) // Jul: 2400
    }

    @Test
    fun fullRegression_deleteMiddleEntry() = runBlocking {
        val date1 = Calendar.getInstance().apply { set(2026, Calendar.APRIL, 1) }.timeInMillis
        val date2 = Calendar.getInstance().apply { set(2026, Calendar.APRIL, 15) }.timeInMillis
        val date3 = Calendar.getInstance().apply { set(2026, Calendar.MAY, 1) }.timeInMillis

        val id1 = repository.insertEntry(FuelEntry(date = date1, odometer = 8000, liters = 30.0, cost = 3000.0))
        val id2 = repository.insertEntry(FuelEntry(date = date2, odometer = 8400, liters = 32.0, cost = 3200.0))
        val id3 = repository.insertEntry(FuelEntry(date = date3, odometer = 8800, liters = 34.0, cost = 3400.0))

        // Delete middle entry
        val middle = repository.getEntryById(id2)!!
        repository.deleteEntry(middle)

        val entries = repository.getAllEntries()
        assertEquals(2, entries.size)

        val dash = MileageCalculator.calculateDashboardStats(entries)
        assertEquals(8800, dash.latestOdometer)
        assertEquals(800, dash.totalDistance)   // 8800 - 8000
        assertEquals(64.0, dash.totalFuel, 0.001)  // 30 + 34
        assertEquals(6400.0, dash.totalCost, 0.001) // 3000 + 3400
        // averageMileage = 800 / 34
        assertEquals(800.0 / 34.0, dash.averageMileage!!, 0.001)
        // costPerKm = 6400 / 800
        assertEquals(8.0, dash.costPerKm!!, 0.001)

        // Charts: 2 fillups (first null, second 800/34), 2 monthly bars
        val fillups = MileageCalculator.calculatePerFillupMileage(entries)
        assertEquals(2, fillups.size)
        assertNull(fillups[0].mileageKmPerL)
        assertEquals(800.0 / 34.0, fillups[1].mileageKmPerL!!, 0.001)

        val monthly = MileageCalculator.calculateMonthlySpend(entries)
        assertEquals(2, monthly.size)
        assertEquals(3000.0, monthly[0].totalCost, 0.001) // Apr
        assertEquals(3400.0, monthly[1].totalCost, 0.001) // May
    }
}
