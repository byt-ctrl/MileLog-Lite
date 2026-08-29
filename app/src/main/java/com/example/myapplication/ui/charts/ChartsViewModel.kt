package com.example.myapplication.ui.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.MileLogApplication
import com.example.myapplication.data.repository.FuelEntryRepository
import com.example.myapplication.domain.calculation.CategoryMileageSeries
import com.example.myapplication.domain.calculation.CategoryMonthlySpendSeries
import com.example.myapplication.domain.calculation.FillupMileage
import com.example.myapplication.domain.calculation.MileageCalculator
import com.example.myapplication.domain.calculation.MonthlyFuelSpend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class ChartsUiState(
    val fillups: List<FillupMileage> = emptyList(),
    val monthlySpends: List<MonthlyFuelSpend> = emptyList(),
    val categoryMileageSeries: List<CategoryMileageSeries> = emptyList(),
    val categoryMonthlySpends: List<CategoryMonthlySpendSeries> = emptyList(),
    val entryCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
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

    private val _retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ChartsUiState> = _retryTrigger
        .flatMapLatest { repository.getAllEntriesFlow() }
        .map { entries ->
            ChartsUiState(
                fillups = MileageCalculator.calculatePerFillupMileage(entries),
                monthlySpends = MileageCalculator.calculateMonthlySpend(entries),
                categoryMileageSeries = MileageCalculator.calculatePerCategoryMileageSeries(entries),
                categoryMonthlySpends = MileageCalculator.calculatePerCategoryMonthlySpend(
                    entries,
                    MileageCalculator.calculateMonthlySpend(entries)
                ),
                entryCount = entries.size,
                isLoading = false
            )
        }
        .catch { e ->
            emit(
                ChartsUiState(
                    isLoading = false,
                    errorMessage = "Couldn't load chart data. Please try again."
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
                ChartsViewModel(application.repository)
            }
        }
    }
}
