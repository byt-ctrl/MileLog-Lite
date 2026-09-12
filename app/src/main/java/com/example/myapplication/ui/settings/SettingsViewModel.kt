package com.example.myapplication.ui.settings

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
import com.example.myapplication.data.local.FuelEntry
import com.example.myapplication.data.repository.FuelEntryRepository
import com.example.myapplication.domain.calculation.MileageCalculator
import com.example.myapplication.domain.demo.DemoDataGenerator
import com.example.myapplication.domain.export.FuelEntryCsvExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Stable identifiers for settings outcomes. The UI resolves each key to a
 * localized `stringResource`.
 */
enum class SettingsMessage(@StringRes val messageRes: Int) {
    EXPORT_SUCCESS(R.string.export_success),
    EXPORT_FAILED(R.string.export_failure),
    CLEARED(R.string.settings_cleared),
    RESTORED(R.string.settings_restored),
    SEEDED(R.string.settings_seeded),
    SEED_FAILED(R.string.settings_seed_failed)
}

data class SettingsUiState(
    val entryCount: Int = 0,
    val totalDistance: Int = 0,
    val exportReady: String? = null,
    val message: SettingsMessage? = null,
    val messageCount: Int = 0,
    val messageDetail: String? = null,
    val isBusy: Boolean = false
)

/** Transient state that must survive alongside the observed entry flow. */
private data class SettingsTransient(
    val exportReady: String? = null,
    val message: SettingsMessage? = null,
    val messageCount: Int = 0,
    val messageDetail: String? = null,
    val isBusy: Boolean = false
)

/**
 * Settings state.
 *
 * Everything exposed here is a real capability of the app: the entry count and
 * distance come from the database, export writes a CSV through the same
 * exporter the History screen uses, and clearing the log keeps the removed rows
 * so the action is recoverable.
 */
class SettingsViewModel(
    application: Application,
    private val repository: FuelEntryRepository
) : AndroidViewModel(application) {

    private val _transient = MutableStateFlow(SettingsTransient())

    /** Rows removed by the most recent clear, held for undo. */
    private var clearedEntries: List<FuelEntry> = emptyList()

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.getAllEntriesFlow(),
        _transient
    ) { entries, transient ->
        SettingsUiState(
            entryCount = entries.size,
            totalDistance = MileageCalculator.calculateDashboardStats(entries).totalDistance,
            exportReady = transient.exportReady,
            message = transient.message,
            messageCount = transient.messageCount,
            messageDetail = transient.messageDetail,
            isBusy = transient.isBusy
        )
    }
        .catch { _ ->
            emit(SettingsUiState(message = SettingsMessage.EXPORT_FAILED))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    fun exportEntries() {
        viewModelScope.launch {
            _transient.update { it.copy(isBusy = true) }
            runCatching { repository.getAllEntries() }
                .onSuccess { entries ->
                    _transient.update {
                        it.copy(
                            isBusy = false,
                            exportReady = FuelEntryCsvExporter.buildCsv(entries)
                        )
                    }
                }
                .onFailure {
                    _transient.update {
                        it.copy(isBusy = false, message = SettingsMessage.EXPORT_FAILED)
                    }
                }
        }
    }

    fun writeExportedCsv(uri: Uri) {
        viewModelScope.launch {
            val csv = _transient.value.exportReady ?: return@launch
            val count = csv.lineSequence().count { it.isNotBlank() } - 1
            runCatching {
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(csv.toByteArray(Charsets.UTF_8))
                } ?: error("OutputStream was null")
            }
                .onSuccess {
                    _transient.update {
                        it.copy(
                            exportReady = null,
                            message = SettingsMessage.EXPORT_SUCCESS,
                            messageCount = count.coerceAtLeast(0)
                        )
                    }
                }
                .onFailure { failure ->
                    _transient.update {
                        it.copy(
                            exportReady = null,
                            message = SettingsMessage.EXPORT_FAILED,
                            messageDetail = failure.localizedMessage
                        )
                    }
                }
        }
    }

    fun clearExportReady() {
        _transient.update { it.copy(exportReady = null) }
    }

    fun consumeMessage() {
        _transient.update {
            it.copy(message = null, messageCount = 0, messageDetail = null)
        }
    }

    /**
     * Removes every entry, holding the rows so [undoClear] can put them back.
     * The repository has no bulk delete, so this walks the rows; the action is
     * rare and the list is small by design.
     */
    fun clearAllEntries() {
        viewModelScope.launch {
            _transient.update { it.copy(isBusy = true) }
            runCatching { repository.getAllEntries() }
                .onSuccess { entries ->
                    clearedEntries = entries
                    entries.forEach { repository.deleteEntry(it) }
                    _transient.update {
                        it.copy(isBusy = false, message = SettingsMessage.CLEARED)
                    }
                }
                .onFailure {
                    _transient.update {
                        it.copy(isBusy = false, message = SettingsMessage.EXPORT_FAILED)
                    }
                }
        }
    }

    fun undoClear() {
        viewModelScope.launch {
            val rows = clearedEntries
            if (rows.isEmpty()) return@launch
            rows.forEach { repository.insertEntry(it) }
            clearedEntries = emptyList()
            _transient.update { it.copy(message = SettingsMessage.RESTORED) }
        }
    }

    /**
     * Appends a run of sample fill-ups so a fresh install has something to
     * visualise. Rows continue after the newest real reading, so this never
     * collides with data the user entered. "Delete all fill-ups" is the way
     * back out.
     */
    fun seedDemoData() {
        viewModelScope.launch {
            _transient.update { it.copy(isBusy = true) }
            runCatching {
                val rows = DemoDataGenerator.generate(repository.getAllEntries())
                repository.insertEntries(rows)
                rows.size
            }
                .onSuccess { added ->
                    _transient.update {
                        it.copy(
                            isBusy = false,
                            message = SettingsMessage.SEEDED,
                            messageCount = added
                        )
                    }
                }
                .onFailure {
                    _transient.update {
                        it.copy(isBusy = false, message = SettingsMessage.SEED_FAILED)
                    }
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MileLogApplication)
                SettingsViewModel(application, application.repository)
            }
        }
    }
}
