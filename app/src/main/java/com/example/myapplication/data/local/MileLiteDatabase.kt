package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Main Room database for MileLog Lite.
 */
@Database(
    entities = [FuelEntry::class],
    version = 1,
    exportSchema = false
)
abstract class MileLiteDatabase : RoomDatabase() {

    abstract fun fuelEntryDao(): FuelEntryDao

    companion object {
        @Volatile
        private var INSTANCE: MileLiteDatabase? = null

        fun getDatabase(context: Context): MileLiteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MileLiteDatabase::class.java,
                    "milelog_lite.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
