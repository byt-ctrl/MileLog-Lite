package com.example.myapplication.ui.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.domain.conversion.DistanceConverter
import com.example.myapplication.domain.conversion.DistanceUnit
import com.example.myapplication.domain.validation.FieldError
import com.example.myapplication.ui.components.GaugeScaleLabels
import com.example.myapplication.ui.components.InstrumentBand
import com.example.myapplication.ui.components.InstrumentBar
import com.example.myapplication.ui.components.LedgerPanel
import com.example.myapplication.ui.components.MileageGauge
import com.example.myapplication.ui.components.ReadoutItem
import com.example.myapplication.ui.components.ReadoutStrip
import com.example.myapplication.ui.components.SectionHeader
import com.example.myapplication.ui.components.SegmentedChoice
import com.example.myapplication.ui.components.distanceLabel
import com.example.myapplication.ui.components.formatDistanceWithUnit
import com.example.myapplication.ui.components.formatMileageWithUnit
import com.example.myapplication.ui.components.formatOne
import com.example.myapplication.ui.components.formatTick
import com.example.myapplication.ui.components.mileageLabel
import com.example.myapplication.ui.components.withUnit
import com.example.myapplication.ui.theme.MicroLabelStyle
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.theme.ledger
import com.example.myapplication.ui.theme.spacing
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private const val GAUGE_CEILING = 30.0

private data class EntryCalc(
    val odometer: Int?,
    val liters: Double?,
    val cost: Double?,
    val distance: Int?,
    val mileage: Double?,
    val costPerKm: Double?,
    val pricePerLitre: Double?
)

/**
 * Log a fill-up.
 *
 * The instrument becomes the input display: mileage, cost per kilometre and
 * price per litre read out as the numbers are typed, and a missing or low
 * odometer simply leaves the dial at zero rather than inventing a value. The
 * sheet below carries the fields in the order the entry is decided - what was
 * burned, when, how far, how much, what it cost - and a valid submit replaces
 * it with a summary of what was recorded.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEntryScreen(
    entryId: Long = 0L,
    onNavigateUp: () -> Unit,
    viewModel: AddEditViewModel = viewModel(factory = AddEditViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = MaterialTheme.spacing
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")) }
    val dateFormatter = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }

    LaunchedEffect(entryId) {
        if (entryId > 0L) {
            viewModel.loadEntry(entryId)
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        InstrumentBar(
            title = stringResource(
                if (uiState.isEditMode) R.string.entry_title_edit else R.string.entry_title_add
            ),
            onNavigateUp = onNavigateUp,
            backContentDescription = stringResource(R.string.action_navigate_back)
        )

        when {
            uiState.isLoading -> LoadingBlock()

            uiState.loadError != null -> LoadErrorBlock(
                messageRes = uiState.loadError!!.messageRes,
                onRetry = viewModel::retryLoad
            )

            else -> {
                EntryInstrument(uiState = uiState, currency = currency)

                if (uiState.isEntrySaved) {
                    EntrySavedSummary(
                        uiState = uiState,
                        currency = currency,
                        dateFormatter = dateFormatter,
                        onDone = onNavigateUp,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    EntryForm(
                        uiState = uiState,
                        viewModel = viewModel,
                        dateFormatter = dateFormatter,
                        onRequestDatePicker = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { viewModel.onDateChanged(it) }
                        showDatePicker = false
                    },
                    modifier = Modifier.heightIn(min = spacing.touchTarget)
                ) {
                    Text(stringResource(R.string.entry_field_date_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    modifier = Modifier.heightIn(min = spacing.touchTarget)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * The live readings, worked out in stored units.
 *
 * The odometer field holds whatever unit the user reads in, so it is converted
 * back to kilometres before anything is measured against it; every figure this
 * returns is kilometres or km/L, and the screens convert on the way out.
 */
private fun computeEntry(uiState: AddEditUiState): EntryCalc {
    val previous = uiState.previousOdometer
    val odometerKm = uiState.odometer.trim().toIntOrNull()?.let { typed ->
        DistanceConverter.toKilometres(typed.toDouble(), uiState.distanceUnit).roundToInt()
    }
    val liters = uiState.liters.trim().toDoubleOrNull()
    val cost = uiState.cost.trim().toDoubleOrNull()

    val distance = if (previous != null && odometerKm != null) odometerKm - previous else null
    val mileage = if (distance != null && distance > 0 && liters != null && liters > 0) {
        distance.toDouble() / liters
    } else {
        null
    }
    val costPerKm = if (distance != null && distance > 0 && cost != null && cost > 0) {
        cost / distance
    } else {
        null
    }
    val pricePerLitre = if (liters != null && liters > 0 && cost != null && cost > 0) {
        cost / liters
    } else {
        null
    }

    return EntryCalc(
        odometer = odometerKm,
        liters = liters,
        cost = cost,
        distance = distance,
        mileage = mileage,
        costPerKm = costPerKm,
        pricePerLitre = pricePerLitre
    )
}

@Composable
private fun EntryInstrument(
    uiState: AddEditUiState,
    currency: NumberFormat
) {
    val ledger = MaterialTheme.ledger
    val spacing = MaterialTheme.spacing
    val unit = uiState.distanceUnit
    val calc = computeEntry(uiState)
    val ceiling = DistanceConverter.convertMileage(GAUGE_CEILING, unit)

    InstrumentBand {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = stringResource(R.string.entry_instrument_label).uppercase(),
                style = MicroLabelStyle,
                color = ledger.chromeTextMuted,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = unit.mileageLabel(),
                style = MaterialTheme.typography.labelSmall,
                color = ledger.chromeTextMuted
            )
        }

        uiState.vehicleName?.let { name ->
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = stringResource(
                    R.string.vehicle_active_note,
                    name,
                    stringResource(uiState.fuelCategory.labelRes)
                ),
                style = MaterialTheme.typography.labelSmall,
                color = ledger.chromeReadout
            )
        }

        Spacer(Modifier.height(spacing.sm))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = calc.mileage?.let { formatOne(DistanceConverter.convertMileage(it, unit)) }
                    ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
                style = MaterialTheme.typography.displayMedium,
                color = ledger.chromeText
            )
            Text(
                text = unit.mileageLabel(),
                style = MaterialTheme.typography.titleMedium,
                color = ledger.chromeReadout,
                modifier = Modifier.padding(start = spacing.sm, bottom = 4.dp)
            )
        }

        Spacer(Modifier.height(spacing.md))

        MileageGauge(
            value = calc.mileage?.let { DistanceConverter.convertMileage(it, unit) },
            ceiling = ceiling,
            contentDescription = instrumentA11y(calc.mileage, ceiling, unit)
        )

        Spacer(Modifier.height(spacing.sm))

        GaugeScaleLabels(ceiling = ceiling)

        Spacer(Modifier.height(spacing.sm))

        Text(
            text = instrumentNote(uiState, unit),
            style = MaterialTheme.typography.labelSmall,
            color = ledger.chromeTextMuted
        )

        Spacer(Modifier.height(spacing.lg))

        ReadoutStrip(
            items = listOf(
                ReadoutItem(
                    label = stringResource(R.string.entry_readout_distance),
                    value = calc.distance?.let { formatDistanceWithUnit(it.toDouble(), unit) }
                        ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
                    note = stringResource(R.string.entry_readout_distance_note)
                ),
                ReadoutItem(
                    label = stringResource(
                        R.string.dashboard_stat_cost_per_distance_title,
                        unit.distanceLabel()
                    ),
                    value = calc.costPerKm?.let { costPerKm ->
                        currency.format(DistanceConverter.convertCostPerDistance(costPerKm, unit))
                    } ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
                    note = stringResource(R.string.entry_readout_cost_per_km_note)
                ),
                ReadoutItem(
                    label = stringResource(R.string.entry_readout_price_per_litre),
                    value = calc.pricePerLitre?.let { currency.format(it) }
                        ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
                    note = stringResource(R.string.entry_readout_price_per_litre_note)
                )
            ),
            compact = true
        )
    }
}

@Composable
private fun instrumentNote(uiState: AddEditUiState, unit: DistanceUnit): String {
    val previous = uiState.previousOdometer
    val previousText = previous?.let { formatDistanceWithUnit(it.toDouble(), unit) }
    return when {
        previousText == null -> stringResource(R.string.entry_instrument_note_no_previous)
        computeEntry(uiState).mileage == null ->
            stringResource(R.string.entry_instrument_note_idle, previousText)
        else -> stringResource(R.string.entry_instrument_note_ready, previousText)
    }
}

@Composable
private fun instrumentA11y(mileage: Double?, ceiling: Double, unit: DistanceUnit): String {
    val scale = withUnit(formatTick(ceiling), unit.mileageLabel())
    return if (mileage == null) {
        stringResource(R.string.entry_instrument_a11y_empty, scale)
    } else {
        stringResource(R.string.entry_instrument_a11y, formatMileageWithUnit(mileage, unit), scale)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryForm(
    uiState: AddEditUiState,
    viewModel: AddEditViewModel,
    dateFormatter: SimpleDateFormat,
    onRequestDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val unit = uiState.distanceUnit
    val datePickerA11y = stringResource(R.string.entry_field_date_a11y)

    val dateFocus = remember { FocusRequester() }
    val odometerFocus = remember { FocusRequester() }
    val litersFocus = remember { FocusRequester() }
    val costFocus = remember { FocusRequester() }

    // Invalid fields are all reported at once, then focus lands on the first of
    // them so the fix is one tap away rather than a scroll away. The order is
    // the order the sheet reads them, declared once on EntryField.
    val focusByField = remember(dateFocus, odometerFocus, litersFocus, costFocus) {
        mapOf(
            EntryField.DATE to dateFocus,
            EntryField.ODOMETER to odometerFocus,
            EntryField.LITERS to litersFocus,
            EntryField.COST to costFocus
        )
    }
    LaunchedEffect(uiState.invalidFields) {
        // A field with no input of its own, like a missing vehicle, has nothing
        // to focus; the banner and its inline message carry it instead.
        uiState.invalidFields.firstNotNullOfOrNull { focusByField[it] }?.requestFocus()
    }

    val odometerHelper: String? = when {
        uiState.odometerError != null -> {
            val res = uiState.odometerError!!
            val context = uiState.odometerMonotonicContext
            if (res == FieldError.ODOMETER_NOT_MONOTONIC && context != null) {
                stringResource(res.messageRes, formatDistanceWithUnit(context.toDouble(), unit))
            } else {
                stringResource(res.messageRes)
            }
        }
        uiState.previousOdometer != null -> {
            stringResource(
                R.string.entry_field_odometer_helper,
                formatDistanceWithUnit(uiState.previousOdometer!!.toDouble(), unit)
            )
        }
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.lg)
    ) {
        if (uiState.invalidFields.isNotEmpty()) {
            ValidationBanner(count = uiState.invalidFields.size)
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            EntryFieldLabel(text = stringResource(R.string.entry_field_category_label))
            SegmentedChoice(
                options = FuelCategory.entries,
                selected = uiState.fuelCategory,
                label = { stringResource(it.labelRes) },
                onSelect = viewModel::onFuelCategoryChanged
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            EntryFieldLabel(text = stringResource(R.string.entry_field_date_label))
            OutlinedTextField(
                value = dateFormatter.format(Date(uiState.dateMillis)),
                onValueChange = {},
                readOnly = true,
                label = null,
                trailingIcon = {
                    IconButton(onClick = onRequestDatePicker) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.entry_field_date_a11y)
                        )
                    }
                },
                shape = MileLogShapes.sm,
                colors = entryFieldColors(),
                isError = uiState.dateError != null,
                supportingText = uiState.dateError?.let { { Text(stringResource(it.messageRes)) } },
                // The field itself is the picker entry: tapping anywhere opens the
                // date picker, and it collapses to a single focus for TalkBack.
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(dateFocus)
                    .clickable(onClick = onRequestDatePicker)
                    .semantics(mergeDescendants = true) {
                        contentDescription = datePickerA11y
                    }
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            EntryFieldLabel(text = stringResource(R.string.entry_field_odometer_label))
            OutlinedTextField(
                value = uiState.odometer,
                onValueChange = viewModel::onOdometerChanged,
                label = null,
                suffix = { Text(stringResource(unit.labelRes)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = MileLogShapes.sm,
                colors = entryFieldColors(),
                isError = uiState.odometerError != null,
                supportingText = odometerHelper?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(odometerFocus)
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            EntryFieldLabel(text = stringResource(R.string.entry_field_liters_label))
            OutlinedTextField(
                value = uiState.liters,
                onValueChange = viewModel::onLitersChanged,
                label = null,
                suffix = { Text(stringResource(R.string.entry_field_liters_suffix)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = MileLogShapes.sm,
                colors = entryFieldColors(),
                isError = uiState.litersError != null,
                supportingText = uiState.litersError?.let { { Text(stringResource(it.messageRes)) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(litersFocus)
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            EntryFieldLabel(text = stringResource(R.string.entry_field_cost_label))
            OutlinedTextField(
                value = uiState.cost,
                onValueChange = viewModel::onCostChanged,
                label = null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { viewModel.saveEntry() }),
                singleLine = true,
                shape = MileLogShapes.sm,
                colors = entryFieldColors(),
                isError = uiState.costError != null,
                supportingText = uiState.costError?.let { { Text(stringResource(it.messageRes)) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(costFocus)
            )
        }

        uiState.vehicleError?.let { error ->
            Text(
                text = stringResource(error.messageRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(spacing.sm))

        // Save is never disabled: a disabled button hides the thing that needs
        // fixing, and every reason a submit can fail is already on screen.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Button(
                onClick = viewModel::saveEntry,
                modifier = Modifier.heightIn(min = 52.dp),
                shape = MileLogShapes.sm
            ) {
                Text(
                    text = stringResource(
                        if (uiState.isEditMode) {
                            R.string.entry_action_save_changes
                        } else {
                            R.string.entry_action_save
                        }
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = stringResource(R.string.entry_save_destination),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** The count of fields the form is refusing, announced rather than only drawn. */
@Composable
private fun ValidationBanner(count: Int) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.colorScheme
    val message = pluralStringResource(R.plurals.entry_validation_banner, count, count)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MileLogShapes.sm)
            .background(colors.errorContainer)
            .border(1.dp, colors.error, MileLogShapes.sm)
            .padding(horizontal = spacing.md, vertical = spacing.md)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = message
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = colors.error,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onErrorContainer,
            modifier = Modifier.padding(start = spacing.sm)
        )
    }
}

/**
 * What was recorded, shown in place of the sheet once the write lands. The
 * entry itself is already saved, so this is a receipt rather than a
 * confirmation step.
 */
@Composable
private fun EntrySavedSummary(
    uiState: AddEditUiState,
    currency: NumberFormat,
    dateFormatter: SimpleDateFormat,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val unit = uiState.distanceUnit
    val calc = computeEntry(uiState)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.lg)
    ) {
        SectionHeader(
            title = stringResource(
                if (uiState.isEditMode) {
                    R.string.entry_saved_title_edit
                } else {
                    R.string.entry_saved_title
                }
            ),
            note = stringResource(R.string.entry_save_destination)
        )

        LedgerPanel {
            SummaryRow(
                label = stringResource(R.string.vehicle_preview_label),
                value = uiState.vehicleName ?: stringResource(R.string.dashboard_stat_latest_odometer_empty)
            )
            SummaryDivider()
            SummaryRow(
                label = stringResource(R.string.entry_field_category_label),
                value = stringResource(uiState.fuelCategory.labelRes)
            )
            SummaryDivider()
            SummaryRow(
                label = stringResource(R.string.entry_field_date_label),
                value = dateFormatter.format(Date(uiState.dateMillis))
            )
            SummaryDivider()
            SummaryRow(
                label = stringResource(R.string.entry_field_odometer_label),
                value = calc.odometer?.let { formatDistanceWithUnit(it.toDouble(), unit) }
                    ?: stringResource(R.string.dashboard_stat_latest_odometer_empty)
            )
            SummaryDivider()
            SummaryRow(
                label = stringResource(R.string.entry_field_liters_label),
                value = calc.liters?.let {
                    stringResource(R.string.dashboard_ledger_liters_value, it)
                } ?: stringResource(R.string.dashboard_stat_latest_odometer_empty)
            )
            SummaryDivider()
            SummaryRow(
                label = stringResource(R.string.entry_field_cost_label),
                value = calc.cost?.let { currency.format(it) }
                    ?: stringResource(R.string.dashboard_stat_latest_odometer_empty)
            )
            calc.mileage?.let { mileage ->
                SummaryDivider()
                SummaryRow(
                    label = stringResource(R.string.ledger_column_mileage),
                    value = formatMileageWithUnit(mileage, unit)
                )
            }
            calc.pricePerLitre?.let { price ->
                SummaryDivider()
                SummaryRow(
                    label = stringResource(R.string.entry_readout_price_per_litre),
                    value = currency.format(price)
                )
            }
        }

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = MileLogShapes.sm
        ) {
            Text(
                text = stringResource(R.string.entry_saved_done),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = spacing.md)
        )
    }
}

@Composable
private fun SummaryDivider() {
    HorizontalDivider(color = MaterialTheme.ledger.rule, thickness = 1.dp)
}

@Composable
private fun entryFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedBorderColor = MaterialTheme.colorScheme.primary
)

@Composable
private fun EntryFieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MicroLabelStyle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm)
    )
}

@Composable
private fun LoadingBlock() {
    val label = stringResource(R.string.entry_loading)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics(mergeDescendants = true) { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LoadErrorBlock(messageRes: Int, onRetry: () -> Unit) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.xl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(spacing.lg))
            Button(onClick = onRetry, shape = MileLogShapes.sm) {
                Text(stringResource(R.string.action_retry))
            }
        }
    }
}
