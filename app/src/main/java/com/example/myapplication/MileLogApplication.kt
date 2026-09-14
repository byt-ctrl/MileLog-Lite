package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.local.MileLiteDatabase
import com.example.myapplication.data.local.SharedPreferencesStorage
import com.example.myapplication.data.local.UserPreferences
import com.example.myapplication.data.repository.FuelEntryRepository
import com.example.myapplication.data.repository.OfflineFuelEntryRepository
import com.example.myapplication.data.repository.OfflineSettingsRepository
import com.example.myapplication.data.repository.OfflineVehicleRepository
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.data.repository.VehicleRepository

/**
 * Application class providing dependency access for MileLog Lite.
 */
class MileLogApplication : Application() {

    val database: MileLiteDatabase by lazy {
        MileLiteDatabase.getDatabase(this)
    }

    val repository: FuelEntryRepository by lazy {
        OfflineFuelEntryRepository(database.fuelEntryDao())
    }

    val vehicleRepository: VehicleRepository by lazy {
        OfflineVehicleRepository(database)
    }

    /**
     * One instance for the process, so the appearance chosen in Settings is the
     * same value the theme reads back at the top of the tree.
     */
    val settingsRepository: SettingsRepository by lazy {
        OfflineSettingsRepository(UserPreferences(SharedPreferencesStorage(this)))
    }
}
