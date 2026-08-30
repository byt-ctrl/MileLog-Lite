package com.example.myapplication.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.example.myapplication.R
import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.domain.validation.FieldError
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Add / Edit Fuel Entry Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEntryScreen(
    entryId: Long = 0L,
    onNavigateUp: () -> Unit,
    viewModel: AddEditViewModel = viewModel(factory = AddEditViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

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

    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (uiState.isEditMode) R.string.entry_title_edit
                            else R.string.entry_title_add
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_navigate_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                val loadingLabel = stringResource(R.string.entry_loading)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .semantics(mergeDescendants = true) { contentDescription = loadingLabel },
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.loadError != null -> {
                val loadErrorText = stringResource(uiState.loadError!!.messageRes)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = loadErrorText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = viewModel::retryLoad) {
                            Text(stringResource(R.string.action_retry))
                        }
                    }
                }
            }

            else -> {
                val datePickerA11y = stringResource(R.string.entry_field_date_a11y)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
            OutlinedTextField(
                value = dateFormatter.format(Date(uiState.dateMillis)),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.entry_field_date_label)) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.entry_field_date_a11y)
                        )
                    }
                },
                isError = uiState.dateError != null,
                supportingText = uiState.dateError?.let { {
                    Text(stringResource(it.messageRes))
                } },
                // The field itself is the picker entry: tapping anywhere opens
                // the date picker, and TalkBack reads it as a single Button so
                // keyboard / screen-reader users get one focus, one action.
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { showDatePicker = true })
                    .semantics(mergeDescendants = true) {
                        contentDescription = datePickerA11y
                    }
            )

            val odometerHelper: String? = when {
                uiState.odometerError != null -> {
                    val res = uiState.odometerError!!
                    val ctx = uiState.odometerMonotonicContext
                    if (res == FieldError.ODOMETER_NOT_MONOTONIC && ctx != null) {
                        stringResource(res.messageRes, ctx)
                    } else {
                        stringResource(res.messageRes)
                    }
                }
                uiState.previousOdometer != null && !uiState.isEditMode -> {
                    stringResource(R.string.entry_field_odometer_helper, uiState.previousOdometer!!)
                }
                else -> null
            }

            OutlinedTextField(
                value = uiState.odometer,
                onValueChange = { viewModel.onOdometerChanged(it) },
                label = { Text(stringResource(R.string.entry_field_odometer_label)) },
                suffix = { Text(stringResource(R.string.entry_field_odometer_suffix)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                isError = uiState.odometerError != null,
                supportingText = odometerHelper?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.liters,
                onValueChange = { viewModel.onLitersChanged(it) },
                label = { Text(stringResource(R.string.entry_field_liters_label)) },
                suffix = { Text(stringResource(R.string.entry_field_liters_suffix)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                isError = uiState.litersError != null,
                supportingText = uiState.litersError?.let { { Text(stringResource(it.messageRes)) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.cost,
                onValueChange = { viewModel.onCostChanged(it) },
                label = { Text(stringResource(R.string.entry_field_cost_label)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.saveEntry() }
                ),
                singleLine = true,
                isError = uiState.costError != null,
                supportingText = uiState.costError?.let { { Text(stringResource(it.messageRes)) } },
                modifier = Modifier.fillMaxWidth()
            )

            FuelCategoryDropdown(
                selected = uiState.fuelCategory,
                onCategorySelected = viewModel::onFuelCategoryChanged,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.saveEntry() },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                Text(
                    text = stringResource(
                        if (uiState.isEditMode) R.string.action_update
                        else R.string.action_save
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
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
                        datePickerState.selectedDateMillis?.let {
                            viewModel.onDateChanged(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.entry_field_date_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Material 3 exposed dropdown for selecting a [FuelCategory].
 *
 * Uses [ExposedDropdownMenuBox] with a read-only [OutlinedTextField] as the
 * anchor. Tapping the field (or the trailing chevron) opens the menu; the
 * text field is non-editable so the user can only pick a value.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FuelCategoryDropdown(
    selected: FuelCategory,
    onCategorySelected: (FuelCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = stringResource(selected.labelRes),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.entry_field_category_label)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            FuelCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(stringResource(category.labelRes)) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}