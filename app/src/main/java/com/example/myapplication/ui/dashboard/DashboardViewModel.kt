package com.example.myapplication.ui.dashboard

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.MileLogApplication
import com.example.myapplication.R
import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.data.repository.FuelEntryRepository
import com.example.myapplication.domain.calculation.MileageCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Stable identifiers for dashboard messages. The UI resolves each key to
 * a localized `stringResource`.
 */
enum class DashboardMessage(@StringRes val messageRes: Int) {
    LOAD_FAILED(R.string.dashboard_error_load)
}

data class DashboardUiState(
    val latestOdometer: Int? = null,
    val latestFuelCategory: FuelCategory? = null,
    val totalDistance: Int = 0,
    val totalFuel: Double = 0.0,
    val totalCost: Double = 0.0,
    val averageMileage: Double? = null,
    val costPerKm: Double? = null,
    val entryCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: DashboardMessage? = null
)

class DashboardViewModel(
    private val repository: FuelEntryRepository
) : ViewModel() {

    private val _retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = _retryTrigger
        .flatMapLatest { repository.getAllEntriesFlow() }
        .map { entries ->
            val stats = MileageCalculator.calculateDashboardStats(entries)
            DashboardUiState(
                latestOdometer = stats.latestOdometer,
                latestFuelCategory = entries.firstOrNull()
                    ?.let { FuelCategory.fromDisplayName(it.fuelCategory) },
                totalDistance = stats.totalDistance,
                totalFuel = stats.totalFuel,
                totalCost = stats.totalCost,
                averageMileage = stats.averageMileage,
                costPerKm = stats.costPerKm,
                entryCount = entries.size,
                isLoading = false
            )
        }
        .catch { _ ->
            emit(
                DashboardUiState(
                    isLoading = false,
                    errorMessage = DashboardMessage.LOAD_FAILED
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true)
        )

    fun retry() {
        _retryTrigger.update { it + 1 }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MileLogApplication)
                DashboardViewModel(application.repository)
            }
        }
    }
}