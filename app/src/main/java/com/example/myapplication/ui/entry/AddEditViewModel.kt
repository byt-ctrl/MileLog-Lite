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
import com.example.myapplication.domain.validation.FuelEntryValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State representing the Add/Edit Fuel Entry form.
 */
data class AddEditUiState(
    val entryId: Long = 0L,
    val dateMillis: Long = System.currentTimeMillis(),
    val odometer: String = "",
    val liters: String = "",
    val cost: String = "",
    val fuelCategory: FuelCategory = FuelCategory.DEFAULT,
    val dateError: String? = null,
    val odometerError: String? = null,
    val litersError: String? = null,
    val costError: String? = null,
    val previousOdometer: Int? = null,
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val isEntrySaved: Boolean = false
) {
    val isEditMode: Boolean get() = entryId > 0L
}

/**
 * ViewModel managing state and validation for the Add/Edit Fuel Entry screen.
 */
class AddEditViewModel(
    private val repository: FuelEntryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    init {
        loadLatestOdometer()
    }

    private var pendingLoadId: Long = 0L

    private fun loadLatestOdometer() {
        viewModelScope.launch {
            runCatching { repository.getLatestEntry() }
                .onSuccess { latest ->
                    _uiState.update { it.copy(previousOdometer = latest?.odometer) }
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
                        _uiState.update {
                            it.copy(
                                entryId = entry.id,
                                dateMillis = entry.date,
                                odometer = entry.odometer.toString(),
                                liters = entry.liters.toString(),
                                cost = entry.cost.toString(),
                                fuelCategory = FuelCategory.fromDisplayName(entry.fuelCategory),
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loadError = "This entry no longer exists. It may have been deleted."
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = "Couldn't load this entry. Please try again."
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
                odometerError = null
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
                    litersError = validationResult.litersError,
                    costError = validationResult.costError
                )
            }
            return false
        }

        viewModelScope.launch {
            val entry = FuelEntry(
                id = currentState.entryId,
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
                AddEditViewModel(application.repository)
            }
        }
    }
}
