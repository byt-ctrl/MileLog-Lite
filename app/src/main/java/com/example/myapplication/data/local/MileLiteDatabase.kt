package com.example.myapplication.data.local

import android.content.Context
import android.database.Cursor
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Main Room database for MileLog Lite.
 */
@Database(
    entities = [FuelEntry::class, Vehicle::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(FuelCategoryConverters::class)
abstract class MileLiteDatabase : RoomDatabase() {

    abstract fun fuelEntryDao(): FuelEntryDao

    abstract fun vehicleDao(): VehicleDao

    companion object {
        @Volatile
        private var INSTANCE: MileLiteDatabase? = null

        /**
         * Brings a version 1 database up to version 2 by adding the fuel
         * category that Sprint 5 introduced.
         *
         * Version 1 shipped without a migration, so its index set cannot be
         * assumed; every statement is guarded so the migration is a no-op on a
         * database that already has the column or the indices.
         */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (!db.tableHasColumn("fuel_entries", "fuelCategory")) {
                    db.execSQL(
                        "ALTER TABLE `fuel_entries` " +
                            "ADD COLUMN `fuelCategory` TEXT NOT NULL DEFAULT 'Petrol'"
                    )
                }
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_fuel_entries_date` " +
                        "ON `fuel_entries` (`date`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_fuel_entries_odometer` " +
                        "ON `fuel_entries` (`odometer`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_fuel_entries_date_odometer` " +
                        "ON `fuel_entries` (`date`, `odometer`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_fuel_entries_fuelCategory` " +
                        "ON `fuel_entries` (`fuelCategory`)"
                )
            }
        }

        /**
         * Adds the vehicles table and links every existing fill-up to a default
         * vehicle, so upgrading installs keep all of their logged data.
         */
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
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
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_vehicles_name` " +
                        "ON `vehicles` (`name`)"
                )
                db.execSQL(
                    "ALTER TABLE `fuel_entries` ADD COLUMN `vehicleId` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_fuel_entries_vehicleId` " +
                        "ON `fuel_entries` (`vehicleId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_fuel_entries_vehicleId_fuelCategory` " +
                        "ON `fuel_entries` (`vehicleId`, `fuelCategory`)"
                )
                db.execSQL(
                    "INSERT INTO `vehicles` " +
                        "(`id`, `name`, `make`, `model`, `registrationNumber`, `fuelType`, `isActive`) " +
                        "VALUES (1, 'My vehicle', '', '', '', 'Petrol', 1)"
                )
                db.execSQL("UPDATE `fuel_entries` SET `vehicleId` = 1")
            }
        }

        /**
         * Replaces the `vehicleId` column with a nullable one backed by a
         * foreign key onto `vehicles` (`ON DELETE CASCADE`). SQLite cannot add a
         * constraint in place, so the table is rebuilt and its indices restored.
         * A fill-up pointing at a vehicle that no longer exists is detached
         * during the copy: the old column is `NOT NULL`, so it cannot be nulled
         * in place before the rebuild.
         */
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `fuel_entries_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`vehicleId` INTEGER, " +
                        "`date` INTEGER NOT NULL, " +
                        "`odometer` INTEGER NOT NULL, " +
                        "`liters` REAL NOT NULL, " +
                        "`cost` REAL NOT NULL, " +
                        "`fuelCategory` TEXT NOT NULL, " +
                        "FOREIGN KEY(`vehicleId`) REFERENCES `vehicles`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "INSERT INTO `fuel_entries_new` " +
                        "(`id`, `vehicleId`, `date`, `odometer`, `liters`, `cost`, `fuelCategory`) " +
                        "SELECT `id`, " +
                        "CASE WHEN `vehicleId` IN (SELECT `id` FROM `vehicles`) " +
                        "THEN `vehicleId` ELSE NULL END, " +
                        "`date`, `odometer`, `liters`, `cost`, `fuelCategory` " +
                        "FROM `fuel_entries`"
                )
                db.execSQL("DROP TABLE `fuel_entries`")
                db.execSQL("ALTER TABLE `fuel_entries_new` RENAME TO `fuel_entries`")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_fuel_entries_date` " +
                        "ON `fuel_entries` (`date`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_fuel_entries_odometer` " +
                        "ON `fuel_entries` (`odometer`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_fuel_entries_date_odometer` " +
                        "ON `fuel_entries` (`date`, `odometer`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_fuel_entries_fuelCategory` " +
                        "ON `fuel_entries` (`fuelCategory`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_fuel_entries_vehicleId` " +
                        "ON `fuel_entries` (`vehicleId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_fuel_entries_vehicleId_fuelCategory` " +
                        "ON `fuel_entries` (`vehicleId`, `fuelCategory`)"
                )
            }
        }

        /**
         * Re-establishes the "at most one active vehicle" invariant after an
         * upgrade or an interrupted write: keeps the lowest-id active vehicle,
         * and activates the first vehicle when none is selected.
         */
        private val REPAIR_ACTIVE_VEHICLE = object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL(
                    "UPDATE `vehicles` SET `isActive` = 0 WHERE `isActive` = 1 " +
                        "AND `id` <> (SELECT MIN(`id`) FROM `vehicles` WHERE `isActive` = 1)"
                )
                db.execSQL(
                    "UPDATE `vehicles` SET `isActive` = 1 " +
                        "WHERE `id` = (SELECT MIN(`id`) FROM `vehicles`) " +
                        "AND NOT EXISTS (SELECT 1 FROM `vehicles` WHERE `isActive` = 1)"
                )
            }
        }

        private fun SupportSQLiteDatabase.tableHasColumn(table: String, column: String): Boolean {
            query("PRAGMA table_info(`$table`)").use { cursor: Cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                if (nameIndex < 0) return false
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameIndex) == column) return true
                }
            }
            return false
        }

        fun getDatabase(context: Context): MileLiteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MileLiteDatabase::class.java,
                    "milelog_lite.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .addCallback(REPAIR_ACTIVE_VEHICLE)
                    // Only a downgrade may wipe: a missing upgrade path now fails
                    // loudly instead of silently destroying data.
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
