package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.local.MileLiteDatabase
import com.example.myapplication.data.repository.FuelEntryRepository
import com.example.myapplication.data.repository.OfflineFuelEntryRepository

/**
 * Application class providing dependency access for MileLog Lite.
 */
class MileLogApplication : Application() {

    val repository: FuelEntryRepository by lazy {
        val database = MileLiteDatabase.getDatabase(this)
        OfflineFuelEntryRepository(database.fuelEntryDao())
    }
}
