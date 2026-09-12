package com.example.myapplication.ui.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.MileLogApplication
import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.data.local.Vehicle
import com.example.myapplication.data.repository.VehicleRepository
import com.example.myapplication.domain.validation.VehicleFieldError
import com.example.myapplication.domain.validation.VehicleValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State representing the Add/Edit Vehicle form.
 *
 * Error fields carry [VehicleFieldError] keys; the UI resolves them via
 * `stringResource()` so error copy follows the user's locale.
 */
data class AddEditVehicleUiState(
    val vehicleId: Long = 0L,
    val name: String = "",
    val make: String = "",
    val model: String = "",
    val registrationNumber: String = "",
    val fuelType: FuelCategory = FuelCategory.DEFAULT,
    val nameError: VehicleFieldError? = null,
    val saveError: VehicleFieldError? = null,
    val isLoading: Boolean = false,
    val loadError: VehicleFieldError? = null,
    val isSaved: Boolean = false
) {
    val isEditMode: Boolean get() = vehicleId > 0L
}

/**
 * ViewModel managing state and validation for the Add/Edit Vehicle screen.
 */
class AddEditVehicleViewModel(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditVehicleUiState())
    val uiState: StateFlow<AddEditVehicleUiState> = _uiState.asStateFlow()

    private var pendingLoadId: Long = 0L

    fun loadVehicle(id: Long) {
        if (id <= 0L) return
        pendingLoadId = id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            val result = runCatching { vehicleRepository.getVehicleById(id) }
            if (pendingLoadId != id) return@launch
            result
                .onSuccess { vehicle ->
                    if (vehicle != null) {
                        _uiState.update {
                            it.copy(
                                vehicleId = vehicle.id,
                                name = vehicle.name,
                                make = vehicle.make,
                                model = vehicle.model,
                                registrationNumber = vehicle.registrationNumber,
                                fuelType = FuelCategory.fromDisplayName(vehicle.fuelType),
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loadError = VehicleFieldError.LOAD_MISSING
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = VehicleFieldError.LOAD_FAILED
                        )
                    }
                }
        }
    }

    fun retryLoad() {
        loadVehicle(pendingLoadId)
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, saveError = null) }
    }

    fun onMakeChanged(value: String) {
        _uiState.update { it.copy(make = value) }
    }

    fun onModelChanged(value: String) {
        _uiState.update { it.copy(model = value) }
    }

    fun onRegistrationChanged(value: String) {
        _uiState.update { it.copy(registrationNumber = value) }
    }

    fun onFuelTypeChanged(category: FuelCategory) {
        _uiState.update { it.copy(fuelType = category) }
    }

    /**
     * Validates and persists the vehicle. A brand new vehicle becomes active
     * when nothing is selected yet, so the app always has something to log
     * against once the first vehicle exists.
     */
    fun save() {
        val current = _uiState.value
        viewModelScope.launch {
            val otherNames = runCatching {
                vehicleRepository.getAllVehicles()
                    .filter { it.id != current.vehicleId }
                    .map { it.name }
            }.getOrElse { emptyList() }

            val validation = VehicleValidator.validate(current.name, otherNames)
            if (!validation.isValid) {
                _uiState.update { it.copy(nameError = validation.nameError) }
                return@launch
            }

            val vehicle = Vehicle(
                id = current.vehicleId,
                name = current.name.trim(),
                make = current.make.trim(),
                model = current.model.trim(),
                registrationNumber = current.registrationNumber.trim(),
                fuelType = current.fuelType.displayName
            )

            runCatching {
                if (current.isEditMode) {
                    val existing = vehicleRepository.getVehicleById(current.vehicleId)
                    vehicleRepository.updateVehicle(
                        vehicle.copy(isActive = existing?.isActive == true)
                    )
                } else {
                    val newId = vehicleRepository.insertVehicle(vehicle)
                    if (vehicleRepository.getActiveVehicle() == null) {
                        vehicleRepository.setActiveVehicle(newId)
                    }
                }
            }
                .onSuccess {
                    _uiState.update { it.copy(isSaved = true) }
                }
                .onFailure {
                    _uiState.update { it.copy(saveError = VehicleFieldError.LOAD_FAILED) }
                }
        }
    }

    fun resetSavedState() {
        _uiState.update { it.copy(isSaved = false) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MileLogApplication)
                AddEditVehicleViewModel(application.vehicleRepository)
            }
        }
    }
}
