package com.example.myapplication.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.MileLogApplication
import com.example.myapplication.data.local.FuelEntry
import com.example.myapplication.data.repository.FuelEntryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel exposing the list of fuel entries for the History screen.
 */
class HistoryViewModel(repository: FuelEntryRepository) : ViewModel() {

    /**
     * Live list of all fuel entries, most recent first. Auto-updates on any insert/update/delete.
     */
    val entries: StateFlow<List<FuelEntry>> = repository.getAllEntriesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

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
