package com.example.myapplication.ui.entry

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.domain.validation.FieldError
import com.example.myapplication.ui.components.GaugeScaleLabels
import com.example.myapplication.ui.components.InstrumentBar
import com.example.myapplication.ui.components.MileageGauge
import com.example.myapplication.ui.components.ReadoutItem
import com.example.myapplication.ui.components.ReadoutStrip
import com.example.myapplication.ui.components.formatOne
import com.example.myapplication.ui.theme.MicroLabelStyle
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.theme.ledger
import com.example.myapplication.ui.theme.spacing
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val GAUGE_CEILING = 30.0

private data class EntryCalc(
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
 * form below carries the fields, and the reading strip stays on screen while
 * the form scrolls.
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
    val integer = remember { NumberFormat.getIntegerInstance(Locale.getDefault()) }

    LaunchedEffect(entryId) {
        if (entryId > 0L) {
            viewModel.loadEntry(entryId)
        }
    }

    LaunchedEffect(uiState.isEntrySaved) {
        if (uiState.isEntrySaved) {
            viewModel.resetSavedState()
            onNavigateUp()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

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
                EntryInstrument(uiState = uiState, currency = currency, integer = integer)
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

private fun computeEntry(uiState: AddEditUiState): EntryCalc {
    val previous = uiState.previousOdometer
    val odometer = uiState.odometer.trim().toIntOrNull()
    val liters = uiState.liters.trim().toDoubleOrNull()
    val cost = uiState.cost.trim().toDoubleOrNull()

    val distance = if (previous != null && odometer != null) odometer - previous else null
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

    return EntryCalc(distance, mileage, costPerKm, pricePerLitre)
}

@Composable
private fun EntryInstrument(
    uiState: AddEditUiState,
    currency: NumberFormat,
    integer: NumberFormat
) {
    val ledger = MaterialTheme.ledger
    val spacing = MaterialTheme.spacing
    val calc = computeEntry(uiState)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ledger.chrome)
            .padding(horizontal = spacing.lg, vertical = spacing.lg)
    ) {
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
                text = stringResource(R.string.entry_instrument_unit),
                style = MaterialTheme.typography.labelSmall,
                color = ledger.chromeTextMuted
            )
        }

        Spacer(Modifier.height(spacing.sm))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = calc.mileage?.let { formatOne(it) }
                    ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
                style = MaterialTheme.typography.displayMedium,
                color = ledger.chromeText
            )
            Text(
                text = stringResource(R.string.dashboard_gauge_unit),
                style = MaterialTheme.typography.titleMedium,
                color = ledger.chromeReadout,
                modifier = Modifier.padding(start = spacing.sm, bottom = 4.dp)
            )
        }

        Spacer(Modifier.height(spacing.md))

        MileageGauge(
            value = calc.mileage,
            ceiling = GAUGE_CEILING,
            contentDescription = instrumentA11y(calc.mileage)
        )

        Spacer(Modifier.height(spacing.sm))

        GaugeScaleLabels(ceiling = GAUGE_CEILING)

        Spacer(Modifier.height(spacing.sm))

        Text(
            text = instrumentNote(uiState, integer),
            style = MaterialTheme.typography.labelSmall,
            color = ledger.chromeTextMuted
        )

        Spacer(Modifier.height(spacing.lg))

        ReadoutStrip(
            items = listOf(
                ReadoutItem(
                    label = stringResource(R.string.entry_readout_distance),
                    value = calc.distance?.let { integer.format(it) + " km" }
                        ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
                    note = stringResource(R.string.entry_readout_distance_note)
                ),
                ReadoutItem(
                    label = stringResource(R.string.dashboard_stat_cost_per_km_title),
                    value = calc.costPerKm?.let { currency.format(it) }
                        ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
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
private fun instrumentNote(uiState: AddEditUiState, integer: NumberFormat): String {
    val previous = uiState.previousOdometer
    return when {
        previous == null -> stringResource(R.string.entry_instrument_note_no_previous)
        computeEntry(uiState).mileage == null ->
            stringResource(R.string.entry_instrument_note_idle, integer.format(previous))
        else -> stringResource(R.string.entry_instrument_note_ready, integer.format(previous))
    }
}

@Composable
private fun instrumentA11y(mileage: Double?): String = if (mileage == null) {
    stringResource(R.string.entry_instrument_a11y_empty, GAUGE_CEILING.toInt())
} else {
    stringResource(R.string.entry_instrument_a11y, formatOne(mileage), GAUGE_CEILING.toInt())
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
    val datePickerA11y = stringResource(R.string.entry_field_date_a11y)

    val odometerHelper: String? = when {
        uiState.odometerError != null -> {
            val res = uiState.odometerError!!
            val context = uiState.odometerMonotonicContext
            if (res == FieldError.ODOMETER_NOT_MONOTONIC && context != null) {
                stringResource(res.messageRes, context)
            } else {
                stringResource(res.messageRes)
            }
        }
        uiState.previousOdometer != null -> {
            stringResource(R.string.entry_field_odometer_helper, uiState.previousOdometer!!)
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
                suffix = { Text(stringResource(R.string.entry_field_odometer_suffix)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = MileLogShapes.sm,
                colors = entryFieldColors(),
                isError = uiState.odometerError != null,
                supportingText = odometerHelper?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            EntryFieldLabel(text = stringResource(R.string.entry_field_category_label))
            FuelCategorySegment(
                selected = uiState.fuelCategory,
                onSelected = viewModel::onFuelCategoryChanged
            )
        }

        Spacer(Modifier.height(spacing.sm))

        Button(
            onClick = viewModel::saveEntry,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = MileLogShapes.sm
        ) {
            Text(
                text = stringResource(
                    if (uiState.isEditMode) R.string.action_update else R.string.action_save
                ),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun FuelCategorySegment(
    selected: FuelCategory,
    onSelected: (FuelCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val ledger = MaterialTheme.ledger
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MileLogShapes.sm)
            .background(ledger.rule)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        FuelCategory.entries.forEach { category ->
            val isSelected = category == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MileLogShapes.sm)
                    .background(if (isSelected) colors.primary else colors.surface)
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelected(category) }
                    )
                    .heightIn(min = 44.dp)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(category.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) colors.onPrimary else colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
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
