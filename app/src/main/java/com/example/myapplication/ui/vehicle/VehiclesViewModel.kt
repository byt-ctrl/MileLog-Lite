package com.example.myapplication.ui.vehicle

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.MileLogApplication
import com.example.myapplication.R
import com.example.myapplication.data.local.Vehicle
import com.example.myapplication.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Stable identifiers for vehicle-list outcomes. The UI resolves each key to a
 * localized `stringResource`.
 */
enum class VehiclesMessage(@StringRes val messageRes: Int) {
    VEHICLE_DELETED(R.string.vehicle_deleted),
    ACTION_FAILED(R.string.vehicles_action_failed)
}

data class VehiclesUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val activeVehicle: Vehicle? = null,
    val message: VehiclesMessage? = null,
    val isBusy: Boolean = false
)

/**
 * Vehicle list state.
 *
 * This owns the fleet every other screen logs against: which vehicle is active
 * (the one fill-ups are written to), and removing a vehicle together with its
 * history. It reads the same repository the dashboard and entry form do, so a
 * change here is visible everywhere without a refresh.
 */
class VehiclesViewModel(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _transient = MutableStateFlow(VehiclesUiState())

    val uiState: StateFlow<VehiclesUiState> = combine(
        vehicleRepository.getAllVehiclesFlow(),
        vehicleRepository.getActiveVehicleFlow(),
        _transient
    ) { vehicles, activeVehicle, transient ->
        transient.copy(vehicles = vehicles, activeVehicle = activeVehicle)
    }
        .catch { _ ->
            emit(VehiclesUiState(message = VehiclesMessage.ACTION_FAILED))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VehiclesUiState()
        )

    /**
     * Selects the vehicle every other screen then logs against.
     */
    fun setActiveVehicle(id: Long) {
        viewModelScope.launch {
            runCatching { vehicleRepository.setActiveVehicle(id) }
                .onFailure {
                    _transient.update { it.copy(message = VehiclesMessage.ACTION_FAILED) }
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
                        it.copy(isBusy = false, message = VehiclesMessage.VEHICLE_DELETED)
                    }
                }
                .onFailure {
                    _transient.update {
                        it.copy(isBusy = false, message = VehiclesMessage.ACTION_FAILED)
                    }
                }
        }
    }

    fun consumeMessage() {
        _transient.update { it.copy(message = null, isBusy = false) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MileLogApplication)
                VehiclesViewModel(application.vehicleRepository)
            }
        }
    }
}
