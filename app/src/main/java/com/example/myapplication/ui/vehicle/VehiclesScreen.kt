package com.example.myapplication.ui.vehicle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.data.local.Vehicle
import com.example.myapplication.ui.components.InstrumentBand
import com.example.myapplication.ui.components.InstrumentBar
import com.example.myapplication.ui.components.LedgerPanel
import com.example.myapplication.ui.components.LogbookContent
import com.example.myapplication.ui.components.ReadoutItem
import com.example.myapplication.ui.components.ReadoutStrip
import com.example.myapplication.ui.components.SectionHeader
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.theme.ledger
import com.example.myapplication.ui.theme.spacing

/**
 * Vehicles.
 *
 * The garage is a primary destination, not a settings detail: which vehicle is
 * selected decides where every fill-up lands, so it has to be reachable in one
 * tap rather than three. The instrument states the current selection, the
 * ledger below lists the fleet, and add, edit, select and delete all act on the
 * same repository the rest of the app reads.
 */
@Composable
fun VehiclesScreen(
    onAddVehicle: () -> Unit,
    onEditVehicle: (Long) -> Unit,
    viewModel: VehiclesViewModel = viewModel(factory = VehiclesViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val spacing = MaterialTheme.spacing
    var vehiclePendingDelete by remember { mutableStateOf<Vehicle?>(null) }

    // Copy is resolved in the composition so a locale change recomposes it;
    // the effect only decides what to do with the outcome.
    val messageText = uiState.message?.let { stringResource(it.messageRes) }
    LaunchedEffect(messageText) {
        val text = messageText ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        viewModel.consumeMessage()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { InstrumentBar(title = stringResource(R.string.vehicles_title)) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            VehiclesBinnacle(uiState = uiState)

            LogbookContent(
                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xl)
            ) {
                SectionHeader(title = stringResource(R.string.vehicles_section_title))
                Spacer(Modifier.height(spacing.md))
                LedgerPanel {
                    if (uiState.vehicles.isEmpty()) {
                        Text(
                            text = stringResource(R.string.vehicles_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(spacing.lg)
                        )
                    } else {
                        // The rows are one radio set. Grouped, a screen reader
                        // announces the choice as a set of vehicles rather than
                        // as a run of unrelated radio buttons. The Add row below
                        // stays outside it: it is an action, not an option.
                        Column(modifier = Modifier.selectableGroup()) {
                            uiState.vehicles.forEachIndexed { index, vehicle ->
                                if (index > 0) {
                                    HorizontalDivider(
                                        color = MaterialTheme.ledger.rule,
                                        thickness = 1.dp
                                    )
                                }
                                VehicleRow(
                                    vehicle = vehicle,
                                    isActive = vehicle.id == uiState.activeVehicle?.id,
                                    onSelect = { viewModel.setActiveVehicle(vehicle.id) },
                                    onEdit = { onEditVehicle(vehicle.id) },
                                    onDelete = { vehiclePendingDelete = vehicle }
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        color = MaterialTheme.ledger.rule,
                        thickness = 1.dp
                    )
                    AddVehicleRow(onClick = onAddVehicle)
                }
            }
        }
    }

    vehiclePendingDelete?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { vehiclePendingDelete = null },
            shape = MileLogShapes.md,
            title = { Text(stringResource(R.string.vehicles_delete_dialog_title)) },
            text = {
                Text(stringResource(R.string.vehicles_delete_dialog_body, vehicle.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteVehicle(vehicle)
                        vehiclePendingDelete = null
                    },
                    modifier = Modifier.heightIn(min = spacing.touchTarget)
                ) {
                    Text(
                        text = stringResource(R.string.vehicles_delete_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { vehiclePendingDelete = null },
                    modifier = Modifier.heightIn(min = spacing.touchTarget)
                ) {
                    Text(stringResource(R.string.vehicles_delete_cancel))
                }
            }
        )
    }
}

/**
 * The configuration readout. The first thing on the screen is which vehicle
 * every fill-up is written to, plus how many are on the device.
 */
@Composable
private fun VehiclesBinnacle(uiState: VehiclesUiState) {
    val ledger = MaterialTheme.ledger
    val spacing = MaterialTheme.spacing

    InstrumentBand {
        Text(
            text = stringResource(R.string.vehicles_title),
            style = MaterialTheme.typography.headlineMedium,
            color = ledger.chromeText
        )
        Spacer(Modifier.height(spacing.xs))
        Text(
            text = stringResource(R.string.vehicles_binnacle_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = ledger.chromeTextMuted
        )
        Spacer(Modifier.height(spacing.lg))
        ReadoutStrip(
            items = listOf(
                ReadoutItem(
                    label = stringResource(R.string.vehicles_readout_active),
                    value = uiState.activeVehicle?.name,
                    note = stringResource(R.string.vehicles_readout_active_note)
                ),
                ReadoutItem(
                    label = stringResource(R.string.vehicles_readout_count),
                    value = uiState.vehicles.size.toString(),
                    note = stringResource(R.string.vehicles_readout_count_note)
                )
            ),
            compact = true
        )
    }
}

/**
 * One vehicle in the list. The leading control selects which vehicle every
 * screen logs against; edit and delete act on the row itself.
 */
@Composable
private fun VehicleRow(
    vehicle: Vehicle,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.colorScheme
    val category = FuelCategory.fromDisplayName(vehicle.fuelType)
    val summary = listOf(vehicle.make, vehicle.model)
        .filter { it.isNotBlank() }
        .joinToString("  ")
        .ifBlank { stringResource(category.labelRes) }
    val note = "$summary  ·  ${stringResource(category.labelRes)}"
    val selectA11y = stringResource(R.string.vehicles_select_a11y, vehicle.name)
    val editA11y = stringResource(R.string.vehicles_edit_a11y, vehicle.name)
    val deleteA11y = stringResource(R.string.vehicles_delete_a11y, vehicle.name)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .selectable(
                selected = isActive,
                role = Role.RadioButton,
                onClick = onSelect
            )
            .semantics { contentDescription = selectA11y }
            .padding(start = spacing.lg, end = spacing.sm, top = spacing.sm, bottom = spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isActive) {
                Icons.Rounded.RadioButtonChecked
            } else {
                Icons.Rounded.RadioButtonUnchecked
            },
            contentDescription = null,
            tint = if (isActive) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = spacing.md)
        ) {
            Text(
                text = vehicle.name,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onEdit,
            modifier = Modifier.semantics { contentDescription = editA11y }
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.semantics { contentDescription = deleteA11y }
        ) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = null,
                tint = colors.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** The row that opens the add-vehicle form. */
@Composable
private fun AddVehicleRow(onClick: () -> Unit) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 60.dp)
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.vehicles_add_row),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.vehicles_add_note),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier
                .padding(start = spacing.sm)
                .size(20.dp)
        )
    }
}
