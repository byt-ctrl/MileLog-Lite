package com.example.myapplication.ui.history

import android.app.Application
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.MileLogApplication
import com.example.myapplication.R
import com.example.myapplication.data.local.FuelCategory
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

/**
 * Stable error / result identifiers for the History screen. The UI maps each
 * key to a localized `stringResource`; counts (entries exported) are passed
 * as positional arguments to the resolved template.
 */
enum class HistoryMessage(@StringRes val messageRes: Int) {
    LOAD_FAILED(R.string.history_error_load),
    EXPORT_BUILD_FAILED(R.string.history_export_build_failed),
    EXPORT_SUCCESS(R.string.export_success),
    EXPORT_WRITE_FAILED(R.string.export_failure)
}

/**
 * UI state for the History screen.
 *
 * [exportMessage] carries either null (no message pending) or a
 * [HistoryMessage] plus an optional count for the success template
 * (`Exported %d entries to CSV`).
 */
data class HistoryUiState(
    val entries: List<FuelEntry> = emptyList(),
    val selectedCategory: FuelCategory? = null,
    val isLoading: Boolean = true,
    val errorMessage: HistoryMessage? = null,
    val exportReady: String? = null,
    val exportMessage: HistoryMessage? = null,
    val exportMessageCount: Int = 0,
    val exportMessageDetail: String? = null,
    val totalEntryCount: Int = 0,
    /**
     * The entry most recently deleted, exposed so the UI can offer an
     * undo affordance via a transient snackbar. Null once consumed.
     */
    val lastDeleted: FuelEntry? = null
)

/**
 * Transient state for the CSV export flow.
 */
private data class ExportState(
    val exportReady: String? = null,
    val exportMessage: HistoryMessage? = null,
    val exportMessageCount: Int = 0,
    val exportMessageDetail: String? = null
)

/**
 * ViewModel exposing the list of fuel entries for the History screen.
 */
class HistoryViewModel(
    application: Application,
    private val repository: FuelEntryRepository
) : AndroidViewModel(application) {

    private val _retryTrigger = MutableStateFlow(0)
    private val _selectedCategory = MutableStateFlow<FuelCategory?>(null)

    private val _exportState = MutableStateFlow(ExportState())

    /**
     * Snapshot of the last deleted entry. The UI shows a transient snackbar
     * with an Undo action; if the user acts, the entry is restored.
     */
    private val _undoState = MutableStateFlow(UndoState())

    private data class UndoState(val lastDeleted: FuelEntry? = null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HistoryUiState> = combine(
        _retryTrigger,
        _selectedCategory,
        _exportState
    ) { _, category, export ->
        Triple(category, export, Unit)
    }
        .flatMapLatest { (category, export, _) ->
            combine(
                repository.getAllEntriesFlow(category),
                repository.getAllEntriesFlow()
            ) { filtered, all ->
                HistoryUiState(
                    entries = filtered,
                    selectedCategory = category,
                    isLoading = false,
                    exportReady = export.exportReady,
                    exportMessage = export.exportMessage,
                    exportMessageCount = export.exportMessageCount,
                    exportMessageDetail = export.exportMessageDetail,
                    totalEntryCount = all.size,
                    lastDeleted = _undoState.value.lastDeleted
                )
            }
        }
        .catch { _ ->
            emit(
                HistoryUiState(
                    isLoading = false,
                    errorMessage = HistoryMessage.LOAD_FAILED
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(isLoading = true)
        )

    /**
     * Deletes the entry with the given id from the database and exposes it
     * via [HistoryUiState.lastDeleted] so the UI can offer an Undo action.
     */
    fun deleteEntry(entry: FuelEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            _undoState.update { it.copy(lastDeleted = entry) }
        }
    }

    /**
     * Restores the most recently deleted entry, if any. Called when the
     * user taps the Undo action on the delete snackbar.
     */
    fun undoDelete() {
        val entry = _undoState.value.lastDeleted ?: return
        viewModelScope.launch {
            repository.insertEntry(entry)
            _undoState.update { it.copy(lastDeleted = null) }
        }
    }

    /**
     * Clears [HistoryUiState.lastDeleted] after the snackbar has been shown
     * and dismissed (or expired). Prevents the snackbar from re-firing on
     * recomposition.
     */
    fun consumeDeleted() {
        _undoState.update { it.copy(lastDeleted = null) }
    }

    fun retry() {
        _retryTrigger.update { it + 1 }
    }

    /**
     * Sets the active fuel category filter. Pass `null` to show all entries.
     */
    fun setCategoryFilter(category: FuelCategory?) {
        _selectedCategory.value = category
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
                        it.copy(exportMessage = HistoryMessage.EXPORT_BUILD_FAILED)
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
            val count = csv.lineSequence().count { it.isNotBlank() } - 1 // minus header row
            runCatching {
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(csv.toByteArray(Charsets.UTF_8))
                } ?: error("OutputStream was null")
            }
                .onSuccess {
                    _exportState.update {
                        it.copy(
                            exportReady = null,
                            exportMessage = HistoryMessage.EXPORT_SUCCESS,
                            exportMessageCount = count.coerceAtLeast(0)
                        )
                    }
                }
                .onFailure { failure ->
                    _exportState.update {
                        it.copy(
                            exportReady = null,
                            exportMessage = HistoryMessage.EXPORT_WRITE_FAILED,
                            exportMessageDetail = failure.localizedMessage
                        )
                    }
                }
        }
    }

    fun clearExportReady() {
        _exportState.update { it.copy(exportReady = null) }
    }

    fun consumeExportMessage() {
        _exportState.update {
            it.copy(
                exportMessage = null,
                exportMessageCount = 0,
                exportMessageDetail = null
            )
        }
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