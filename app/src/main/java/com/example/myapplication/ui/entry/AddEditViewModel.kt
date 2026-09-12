package com.example.myapplication.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.MileLogApplication
import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.data.local.FuelEntry
import com.example.myapplication.data.repository.FuelEntryRepository
import com.example.myapplication.data.repository.VehicleRepository
import com.example.myapplication.domain.validation.FieldError
import com.example.myapplication.domain.validation.FuelEntryValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State representing the Add/Edit Fuel Entry form.
 *
 * Error fields carry [FieldError] keys; the UI resolves them via
 * `stringResource()` so error copy follows the user's locale.
 */
data class AddEditUiState(
    val entryId: Long = 0L,
    val vehicleId: Long? = null,
    val vehicleName: String? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val odometer: String = "",
    val liters: String = "",
    val cost: String = "",
    val fuelCategory: FuelCategory = FuelCategory.DEFAULT,
    val dateError: FieldError? = null,
    val odometerError: FieldError? = null,
    val odometerMonotonicContext: Int? = null,
    val litersError: FieldError? = null,
    val costError: FieldError? = null,
    val vehicleError: FieldError? = null,
    val previousOdometer: Int? = null,
    val isLoading: Boolean = false,
    val loadError: FieldError? = null,
    val loadErrorContext: String? = null,
    val isEntrySaved: Boolean = false
) {
    val isEditMode: Boolean get() = entryId > 0L
}

/**
 * ViewModel managing state and validation for the Add/Edit Fuel Entry screen.
 */
class AddEditViewModel(
    private val repository: FuelEntryRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    init {
        loadActiveVehicle()
    }

    private var pendingLoadId: Long = 0L

    /**
     * Resolves the active vehicle and the odometer reading a new fill-up must
     * beat. Without a vehicle there is nothing to log against, so the form
     * reports [FieldError.NO_VEHICLE] on save.
     */
    private fun loadActiveVehicle() {
        viewModelScope.launch {
            runCatching { vehicleRepository.getActiveVehicle() }
                .onSuccess { vehicle ->
                    val previous = vehicle?.let {
                        runCatching { repository.getLatestEntryForVehicle(it.id) }
                            .getOrNull()
                            ?.odometer
                    }
                    _uiState.update { state ->
                        // An entry being edited owns its own vehicle; never
                        // overwrite it with whichever vehicle is active.
                        if (state.isEditMode) {
                            state
                        } else {
                            state.copy(
                                vehicleId = vehicle?.id,
                                vehicleName = vehicle?.name,
                                previousOdometer = previous,
                                vehicleError = null
                            )
                        }
                    }
                }
        }
    }

    fun loadEntry(id: Long) {
        if (id <= 0L) return
        pendingLoadId = id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            val result = runCatching { repository.getEntryById(id) }
            if (pendingLoadId != id) return@launch
            result
                .onSuccess { entry ->
                    if (entry != null) {
                        val vehicle = if (entry.vehicleId > 0L) {
                            runCatching { vehicleRepository.getVehicleById(entry.vehicleId) }
                                .getOrNull()
                        } else {
                            null
                        }
                        // The live instrument needs the reading this entry is
                        // measured against, which is the nearest lower
                        // odometer within the same vehicle rather than the
                        // newest entry in the log.
                        val previous = runCatching {
                            repository.getAllEntriesForVehicle(entry.vehicleId)
                        }
                            .getOrNull()
                            ?.filter { it.id != entry.id && it.odometer < entry.odometer }
                            ?.maxByOrNull { it.odometer }
                            ?.odometer
                        _uiState.update {
                            it.copy(
                                entryId = entry.id,
                                vehicleId = entry.vehicleId,
                                vehicleName = vehicle?.name,
                                dateMillis = entry.date,
                                odometer = entry.odometer.toString(),
                                liters = entry.liters.toString(),
                                cost = entry.cost.toString(),
                                fuelCategory = FuelCategory.fromDisplayName(entry.fuelCategory),
                                previousOdometer = previous,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loadError = FieldError.LOAD_MISSING
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = FieldError.LOAD_FAILED
                        )
                    }
                }
        }
    }

    fun retryLoad() {
        loadEntry(pendingLoadId)
    }

    fun onDateChanged(millis: Long) {
        _uiState.update {
            it.copy(
                dateMillis = millis,
                dateError = null
            )
        }
    }

    fun onOdometerChanged(value: String) {
        _uiState.update {
            it.copy(
                odometer = value,
                odometerError = null,
                odometerMonotonicContext = null
            )
        }
    }

    fun onLitersChanged(value: String) {
        _uiState.update {
            it.copy(
                liters = value,
                litersError = null
            )
        }
    }

    fun onCostChanged(value: String) {
        _uiState.update {
            it.copy(
                cost = value,
                costError = null
            )
        }
    }

    fun onFuelCategoryChanged(category: FuelCategory) {
        _uiState.update { it.copy(fuelCategory = category) }
    }

    fun saveEntry(): Boolean {
        val currentState = _uiState.value

        if (!currentState.isEditMode && currentState.vehicleId == null) {
            _uiState.update { it.copy(vehicleError = FieldError.NO_VEHICLE) }
            return false
        }

        val validationResult = FuelEntryValidator.validate(
            dateMillis = currentState.dateMillis,
            odometerStr = currentState.odometer,
            litersStr = currentState.liters,
            costStr = currentState.cost,
            previousOdometer = if (currentState.isEditMode) null else currentState.previousOdometer
        )

        if (!validationResult.isValid) {
            _uiState.update {
                it.copy(
                    dateError = validationResult.dateError,
                    odometerError = validationResult.odometerError,
                    odometerMonotonicContext = validationResult.odometerMonotonicContext,
                    litersError = validationResult.litersError,
                    costError = validationResult.costError,
                    vehicleError = null
                )
            }
            return false
        }

        viewModelScope.launch {
            val entry = FuelEntry(
                id = currentState.entryId,
                vehicleId = currentState.vehicleId ?: 0L,
                date = currentState.dateMillis,
                odometer = currentState.odometer.trim().toInt(),
                liters = currentState.liters.trim().toDouble(),
                cost = currentState.cost.trim().toDouble(),
                fuelCategory = currentState.fuelCategory.displayName
            )

            if (currentState.isEditMode) {
                repository.updateEntry(entry)
            } else {
                repository.insertEntry(entry)
            }

            _uiState.update { it.copy(isEntrySaved = true) }
        }

        return true
    }

    fun resetSavedState() {
        _uiState.update { it.copy(isEntrySaved = false) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MileLogApplication)
                AddEditViewModel(application.repository, application.vehicleRepository)
            }
        }
    }
}