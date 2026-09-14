package com.example.myapplication.ui.charts

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.MileLogApplication
import com.example.myapplication.R
import com.example.myapplication.data.local.Vehicle
import com.example.myapplication.data.repository.FuelEntryRepository
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.data.repository.VehicleRepository
import com.example.myapplication.domain.calculation.CategoryMileageSeries
import com.example.myapplication.domain.calculation.CategoryMonthlySpendSeries
import com.example.myapplication.domain.calculation.FillupMileage
import com.example.myapplication.domain.calculation.MileageCalculator
import com.example.myapplication.domain.calculation.MonthlyFuelSpend
import com.example.myapplication.domain.conversion.DistanceUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 * Stable identifiers for charts-screen messages. The UI resolves each key
 * to a localized `stringResource`.
 */
enum class ChartsMessage(@StringRes val messageRes: Int) {
    LOAD_FAILED(R.string.charts_error_load)
}

data class ChartsUiState(
    val vehicle: Vehicle? = null,
    val fillups: List<FillupMileage> = emptyList(),
    val monthlySpends: List<MonthlyFuelSpend> = emptyList(),
    val categoryMileageSeries: List<CategoryMileageSeries> = emptyList(),
    val categoryMonthlySpends: List<CategoryMonthlySpendSeries> = emptyList(),
    /**
     * Unit the trend chart converts its points and axis to. The series
     * themselves stay in km/L; this only changes what is drawn.
     */
    val distanceUnit: DistanceUnit = DistanceUnit.DEFAULT,
    val entryCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: ChartsMessage? = null
)

class ChartsViewModel(
    private val repository: FuelEntryRepository,
    private val vehicleRepository: VehicleRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ChartsUiState> = combine(
        _retryTrigger.flatMapLatest { vehicleRepository.getActiveVehicleFlow() },
        settingsRepository.distanceUnit
    ) { vehicle, distanceUnit -> vehicle to distanceUnit }
        .flatMapLatest { (vehicle, distanceUnit) ->
            val entriesFlow = if (vehicle == null) {
                flowOf(emptyList())
            } else {
                repository.getAllEntriesFlowForVehicle(vehicle.id)
            }
            entriesFlow.map { entries -> Triple(vehicle, distanceUnit, entries) }
        }
        .map { (vehicle, distanceUnit, entries) ->
            ChartsUiState(
                vehicle = vehicle,
                fillups = MileageCalculator.calculatePerFillupMileage(entries),
                monthlySpends = MileageCalculator.calculateMonthlySpend(entries),
                categoryMileageSeries = MileageCalculator.calculatePerCategoryMileageSeries(entries),
                categoryMonthlySpends = MileageCalculator.calculatePerCategoryMonthlySpend(
                    entries,
                    MileageCalculator.calculateMonthlySpend(entries)
                ),
                distanceUnit = distanceUnit,
                entryCount = entries.size,
                isLoading = false
            )
        }
        .catch { _ ->
            emit(
                ChartsUiState(
                    isLoading = false,
                    errorMessage = ChartsMessage.LOAD_FAILED
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChartsUiState(isLoading = true)
        )

    fun retry() {
        _retryTrigger.update { it + 1 }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MileLogApplication)
                ChartsViewModel(
                    application.repository,
                    application.vehicleRepository,
                    application.settingsRepository
                )
            }
        }
    }
}