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
import com.example.myapplication.data.local.Vehicle
import com.example.myapplication.data.repository.FuelEntryRepository
import com.example.myapplication.data.repository.VehicleRepository
import com.example.myapplication.domain.calculation.MileageCalculator
import com.example.myapplication.domain.demo.DemoDataGenerator
import com.example.myapplication.domain.export.FuelEntryCsvExporter
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
    SEED_FAILED(R.string.settings_seed_failed),
    VEHICLE_DELETED(R.string.vehicle_deleted),
    ACTION_FAILED(R.string.settings_action_failed)
}

data class SettingsUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val activeVehicle: Vehicle? = null,
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
 * Everything exposed here is a real capability of the app: the vehicle list and
 * active selection come from the database, the entry count and distance belong
 * to the active vehicle, export writes a CSV through the same exporter the
 * History screen uses, and clearing the log keeps the removed rows so the action
 * is recoverable.
 */
class SettingsViewModel(
    application: Application,
    private val repository: FuelEntryRepository,
    private val vehicleRepository: VehicleRepository
) : AndroidViewModel(application) {

    private val _transient = MutableStateFlow(SettingsTransient())

    /** Rows removed by the most recent clear, held for undo. */
    private var clearedEntries: List<FuelEntry> = emptyList()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SettingsUiState> = combine(
        vehicleRepository.getAllVehiclesFlow(),
        vehicleRepository.getActiveVehicleFlow(),
        _transient
    ) { vehicles, activeVehicle, transient ->
        Triple(vehicles, activeVehicle, transient)
    }
        .flatMapLatest { (vehicles, activeVehicle, transient) ->
            val entriesFlow = if (activeVehicle == null) {
                flowOf(emptyList())
            } else {
                repository.getAllEntriesFlowForVehicle(activeVehicle.id)
            }
            entriesFlow.map { entries ->
                SettingsUiState(
                    vehicles = vehicles,
                    activeVehicle = activeVehicle,
                    entryCount = entries.size,
                    totalDistance = MileageCalculator.calculateDashboardStats(entries).totalDistance,
                    exportReady = transient.exportReady,
                    message = transient.message,
                    messageCount = transient.messageCount,
                    messageDetail = transient.messageDetail,
                    isBusy = transient.isBusy
                )
            }
        }
        .catch { _ ->
            emit(SettingsUiState(message = SettingsMessage.EXPORT_FAILED))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    /**
     * Selects the vehicle every screen then logs against.
     */
    fun setActiveVehicle(id: Long) {
        viewModelScope.launch {
            runCatching { vehicleRepository.setActiveVehicle(id) }
                .onFailure {
                    _transient.update { it.copy(message = SettingsMessage.ACTION_FAILED) }
                }
        }
    }

    /**
     * Removes a vehicle and every fill-up logged against it. Another vehicle is
     * promoted to active when the removed one was selected.
     */
    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            _transient.update { it.copy(isBusy = true) }
            runCatching { vehicleRepository.deleteVehicleWithEntries(vehicle.id) }
                .onSuccess {
                    _transient.update {
                        it.copy(isBusy = false, message = SettingsMessage.VEHICLE_DELETED)
                    }
                }
                .onFailure {
                    _transient.update {
                        it.copy(isBusy = false, message = SettingsMessage.ACTION_FAILED)
                    }
                }
        }
    }

    /**
     * Builds the CSV text for the active vehicle's entries and exposes it as
     * [SettingsUiState.exportReady] for the system document picker.
     */
    fun exportEntries() {
        viewModelScope.launch {
            _transient.update { it.copy(isBusy = true) }
            runCatching {
                val vehicle = vehicleRepository.getActiveVehicle()
                val entries = vehicle?.let { repository.getAllEntriesForVehicle(it.id) }
                    ?: emptyList()
                FuelEntryCsvExporter.buildCsv(
                    entries = entries,
                    vehicleName = vehicle?.name.orEmpty()
                )
            }
                .onSuccess { csv ->
                    _transient.update {
                        it.copy(isBusy = false, exportReady = csv)
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
                    stream.write(FuelEntryCsvExporter.encode(csv))
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
     * Removes every fill-up for the active vehicle, holding the rows so
     * [undoClear] can put them back. The repository has no bulk delete, so this
     * walks the rows; the action is rare and the list is small by design.
     */
    fun clearAllEntries() {
        viewModelScope.launch {
            _transient.update { it.copy(isBusy = true) }
            runCatching {
                val vehicle = vehicleRepository.getActiveVehicle()
                if (vehicle == null) emptyList() else repository.getAllEntriesForVehicle(vehicle.id)
            }
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
     * Creates the six demo vehicles when they do not exist yet and appends a run
     * of sample fill-ups to each, so a fresh install has several distinct
     * profiles to explore. Rows continue after each vehicle's newest reading, so
     * this never collides with data the user entered. "Delete all fill-ups" is
     * the way back out.
     */
    fun seedDemoData() {
        viewModelScope.launch {
            _transient.update { it.copy(isBusy = true) }
            runCatching {
                val existingByName = vehicleRepository.getAllVehicles().associateBy { it.name }
                var entryCount = 0

                DemoDataGenerator.profiles.forEach { profile ->
                    val vehicle = existingByName[profile.name] ?: run {
                        val created = Vehicle(
                            name = profile.name,
                            make = profile.make,
                            model = profile.model,
                            fuelType = profile.fuelCategory.displayName
                        )
                        created.copy(id = vehicleRepository.insertVehicle(created))
                    }

                    val rows = DemoDataGenerator.generateForVehicle(
                        profile = profile,
                        vehicleId = vehicle.id,
                        existing = repository.getAllEntriesForVehicle(vehicle.id)
                    )
                    entryCount += repository.insertEntries(rows).size
                }

                if (vehicleRepository.getActiveVehicle() == null) {
                    vehicleRepository.getAllVehicles().firstOrNull()
                        ?.let { vehicleRepository.setActiveVehicle(it.id) }
                }

                entryCount
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
                SettingsViewModel(
                    application,
                    application.repository,
                    application.vehicleRepository
                )
            }
        }
    }
}
