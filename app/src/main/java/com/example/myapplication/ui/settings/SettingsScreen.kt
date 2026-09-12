package com.example.myapplication.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import com.example.myapplication.domain.demo.DemoDataGenerator
import com.example.myapplication.ui.components.InstrumentBand
import com.example.myapplication.ui.components.InstrumentBar
import com.example.myapplication.ui.components.LedgerPanel
import com.example.myapplication.ui.components.ReadoutItem
import com.example.myapplication.ui.components.ReadoutStrip
import com.example.myapplication.ui.components.SectionHeader
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.theme.ledger
import com.example.myapplication.ui.theme.spacing
import java.text.NumberFormat
import java.util.Locale

/**
 * Settings.
 *
 * A Configure surface. It opens with the current configuration stated as an
 * instrument readout rather than a decorative header, then groups the decisions
 * that exist. Every row here does something real: export writes a CSV, clearing
 * the log can be undone, and the rest states facts about the app. Nothing is
 * offered that the product does not have.
 */
@Composable
fun SettingsScreen(
    onAddVehicle: () -> Unit,
    onEditVehicle: (Long) -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val spacing = MaterialTheme.spacing
    val integerFormatter = remember { NumberFormat.getIntegerInstance(Locale.getDefault()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSeedDialog by remember { mutableStateOf(false) }
    var vehiclePendingDelete by remember { mutableStateOf<Vehicle?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.writeExportedCsv(uri)
        } else {
            viewModel.clearExportReady()
        }
    }

    // Hold on to the prepared CSV until the picker returns: the write reads it
    // back from the ViewModel, so clearing here would leave nothing to save.
    // It is cleared by writeExportedCsv() once written, or on cancel below.
    LaunchedEffect(uiState.exportReady) {
        if (uiState.exportReady == null) return@LaunchedEffect
        exportLauncher.launch("milelog_fuel_entries.csv")
    }

    // Copy is resolved in the composition so a locale change recomposes it;
    // the effect only decides what to do with the result.
    val messageKey = uiState.message
    val messageText: String? = messageKey?.let { key ->
        when (key) {
            SettingsMessage.EXPORT_SUCCESS -> stringResource(key.messageRes, uiState.messageCount)
            SettingsMessage.SEEDED -> stringResource(
                key.messageRes,
                uiState.messageCount,
                DemoDataGenerator.profiles.size
            )
            SettingsMessage.EXPORT_FAILED ->
                stringResource(key.messageRes, uiState.messageDetail.orEmpty())
            else -> stringResource(key.messageRes)
        }
    }
    val undoLabel = stringResource(R.string.action_undo)

    LaunchedEffect(messageText) {
        val text = messageText ?: return@LaunchedEffect
        val result = if (messageKey == SettingsMessage.CLEARED) {
            snackbarHostState.showSnackbar(
                message = text,
                actionLabel = undoLabel,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
        } else {
            snackbarHostState.showSnackbar(text)
            null
        }
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoClear()
        }
        viewModel.consumeMessage()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { InstrumentBar(title = stringResource(R.string.settings_title)) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsBinnacle(uiState = uiState, integerFormatter = integerFormatter)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.lg, vertical = spacing.xl),
                verticalArrangement = Arrangement.spacedBy(spacing.xxl)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(
                        title = stringResource(R.string.settings_section_vehicle),
                        note = stringResource(R.string.settings_vehicle_active_note)
                    )
                    Spacer(Modifier.height(spacing.md))
                    LedgerPanel {
                        if (uiState.vehicles.isEmpty()) {
                            SettingsRow(title = stringResource(R.string.settings_vehicle_empty))
                        } else {
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
                            HorizontalDivider(
                                color = MaterialTheme.ledger.rule,
                                thickness = 1.dp
                            )
                        }
                        SettingsRow(
                            title = stringResource(R.string.settings_vehicle_add_row),
                            note = stringResource(R.string.settings_vehicle_add_note),
                            onClick = onAddVehicle
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(title = stringResource(R.string.settings_section_appearance))
                    Spacer(Modifier.height(spacing.md))
                    LedgerPanel {
                        SettingsRow(
                            title = stringResource(R.string.settings_appearance_row),
                            note = stringResource(R.string.settings_appearance_note),
                            value = stringResource(R.string.settings_appearance_value)
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(title = stringResource(R.string.settings_section_data))
                    Spacer(Modifier.height(spacing.md))
                    LedgerPanel {
                        SettingsRow(
                            title = stringResource(R.string.settings_seed_row),
                            note = stringResource(R.string.settings_seed_row_note),
                            onClick = { showSeedDialog = true }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.ledger.rule,
                            thickness = 1.dp
                        )
                        SettingsRow(
                            title = stringResource(R.string.settings_export_row),
                            note = stringResource(R.string.settings_export_row_note),
                            onClick = viewModel::exportEntries
                        )
                        HorizontalDivider(
                            color = MaterialTheme.ledger.rule,
                            thickness = 1.dp
                        )
                        SettingsRow(
                            title = stringResource(R.string.settings_delete_row),
                            note = stringResource(R.string.settings_delete_row_note),
                            danger = true,
                            onClick = { showDeleteDialog = true }
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(title = stringResource(R.string.settings_section_about))
                    Spacer(Modifier.height(spacing.md))
                    LedgerPanel {
                        SettingsRow(
                            title = stringResource(R.string.app_name),
                            value = stringResource(R.string.settings_about_version_value)
                        )
                        HorizontalDivider(
                            color = MaterialTheme.ledger.rule,
                            thickness = 1.dp
                        )
                        SettingsRow(
                            title = stringResource(R.string.settings_about_storage),
                            value = stringResource(R.string.settings_about_storage_value)
                        )
                        HorizontalDivider(
                            color = MaterialTheme.ledger.rule,
                            thickness = 1.dp
                        )
                        SettingsRow(
                            title = stringResource(R.string.settings_about_network),
                            value = stringResource(R.string.settings_about_network_value)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = MileLogShapes.md,
            title = { Text(stringResource(R.string.settings_delete_dialog_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.settings_delete_dialog_body,
                        uiState.entryCount
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllEntries()
                        showDeleteDialog = false
                    },
                    modifier = Modifier.heightIn(min = spacing.touchTarget)
                ) {
                    Text(
                        text = stringResource(R.string.settings_delete_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.heightIn(min = spacing.touchTarget)
                ) {
                    Text(stringResource(R.string.settings_delete_cancel))
                }
            }
        )
    }

    if (showSeedDialog) {
        AlertDialog(
            onDismissRequest = { showSeedDialog = false },
            shape = MileLogShapes.md,
            title = { Text(stringResource(R.string.settings_seed_dialog_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.settings_seed_dialog_body,
                        DemoDataGenerator.profiles.size,
                        DemoDataGenerator.DEFAULT_ENTRY_COUNT
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.seedDemoData()
                        showSeedDialog = false
                    },
                    modifier = Modifier.heightIn(min = spacing.touchTarget)
                ) {
                    Text(stringResource(R.string.settings_seed_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSeedDialog = false },
                    modifier = Modifier.heightIn(min = spacing.touchTarget)
                ) {
                    Text(stringResource(R.string.settings_seed_cancel))
                }
            }
        )
    }

    vehiclePendingDelete?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { vehiclePendingDelete = null },
            shape = MileLogShapes.md,
            title = { Text(stringResource(R.string.settings_vehicle_delete_dialog_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.settings_vehicle_delete_dialog_body,
                        vehicle.name
                    )
                )
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
                        text = stringResource(R.string.settings_vehicle_delete_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { vehiclePendingDelete = null },
                    modifier = Modifier.heightIn(min = spacing.touchTarget)
                ) {
                    Text(stringResource(R.string.settings_vehicle_delete_cancel))
                }
            }
        )
    }
}

/**
 * One vehicle in the Vehicle group. The leading control selects which vehicle
 * every screen logs against; edit and delete act on the row itself.
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
    val editA11y = stringResource(R.string.settings_vehicle_edit_a11y, vehicle.name)
    val deleteA11y = stringResource(R.string.settings_vehicle_delete_a11y, vehicle.name)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .selectable(
                selected = isActive,
                role = Role.RadioButton,
                onClick = onSelect
            )
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

@Composable
private fun SettingsBinnacle(
    uiState: SettingsUiState,
    integerFormatter: NumberFormat
) {
    val ledger = MaterialTheme.ledger
    val spacing = MaterialTheme.spacing

    InstrumentBand {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            color = ledger.chromeText
        )
        Spacer(Modifier.height(spacing.xs))
        Text(
            text = stringResource(R.string.settings_binnacle_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = ledger.chromeTextMuted
        )
        Spacer(Modifier.height(spacing.lg))
        ReadoutStrip(
            items = listOf(
                ReadoutItem(
                    label = stringResource(R.string.settings_readout_entries),
                    value = integerFormatter.format(uiState.entryCount),
                    note = stringResource(R.string.settings_readout_entries_note)
                ),
                ReadoutItem(
                    label = stringResource(R.string.settings_readout_distance),
                    value = stringResource(
                        R.string.dashboard_stat_latest_odometer_unit,
                        integerFormatter.format(uiState.totalDistance)
                    ),
                    note = stringResource(R.string.settings_readout_distance_note)
                ),
                ReadoutItem(
                    label = stringResource(R.string.settings_readout_storage),
                    value = stringResource(R.string.settings_about_storage_value),
                    note = stringResource(R.string.settings_readout_storage_note)
                )
            ),
            compact = true
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    note: String? = null,
    value: String? = null,
    danger: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.colorScheme
    val modifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (danger) colors.error else colors.onSurface
            )
            note?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
        value?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = spacing.md)
            )
        }
        if (onClick != null) {
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
}
