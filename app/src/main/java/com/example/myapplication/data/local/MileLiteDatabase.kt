package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Main Room database for MileLog Lite.
 */
@Database(
    entities = [FuelEntry::class, Vehicle::class],
    version = 3,
    exportSchema = false
)
abstract class MileLiteDatabase : RoomDatabase() {

    abstract fun fuelEntryDao(): FuelEntryDao

    abstract fun vehicleDao(): VehicleDao

    companion object {
        @Volatile
        private var INSTANCE: MileLiteDatabase? = null

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

        fun getDatabase(context: Context): MileLiteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MileLiteDatabase::class.java,
                    "milelog_lite.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
