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
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.data.repository.VehicleRepository
import com.example.myapplication.domain.conversion.DistanceConverter
import com.example.myapplication.domain.conversion.DistanceUnit
import com.example.myapplication.domain.validation.FieldError
import com.example.myapplication.domain.validation.FuelEntryValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    /** Unit the odometer field, the instrument and the helper copy are shown in. */
    val distanceUnit: DistanceUnit = DistanceUnit.DEFAULT,
    /**
     * The reading exactly as stored, with the text it was rendered back as.
     * Saving an entry whose odometer was never touched writes the stored value
     * back rather than re-rounding the round trip through [distanceUnit], so
     * correcting a cost cannot move the odometer by a kilometre.
     */
    val storedOdometerKm: Int? = null,
    val storedOdometerText: String? = null,
    val isLoading: Boolean = false,
    val loadError: FieldError? = null,
    val loadErrorContext: String? = null,
    val isEntrySaved: Boolean = false
) {
    val isEditMode: Boolean get() = entryId > 0L

    /**
     * Fields the form is currently refusing, in the order they appear on the
     * sheet. Drives the banner's count and which field takes focus.
     */
    val invalidFields: List<EntryField>
        get() = buildList {
            if (dateError != null) add(EntryField.DATE)
            if (odometerError != null) add(EntryField.ODOMETER)
            if (litersError != null) add(EntryField.LITERS)
            if (costError != null) add(EntryField.COST)
            if (vehicleError != null) add(EntryField.VEHICLE)
        }
}

/**
 * The fields on the entry sheet that can be refused, in the order the sheet
 * reads them. Fuel type and date cannot fail on their own: the type is always
 * one of three and the date picker always returns a date.
 */
enum class EntryField { DATE, ODOMETER, LITERS, COST, VEHICLE }

/**
 * The kilometres the entry form should store for the odometer.
 *
 * The field is typed in whatever unit the user reads in, so a reading has to be
 * converted to the kilometres the table holds. Converting a stored reading into
 * that unit and back is not always invertible - a mile is longer than a
 * kilometre, so whole-mile rounding can move a reading by one - which is why an
 * untouched field keeps [storedKm] and only an edited one is re-derived.
 *
 * @param typed What is in the field.
 * @param unit The unit the field is in.
 * @param storedKm The reading the entry was loaded with, if it was loaded.
 * @param storedText The text [storedKm] was rendered back as, if it was loaded.
 * @return The reading in kilometres, or null when [typed] is not a number.
 */
internal fun odometerToStoreKm(
    typed: String,
    unit: DistanceUnit,
    storedKm: Int? = null,
    storedText: String? = null
): Int? {
    val trimmed = typed.trim()
    if (storedKm != null && trimmed == storedText) return storedKm
    return trimmed.toIntOrNull()
        ?.let { DistanceConverter.toKilometres(it.toDouble(), unit).roundToInt() }
}

/**
 * ViewModel managing state and validation for the Add/Edit Fuel Entry screen.
 */
class AddEditViewModel(
    private val repository: FuelEntryRepository,
    private val vehicleRepository: VehicleRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddEditUiState(distanceUnit = settingsRepository.distanceUnit.value)
    )
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
                        val vehicle = entry.vehicleId
                            ?.takeIf { it > 0L }
                            ?.let { vehicleId ->
                                runCatching { vehicleRepository.getVehicleById(vehicleId) }
                                    .getOrNull()
                            }
                        // The live instrument needs the reading this entry is
                        // measured against, which is the nearest lower
                        // odometer within the same vehicle rather than the
                        // newest entry in the log.
                        val previous = entry.vehicleId?.let { vehicleId ->
                            runCatching { repository.getAllEntriesForVehicle(vehicleId) }
                                .getOrNull()
                                ?.filter { it.id != entry.id && it.odometer < entry.odometer }
                                ?.maxByOrNull { it.odometer }
                                ?.odometer
                        }
                        val unit = _uiState.value.distanceUnit
                        val odometerText = DistanceConverter
                            .convertDistance(entry.odometer.toDouble(), unit)
                            .roundToInt()
                            .toString()
                        _uiState.update {
                            it.copy(
                                entryId = entry.id,
                                vehicleId = entry.vehicleId,
                                vehicleName = vehicle?.name,
                                dateMillis = entry.date,
                                odometer = odometerText,
                                storedOdometerKm = entry.odometer,
                                storedOdometerText = odometerText,
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

        val odometerKm = resolveOdometerKm(currentState)

        val validationResult = FuelEntryValidator.validate(
            dateMillis = currentState.dateMillis,
            // The validator, the stored reading and the previous reading all
            // speak kilometres, so a reading typed in miles is converted before
            // it is compared. A value that will not parse is passed through
            // untouched so the validator can name what is wrong with it.
            odometerStr = odometerKm?.toString() ?: currentState.odometer,
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

        // A valid result means the reading parsed and beat the previous one,
        // which is only possible if the conversion produced a value.
        val savedOdometer = odometerKm ?: return false

        viewModelScope.launch {
            val entry = FuelEntry(
                id = currentState.entryId,
                vehicleId = currentState.vehicleId,
                date = currentState.dateMillis,
                odometer = savedOdometer,
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

    /**
     * The form's odometer reading as stored kilometres, or null when what is in
     * the field is not a number.
     *
     * An untouched field on an existing entry keeps the reading it was loaded
     * with: converting to the display unit and back is not always invertible, and
     * an edit to the cost is not a reason to move the odometer.
     */
    private fun resolveOdometerKm(state: AddEditUiState): Int? = odometerToStoreKm(
        typed = state.odometer,
        unit = state.distanceUnit,
        storedKm = state.storedOdometerKm,
        storedText = state.storedOdometerText
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MileLogApplication)
                AddEditViewModel(
                    application.repository,
                    application.vehicleRepository,
                    application.settingsRepository
                )
            }
        }
    }
}