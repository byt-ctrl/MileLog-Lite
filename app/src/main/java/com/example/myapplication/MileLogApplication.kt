package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.local.MileLiteDatabase
import com.example.myapplication.data.repository.FuelEntryRepository
import com.example.myapplication.data.repository.OfflineFuelEntryRepository
import com.example.myapplication.data.repository.OfflineVehicleRepository
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
}
