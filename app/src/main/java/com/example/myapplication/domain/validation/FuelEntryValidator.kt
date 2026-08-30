package com.example.myapplication.domain.validation

import androidx.annotation.StringRes
import com.example.myapplication.R

/**
 * Stable error identifiers for fuel-entry form validation. The domain layer
 * returns these keys; the UI layer resolves them to localized messages via
 * `stringResource()` so the validator stays free of Android resources.
 */
enum class FieldError(@StringRes val messageRes: Int) {
    DATE_REQUIRED(R.string.error_date_required),
    ODOMETER_REQUIRED(R.string.error_odometer_required),
    ODOMETER_NOT_POSITIVE(R.string.error_odometer_positive),
    ODOMETER_NOT_MONOTONIC(R.string.error_odometer_monotonic),
    LITERS_REQUIRED(R.string.error_liters_required),
    LITERS_NOT_POSITIVE(R.string.error_liters_positive),
    COST_REQUIRED(R.string.error_cost_required),
    COST_NOT_POSITIVE(R.string.error_cost_positive),
    LOAD_MISSING(R.string.entry_error_load_missing),
    LOAD_FAILED(R.string.entry_error_load_failed)
}

/**
 * Result of validating a fuel entry input form. Each field carries either
 * null (valid) or a [FieldError] key. The UI resolves the key to the
 * locale-appropriate message and supplies any required arguments.
 */
data class ValidationResult(
    val isValid: Boolean,
    val dateError: FieldError? = null,
    val odometerError: FieldError? = null,
    val odometerMonotonicContext: Int? = null,
    val litersError: FieldError? = null,
    val costError: FieldError? = null
)

/**
 * Validates fuel entry inputs according to business rules.
 */
object FuelEntryValidator {

    /**
     * Validates raw form string inputs.
     *
     * @param dateMillis Date timestamp in milliseconds.
     * @param odometerStr Odometer string input.
     * @param litersStr Fuel volume string input.
     * @param costStr Total cost string input.
     * @param previousOdometer Previous highest odometer reading (if available).
     */
    fun validate(
        dateMillis: Long?,
        odometerStr: String,
        litersStr: String,
        costStr: String,
        previousOdometer: Int? = null
    ): ValidationResult {
        var dateError: FieldError? = null
        var odometerError: FieldError? = null
        var odometerMonotonicContext: Int? = null
        var litersError: FieldError? = null
        var costError: FieldError? = null

        if (dateMillis == null || dateMillis <= 0) {
            dateError = FieldError.DATE_REQUIRED
        }

        val odometerInt = odometerStr.trim().toIntOrNull()
        if (odometerStr.trim().isEmpty()) {
            odometerError = FieldError.ODOMETER_REQUIRED
        } else if (odometerInt == null || odometerInt <= 0) {
            odometerError = FieldError.ODOMETER_NOT_POSITIVE
        } else if (previousOdometer != null && odometerInt <= previousOdometer) {
            odometerError = FieldError.ODOMETER_NOT_MONOTONIC
            odometerMonotonicContext = previousOdometer
        }

        val litersDouble = litersStr.trim().toDoubleOrNull()
        if (litersStr.trim().isEmpty()) {
            litersError = FieldError.LITERS_REQUIRED
        } else if (litersDouble == null || litersDouble <= 0.0) {
            litersError = FieldError.LITERS_NOT_POSITIVE
        }

        val costDouble = costStr.trim().toDoubleOrNull()
        if (costStr.trim().isEmpty()) {
            costError = FieldError.COST_REQUIRED
        } else if (costDouble == null || costDouble <= 0.0) {
            costError = FieldError.COST_NOT_POSITIVE
        }

        val isValid = dateError == null && odometerError == null &&
            litersError == null && costError == null

        return ValidationResult(
            isValid = isValid,
            dateError = dateError,
            odometerError = odometerError,
            odometerMonotonicContext = odometerMonotonicContext,
            litersError = litersError,
            costError = costError
        )
    }
}