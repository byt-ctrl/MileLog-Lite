package com.example.myapplication.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.MileLogApplication
import com.example.myapplication.data.repository.FuelEntryRepository
import com.example.myapplication.domain.calculation.MileageCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val latestOdometer: Int? = null,
    val totalDistance: Int = 0,
    val totalFuel: Double = 0.0,
    val totalCost: Double = 0.0,
    val averageMileage: Double? = null,
    val costPerKm: Double? = null,
    val entryCount: Int = 0,
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val repository: FuelEntryRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.getAllEntriesFlow()
        .map { entries ->
            val stats = MileageCalculator.calculateDashboardStats(entries)
            DashboardUiState(
                latestOdometer = stats.latestOdometer,
                totalDistance = stats.totalDistance,
                totalFuel = stats.totalFuel,
                totalCost = stats.totalCost,
                averageMileage = stats.averageMileage,
                costPerKm = stats.costPerKm,
                entryCount = entries.size,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true)
        )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MileLogApplication)
                DashboardViewModel(application.repository)
            }
        }
    }
}
