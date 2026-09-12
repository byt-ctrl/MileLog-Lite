package com.example.myapplication.domain.validation

import androidx.annotation.StringRes
import com.example.myapplication.R

/**
 * Stable error identifiers for vehicle form validation. The domain layer returns
 * these keys; the UI layer resolves them to localized messages via
 * `stringResource()` so the validator stays free of Android resources.
 */
enum class VehicleFieldError(@StringRes val messageRes: Int) {
    NAME_REQUIRED(R.string.error_vehicle_name_required),
    NAME_DUPLICATE(R.string.error_vehicle_name_duplicate),
    LOAD_MISSING(R.string.vehicle_error_load_missing),
    LOAD_FAILED(R.string.vehicle_error_load_failed)
}

/**
 * Result of validating a vehicle input form. Each field carries either null
 * (valid) or a [VehicleFieldError] key.
 */
data class VehicleValidationResult(
    val isValid: Boolean,
    val nameError: VehicleFieldError? = null
)

/**
 * Validates vehicle inputs according to business rules.
 */
object VehicleValidator {

    /**
     * Validates the vehicle name.
     *
     * @param name raw name input.
     * @param otherVehicleNames names of every other vehicle, used to reject
     *   duplicates. Callers exclude the vehicle being edited so a no-op rename
     *   does not collide with itself.
     */
    fun validate(
        name: String,
        otherVehicleNames: Collection<String> = emptyList()
    ): VehicleValidationResult {
        val trimmed = name.trim()
        val nameError = when {
            trimmed.isEmpty() -> VehicleFieldError.NAME_REQUIRED
            otherVehicleNames.any { it.equals(trimmed, ignoreCase = true) } ->
                VehicleFieldError.NAME_DUPLICATE
            else -> null
        }

        return VehicleValidationResult(
            isValid = nameError == null,
            nameError = nameError
        )
    }
}
