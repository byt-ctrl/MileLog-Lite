package com.example.myapplication.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.MileLogApplication
import com.example.myapplication.data.local.FuelEntry
import com.example.myapplication.data.repository.FuelEntryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val entries: List<FuelEntry> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel exposing the list of fuel entries for the History screen.
 */
class HistoryViewModel(private val repository: FuelEntryRepository) : ViewModel() {

    private val _retryTrigger = MutableStateFlow(0)

    /**
     * Live list of all fuel entries, most recent first. Auto-updates on any insert/update/delete.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HistoryUiState> = _retryTrigger
        .flatMapLatest { repository.getAllEntriesFlow() }
        .map { entries -> HistoryUiState(entries = entries, isLoading = false) }
        .catch { e ->
            emit(
                HistoryUiState(
                    isLoading = false,
                    errorMessage = "Couldn't load fuel history. Please try again."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(isLoading = true)
        )

    /**
     * Deletes the entry with the given id from the database.
     */
    fun deleteEntry(entry: FuelEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    fun retry() {
        _retryTrigger.update { it + 1 }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MileLogApplication)
                HistoryViewModel(application.repository)
            }
        }
    }
}
