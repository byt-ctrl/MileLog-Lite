package com.example.myapplication.ui.history

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.MileLogApplication
import com.example.myapplication.data.local.FuelEntry
import com.example.myapplication.data.repository.FuelEntryRepository
import com.example.myapplication.domain.export.FuelEntryCsvExporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val entries: List<FuelEntry> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val exportReady: String? = null,
    val exportMessage: String? = null
)

/**
 * Transient state for the CSV export flow.
 */
private data class ExportState(
    val exportReady: String? = null,
    val exportMessage: String? = null
)

/**
 * ViewModel exposing the list of fuel entries for the History screen.
 */
class HistoryViewModel(
    application: Application,
    private val repository: FuelEntryRepository
) : AndroidViewModel(application) {

    private val _retryTrigger = MutableStateFlow(0)

    /**
     * Transient export state (CSV payload + result message) kept separate from
     * the entries flow so export events are not clobbered by list updates.
     */
    private val _exportState = MutableStateFlow(ExportState())

    /**
     * Live list of all fuel entries, most recent first. Auto-updates on any insert/update/delete.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HistoryUiState> = combine(
        _retryTrigger.flatMapLatest { repository.getAllEntriesFlow() },
        _exportState
    ) { entries, export ->
        HistoryUiState(
            entries = entries,
            isLoading = false,
            exportReady = export.exportReady,
            exportMessage = export.exportMessage
        )
    }
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

    /**
     * Builds the CSV text for all entries and exposes it as [HistoryUiState.exportReady],
     * which the UI consumes by launching the system document-creation picker.
     */
    fun exportEntries() {
        viewModelScope.launch {
            runCatching { repository.getAllEntries() }
                .onSuccess { entries ->
                    _exportState.update {
                        it.copy(exportReady = FuelEntryCsvExporter.buildCsv(entries))
                    }
                }
                .onFailure {
                    _exportState.update {
                        it.copy(exportMessage = "Couldn't prepare the export. Please try again.")
                    }
                }
        }
    }

    /**
     * Writes the previously prepared CSV to the user-chosen document URI
     * and surfaces the outcome via [HistoryUiState.exportMessage].
     */
    fun writeExportedCsv(uri: Uri) {
        viewModelScope.launch {
            val csv = _exportState.value.exportReady ?: return@launch
            runCatching {
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(csv.toByteArray(Charsets.UTF_8))
                }
            }
                .onSuccess {
                    _exportState.update {
                        it.copy(
                            exportReady = null,
                            exportMessage = "CSV exported successfully."
                        )
                    }
                }
                .onFailure {
                    _exportState.update {
                        it.copy(
                            exportReady = null,
                            exportMessage = "Export failed. Please try again."
                        )
                    }
                }
        }
    }

    /**
     * Clears the export-ready payload after the UI has launched the picker.
     */
    fun clearExportReady() {
        _exportState.update { it.copy(exportReady = null) }
    }

    /**
     * Consumes the transient export result message after it has been shown.
     */
    fun consumeExportMessage() {
        _exportState.update { it.copy(exportMessage = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MileLogApplication)
                HistoryViewModel(application, application.repository)
            }
        }
    }
}
