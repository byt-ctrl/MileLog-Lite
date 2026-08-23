package com.example.myapplication.domain.validation

/**
 * Result of validating a fuel entry input form.
 */
data class ValidationResult(
    val isValid: Boolean,
    val dateError: String? = null,
    val odometerError: String? = null,
    val litersError: String? = null,
    val costError: String? = null
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
        var dateError: String? = null
        var odometerError: String? = null
        var litersError: String? = null
        var costError: String? = null

        // 1. Validate Date
        if (dateMillis == null || dateMillis <= 0) {
            dateError = "Please select a valid date"
        }

        // 2. Validate Odometer
        val odometerInt = odometerStr.trim().toIntOrNull()
        if (odometerStr.trim().isEmpty()) {
            odometerError = "Odometer reading is required"
        } else if (odometerInt == null || odometerInt <= 0) {
            odometerError = "Odometer must be a positive number"
        } else if (previousOdometer != null && odometerInt <= previousOdometer) {
            odometerError = "Odometer must be greater than previous reading ($previousOdometer km)"
        }

        // 3. Validate Liters (Fuel Volume)
        val litersDouble = litersStr.trim().toDoubleOrNull()
        if (litersStr.trim().isEmpty()) {
            litersError = "Fuel quantity is required"
        } else if (litersDouble == null || litersDouble <= 0.0) {
            litersError = "Fuel quantity must be greater than 0"
        }

        // 4. Validate Cost
        val costDouble = costStr.trim().toDoubleOrNull()
        if (costStr.trim().isEmpty()) {
            costError = "Total cost is required"
        } else if (costDouble == null || costDouble <= 0.0) {
            costError = "Cost must be greater than 0"
        }

        val isValid = dateError == null && odometerError == null && litersError == null && costError == null

        return ValidationResult(
            isValid = isValid,
            dateError = dateError,
            odometerError = odometerError,
            litersError = litersError,
            costError = costError
        )
    }
}
