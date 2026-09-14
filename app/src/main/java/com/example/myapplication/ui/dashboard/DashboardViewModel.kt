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
import com.example.myapplication.data.local.FuelEntry
import com.example.myapplication.data.local.Vehicle
import com.example.myapplication.data.repository.FuelEntryRepository
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.data.repository.VehicleRepository
import com.example.myapplication.domain.calculation.FillupMileage
import com.example.myapplication.domain.calculation.MileageCalculator
import com.example.myapplication.domain.conversion.DistanceUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    val vehicle: Vehicle? = null,
    val latestOdometer: Int? = null,
    val latestFuelCategory: FuelCategory? = null,
    val totalDistance: Int = 0,
    val totalFuel: Double = 0.0,
    val totalCost: Double = 0.0,
    val averageMileage: Double? = null,
    val costPerKm: Double? = null,
    val entryCount: Int = 0,
    /**
     * The most recent fill-ups with their computed mileage, newest first, for
     * the ledger preview. Capped at [RECENT_LIMIT].
     */
    val recentFillups: List<FillupMileage> = emptyList(),
    /**
     * Measured fill-ups in odometer order, excluding the baseline entry that
     * has no previous reading to measure against.
     */
    val trendFillups: List<FillupMileage> = emptyList(),
    /** Mileage returned by the most recent fill-up, for the gauge comparison. */
    val latestMileage: Double? = null,
    /** Unit every distance and mileage readout is converted to for display. */
    val distanceUnit: DistanceUnit = DistanceUnit.DEFAULT,
    val isLoading: Boolean = true,
    val errorMessage: DashboardMessage? = null
) {
    companion object {
        const val RECENT_LIMIT = 5
    }
}

/** Everything a dashboard reading is built from, resolved once per emission. */
private data class DashboardInput(
    val vehicle: Vehicle?,
    val distanceUnit: DistanceUnit,
    val entries: List<FuelEntry>
)

class DashboardViewModel(
    private val repository: FuelEntryRepository,
    private val vehicleRepository: VehicleRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val inputs: Flow<DashboardInput> = _retryTrigger
        .flatMapLatest { vehicleRepository.getActiveVehicleFlow() }
        .flatMapLatest { vehicle: Vehicle? ->
            val entriesFlow: Flow<List<FuelEntry>> = if (vehicle == null) {
                flowOf(emptyList())
            } else {
                repository.getAllEntriesFlowForVehicle(vehicle.id)
            }
            combine(entriesFlow, settingsRepository.distanceUnit) { entries, distanceUnit ->
                DashboardInput(vehicle, distanceUnit, entries)
            }
        }

    val uiState: StateFlow<DashboardUiState> = inputs
        .map { input -> buildState(input) }
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

    private fun buildState(input: DashboardInput): DashboardUiState {
        val entries = input.entries
        val stats = MileageCalculator.calculateDashboardStats(entries)
        val fillups = MileageCalculator.calculatePerFillupMileage(entries)
        val measured = fillups.filter { it.mileageKmPerL != null }
        return DashboardUiState(
            vehicle = input.vehicle,
            latestOdometer = stats.latestOdometer,
            latestFuelCategory = entries.firstOrNull()
                ?.let { FuelCategory.fromDisplayName(it.fuelCategory) },
            totalDistance = stats.totalDistance,
            totalFuel = stats.totalFuel,
            totalCost = stats.totalCost,
            averageMileage = stats.averageMileage,
            costPerKm = stats.costPerKm,
            entryCount = entries.size,
            recentFillups = fillups
                .takeLast(DashboardUiState.RECENT_LIMIT)
                .reversed(),
            trendFillups = measured.takeLast(TREND_LIMIT),
            latestMileage = measured.lastOrNull()?.mileageKmPerL,
            distanceUnit = input.distanceUnit,
            isLoading = false
        )
    }

    fun retry() {
        _retryTrigger.update { it + 1 }
    }

    companion object {
        /**
         * Measured fill-ups drawn on the trend chart. Kept small on purpose: on
         * a phone each column is roughly a fifth of the sheet, and more than
         * that squeezes the date captions into nothing.
         */
        private const val TREND_LIMIT = 5

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MileLogApplication)
                DashboardViewModel(
                    application.repository,
                    application.vehicleRepository,
                    application.settingsRepository
                )
            }
        }
    }
}