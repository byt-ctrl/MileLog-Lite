package com.example.myapplication.ui.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.MileLogApplication
import com.example.myapplication.data.repository.FuelEntryRepository
import com.example.myapplication.domain.calculation.FillupMileage
import com.example.myapplication.domain.calculation.MileageCalculator
import com.example.myapplication.domain.calculation.MonthlyFuelSpend
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ChartsUiState(
    val fillups: List<FillupMileage> = emptyList(),
    val monthlySpends: List<MonthlyFuelSpend> = emptyList(),
    val entryCount: Int = 0,
    val isLoading: Boolean = true
)

/**
 * ViewModel feeding the Charts/Insights screen.
 *
 * Exposes the per-fill-up mileage series and the monthly fuel spend series,
 * recomputed automatically whenever entries change in the database.
 */
class ChartsViewModel(
    private val repository: FuelEntryRepository
) : ViewModel() {

    val uiState: StateFlow<ChartsUiState> = repository.getAllEntriesFlow()
        .map { entries ->
            ChartsUiState(
                fillups = MileageCalculator.calculatePerFillupMileage(entries),
                monthlySpends = MileageCalculator.calculateMonthlySpend(entries),
                entryCount = entries.size,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChartsUiState(isLoading = true)
        )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MileLogApplication)
                ChartsViewModel(application.repository)
            }
        }
    }
}
