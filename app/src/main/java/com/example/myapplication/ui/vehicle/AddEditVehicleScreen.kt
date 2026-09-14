package com.example.myapplication.ui.vehicle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.domain.conversion.DistanceUnit
import com.example.myapplication.ui.components.InstrumentBand
import com.example.myapplication.ui.components.InstrumentBar
import com.example.myapplication.ui.components.LogbookContent
import com.example.myapplication.ui.components.SegmentedChoice
import com.example.myapplication.ui.theme.MicroLabelStyle
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.theme.MileLogWindow
import com.example.myapplication.ui.theme.ledger
import com.example.myapplication.ui.theme.spacing

/**
 * Add or edit a vehicle.
 *
 * The instrument states the vehicle being configured, then the form below
 * carries the fields every screen reads back: the name shown in the header, the
 * make and model, an optional registration, the default fuel type, and the unit
 * every distance is printed in.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditVehicleScreen(
    vehicleId: Long = 0L,
    onNavigateUp: () -> Unit,
    viewModel: AddEditVehicleViewModel = viewModel(factory = AddEditVehicleViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(vehicleId) {
        if (vehicleId > 0L) {
            viewModel.loadVehicle(vehicleId)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.resetSavedState()
            onNavigateUp()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Same two-question frame as the entry sheet: the content width decides
        // whether the preview sits beside the fields, and it is the screen's own
        // frame, not the window, so a rail on screen is already accounted for.
        val sideBySide = maxWidth >= MileLogWindow.wide

        Column(modifier = Modifier.fillMaxSize()) {
            InstrumentBar(
                title = stringResource(
                    if (uiState.isEditMode) R.string.vehicle_title_edit else R.string.vehicle_title_add
                ),
                onNavigateUp = onNavigateUp,
                backContentDescription = stringResource(R.string.action_navigate_back)
            )

            when {
                uiState.isLoading -> VehicleLoadingBlock()

                uiState.loadError != null -> VehicleLoadErrorBlock(
                    messageRes = uiState.loadError!!.messageRes,
                    onRetry = viewModel::retryLoad
                )

                else -> {
                    // One scroll context for the whole sheet. The instrument is
                    // the top of the page rather than a pinned header, so the
                    // keyboard shortens the entire form and the vehicle preview
                    // scrolls with the fields it describes.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .imePadding()
                            .imeNestedScroll()
                    ) {
                        if (sideBySide) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xl),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    VehicleInstrument(uiState = uiState)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    VehicleForm(
                                        uiState = uiState,
                                        viewModel = viewModel
                                    )
                                }
                            }
                        } else {
                            VehicleInstrument(uiState = uiState)
                            LogbookContent {
                                VehicleForm(
                                    uiState = uiState,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleInstrument(uiState: AddEditVehicleUiState) {
    val ledger = MaterialTheme.ledger
    val spacing = MaterialTheme.spacing

    val name = uiState.name.trim().ifEmpty {
        stringResource(R.string.vehicle_preview_placeholder)
    }
    val details = listOf(uiState.make.trim(), uiState.model.trim(), uiState.fuelType.displayName)
        .filter { it.isNotEmpty() }
        .joinToString("  ·  ")

    InstrumentBand {
        Text(
            text = stringResource(R.string.vehicle_preview_label).uppercase(),
            style = MicroLabelStyle,
            color = ledger.chromeTextMuted
        )
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = name,
            style = MaterialTheme.typography.displayMedium,
            color = ledger.chromeText
        )
        if (details.isNotEmpty()) {
            Spacer(Modifier.height(spacing.sm))
            Text(
                text = details,
                style = MaterialTheme.typography.labelSmall,
                color = ledger.chromeTextMuted
            )
        }
    }
}

@Composable
private fun VehicleForm(
    uiState: AddEditVehicleUiState,
    viewModel: AddEditVehicleViewModel,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.lg)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            VehicleFieldLabel(text = stringResource(R.string.vehicle_field_name_label))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChanged,
                label = null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = MileLogShapes.sm,
                colors = vehicleFieldColors(),
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(stringResource(it.messageRes)) } },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            VehicleFieldLabel(text = stringResource(R.string.vehicle_field_make_label))
            OutlinedTextField(
                value = uiState.make,
                onValueChange = viewModel::onMakeChanged,
                label = null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = MileLogShapes.sm,
                colors = vehicleFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            VehicleFieldLabel(text = stringResource(R.string.vehicle_field_model_label))
            OutlinedTextField(
                value = uiState.model,
                onValueChange = viewModel::onModelChanged,
                label = null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = MileLogShapes.sm,
                colors = vehicleFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            VehicleFieldLabel(text = stringResource(R.string.vehicle_field_registration_label))
            OutlinedTextField(
                value = uiState.registrationNumber,
                onValueChange = viewModel::onRegistrationChanged,
                label = null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = MileLogShapes.sm,
                colors = vehicleFieldColors(),
                supportingText = { Text(stringResource(R.string.vehicle_field_registration_note)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            VehicleFieldLabel(text = stringResource(R.string.vehicle_field_fuel_label))
            SegmentedChoice(
                options = FuelCategory.entries,
                selected = uiState.fuelType,
                label = { stringResource(it.labelRes) },
                onSelect = viewModel::onFuelTypeChanged
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            VehicleFieldLabel(text = stringResource(R.string.vehicle_field_distance_unit_label))
            SegmentedChoice(
                options = DistanceUnit.entries,
                selected = uiState.distanceUnit,
                label = { stringResource(it.labelRes) },
                onSelect = viewModel::onDistanceUnitChanged
            )
            Text(
                text = stringResource(R.string.vehicle_field_distance_unit_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = spacing.sm)
            )
        }

        uiState.saveError?.let { error ->
            Text(
                text = stringResource(error.messageRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(spacing.sm))

        Button(
            onClick = viewModel::save,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = MileLogShapes.sm
        ) {
            Text(
                text = stringResource(
                    if (uiState.isEditMode) {
                        R.string.vehicle_action_save_changes
                    } else {
                        R.string.vehicle_action_save
                    }
                ),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun vehicleFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedBorderColor = MaterialTheme.colorScheme.primary
)

@Composable
private fun VehicleFieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MicroLabelStyle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm)
    )
}

@Composable
private fun VehicleLoadingBlock() {
    val label = stringResource(R.string.vehicle_loading)
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
private fun VehicleLoadErrorBlock(messageRes: Int, onRetry: () -> Unit) {
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
