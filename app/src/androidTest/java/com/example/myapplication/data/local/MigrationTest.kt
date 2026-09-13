package com.example.myapplication.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migration tests for [MileLiteDatabase].
 *
 * Versions 1 and 2 predate schema export, so their schemas are built here by
 * hand with raw SQL and a version stamp, then Room is opened at the current
 * version and expected to migrate without loss. This is the only way to cover
 * those historical upgrades, since no schema JSON exists for them.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val dbName = "migration-test.db"
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(dbName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun migrateFromVersion1_addsFuelCategoryAndVehicleLink() = runBlocking {
        createLegacyDatabase(
            version = 1,
            statements = listOf(
                "INSERT INTO `fuel_entries` (`id`, `date`, `odometer`, `liters`, `cost`) " +
                    "VALUES (1, 1000, 10000, 30.0, 3000.0)"
            )
        )

        val database = openMigratedDatabase()
        try {
            val entries = database.fuelEntryDao().getAll()
            assertEquals(1, entries.size)
            val entry = entries.single()
            assertEquals(1L, entry.id)
            assertEquals(10000, entry.odometer)
            assertEquals(30.0, entry.liters, 0.0001)
            // Added by MIGRATION_1_2 with its default.
            assertEquals("Petrol", entry.fuelCategory)
            // Added and backfilled by MIGRATION_2_3.
            assertEquals(1L, entry.vehicleId)
            assertEquals(1, database.vehicleDao().getAll().size)
        } finally {
            database.close()
        }
    }

    @Test
    fun migrateFromVersion2_preservesCategoryAndAttachesVehicle() = runBlocking {
        createLegacyDatabase(
            version = 2,
            statements = listOf(
                "INSERT INTO `fuel_entries` " +
                    "(`id`, `date`, `odometer`, `liters`, `cost`, `fuelCategory`) " +
                    "VALUES (1, 1000, 10000, 30.0, 3000.0, 'Diesel')"
            )
        )

        val database = openMigratedDatabase()
        try {
            val entry = database.fuelEntryDao().getAll().single()
            assertEquals("Diesel", entry.fuelCategory)
            assertEquals(1L, entry.vehicleId)
        } finally {
            database.close()
        }
    }

    @Test
    fun migrateFromVersion3_detachesEntriesWhoseVehicleNoLongerExists() = runBlocking {
        createLegacyDatabase(
            version = 3,
            statements = listOf(
                "INSERT INTO `fuel_entries` " +
                    "(`id`, `date`, `odometer`, `liters`, `cost`, `fuelCategory`, `vehicleId`) " +
                    "VALUES (1, 1000, 10000, 30.0, 3000.0, 'Petrol', 1)",
                "INSERT INTO `fuel_entries` " +
                    "(`id`, `date`, `odometer`, `liters`, `cost`, `fuelCategory`, `vehicleId`) " +
                    "VALUES (2, 2000, 10500, 25.0, 2500.0, 'Petrol', 42)"
            )
        )

        val database = openMigratedDatabase()
        try {
            val entries = database.fuelEntryDao().getAll().associateBy { it.id }
            assertEquals(2, entries.size)
            // The valid reference survives the table rebuild.
            assertEquals(1L, entries.getValue(1L).vehicleId)
            // The dangling reference is detached rather than dropped or left
            // violating the new foreign key.
            assertNull(entries.getValue(2L).vehicleId)
            assertNotNull(database.vehicleDao().getById(1L))
        } finally {
            database.close()
        }
    }

    @Test
    fun migratedDatabase_enforcesTheVehicleForeignKey() = runBlocking {
        createLegacyDatabase(version = 3)

        val database = openMigratedDatabase()
        try {
            val failure = runCatching {
                database.fuelEntryDao().insert(
                    FuelEntry(
                        vehicleId = 999L,
                        date = 1000L,
                        odometer = 1000,
                        liters = 10.0,
                        cost = 1000.0
                    )
                )
            }
            assertTrue(
                "Expected the vehicleId foreign key to reject an unknown vehicle",
                failure.isFailure
            )
        } finally {
            database.close()
        }
    }

    private fun openMigratedDatabase(): MileLiteDatabase =
        Room.databaseBuilder(context, MileLiteDatabase::class.java, dbName)
            .addMigrations(
                MileLiteDatabase.MIGRATION_1_2,
                MileLiteDatabase.MIGRATION_2_3,
                MileLiteDatabase.MIGRATION_3_4
            )
            .allowMainThreadQueries()
            .build()

    /**
     * Recreates the schema as it stood at [version], runs any [statements], then
     * stamps the version so Room sees an upgrade to perform.
     */
    private fun createLegacyDatabase(version: Int, statements: List<String> = emptyList()) {
        context.deleteDatabase(dbName)
        val file = context.getDatabasePath(dbName)
        file.parentFile?.mkdirs()
        val db = SQLiteDatabase.openOrCreateDatabase(file, null)
        try {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `fuel_entries` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`date` INTEGER NOT NULL, " +
                    "`odometer` INTEGER NOT NULL, " +
                    "`liters` REAL NOT NULL, " +
                    "`cost` REAL NOT NULL)"
            )
            if (version >= 2) {
                db.execSQL(
                    "ALTER TABLE `fuel_entries` ADD COLUMN `fuelCategory` TEXT NOT NULL DEFAULT 'Petrol'"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_fuel_entries_fuelCategory` " +
                        "ON `fuel_entries` (`fuelCategory`)"
                )
            }
            if (version >= 3) {
                db.execSQL(
                    "ALTER TABLE `fuel_entries` ADD COLUMN `vehicleId` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `vehicles` (" +
                        "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                        "`name` TEXT NOT NULL, " +
                        "`make` TEXT NOT NULL, " +
                        "`model` TEXT NOT NULL, " +
                        "`registrationNumber` TEXT NOT NULL, " +
                        "`fuelType` TEXT NOT NULL, " +
                        "`isActive` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_vehicles_name` ON `vehicles` (`name`)"
                )
                db.execSQL(
                    "INSERT INTO `vehicles` " +
                        "(`id`, `name`, `make`, `model`, `registrationNumber`, `fuelType`, `isActive`) " +
                        "VALUES (1, 'My vehicle', '', '', '', 'Petrol', 1)"
                )
            }
            statements.forEach { db.execSQL(it) }
            db.version = version
        } finally {
            db.close()
        }
    }
}
