package com.example.myapplication.ui.history

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.data.local.FuelEntry
import com.example.myapplication.ui.components.InstrumentBar
import com.example.myapplication.ui.components.LedgerColumnLabels
import com.example.myapplication.ui.components.LedgerColumnWeights
import com.example.myapplication.ui.components.LedgerHeaderRow
import com.example.myapplication.ui.components.LedgerRow
import com.example.myapplication.ui.components.formatDistanceWithUnit
import com.example.myapplication.ui.components.formatMileageWithUnit
import com.example.myapplication.ui.theme.MicroLabelStyle
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.theme.MileLogWindow
import com.example.myapplication.ui.theme.ledger
import com.example.myapplication.ui.theme.spacing
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fuel history.
 *
 * A ruled ledger rather than a stack of cards: one row per fill-up, figures
 * right-aligned so the columns compare straight down the page, and the delete
 * action as an independent control beside the edit target rather than nested
 * inside it.
 */
@Composable
fun HistoryScreen(
    onEditEntry: (Long) -> Unit,
    onAddEntry: () -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val entries = uiState.entries
    val distanceUnit = uiState.distanceUnit
    var entryPendingDelete by remember { mutableStateOf<FuelEntry?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")) }
    val dateFormatter = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }
    val spacing = MaterialTheme.spacing

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

    // Resolve copy in the composition so a locale change recomposes it, then
    // act on it from the effect. Reaching for LocalContext inside a
    // LaunchedEffect would freeze the string at its first value.
    val exportMessageText: String? = uiState.exportMessage?.let { key ->
        when (key) {
            HistoryMessage.EXPORT_SUCCESS -> stringResource(key.messageRes, uiState.exportMessageCount)
            HistoryMessage.EXPORT_WRITE_FAILED ->
                stringResource(key.messageRes, uiState.exportMessageDetail.orEmpty())
            else -> stringResource(key.messageRes)
        }
    }
    val entryDeletedText = stringResource(R.string.history_entry_deleted)
    val undoLabel = stringResource(R.string.action_undo)

    LaunchedEffect(exportMessageText) {
        val text = exportMessageText ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        viewModel.consumeExportMessage()
    }

    LaunchedEffect(uiState.lastDeleted) {
        if (uiState.lastDeleted == null) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = entryDeletedText,
            actionLabel = undoLabel,
            withDismissAction = true,
            duration = SnackbarDuration.Short
        )
        when (result) {
            SnackbarResult.ActionPerformed -> viewModel.undoDelete()
            SnackbarResult.Dismissed -> viewModel.consumeDeleted()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            InstrumentBar(
                title = stringResource(R.string.history_title),
                actions = {
                    if (!uiState.isLoading && uiState.errorMessage == null) {
                        IconButton(onClick = viewModel::exportEntries) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = stringResource(R.string.history_export_action),
                                tint = MaterialTheme.ledger.chromeText
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Captured once: inside the nested Column scope below, maxWidth is
            // no longer reachable as an implicit receiver.
            val availableWidth = maxWidth
            val wide = availableWidth >= MileLogWindow.medium

            when {
                uiState.isLoading -> LoadingBlock()

                uiState.errorMessage != null -> ErrorBlock(onRetry = viewModel::retry)

                entries.isEmpty() -> EmptyBlock(
                    filteredCategory = uiState.selectedCategory,
                    hasAnyEntries = uiState.totalEntryCount > 0,
                    onAddEntry = onAddEntry,
                    onClearFilter = { viewModel.setCategoryFilter(null) }
                )

                else -> Column(modifier = Modifier.fillMaxSize()) {
                    // Centre the ledger once the window is wider than a
                    // comfortable reading measure, rather than stretching the
                    // columns out to the screen edges.
                    val sidePadding = if (availableWidth > MileLogWindow.contentMaxWidth) {
                        (availableWidth - MileLogWindow.contentMaxWidth) / 2 + spacing.lg
                    } else {
                        spacing.lg
                    }

                    uiState.vehicle?.let { vehicle ->
                        Text(
                            text = stringResource(
                                R.string.vehicle_active_note,
                                vehicle.name,
                                stringResource(
                                    FuelCategory.fromDisplayName(vehicle.fuelType).labelRes
                                )
                            ),
                            style = MicroLabelStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                start = sidePadding,
                                end = sidePadding,
                                top = spacing.md
                            )
                        )
                    }

                    CategoryFilters(
                        selected = uiState.selectedCategory,
                        onSelected = viewModel::setCategoryFilter,
                        sidePadding = sidePadding
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = sidePadding,
                            end = sidePadding,
                            top = spacing.md,
                            bottom = spacing.xxxl
                        )
                    ) {
                        if (wide) {
                            item {
                                LedgerHeaderRow(
                                    labels = LedgerColumnLabels.map { stringResource(it) },
                                    weights = LedgerColumnWeights,
                                    reserveTrailing = true
                                )
                            }
                        }
                        itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.ledger.rule,
                                    thickness = 1.dp
                                )
                            }
                            val formattedDate = dateFormatter.format(Date(entry.date))
                            LedgerRow(
                                date = formattedDate,
                                odometer = formatDistanceWithUnit(
                                    entry.odometer.toDouble(),
                                    distanceUnit
                                ),
                                liters = if (wide) {
                                    stringResource(R.string.dashboard_ledger_liters_value, entry.liters)
                                } else {
                                    stringResource(R.string.dashboard_ledger_liters_value, entry.liters) +
                                        "  ·  " + stringResource(
                                            FuelCategory.fromDisplayName(entry.fuelCategory).labelRes
                                        )
                                },
                                mileage = uiState.mileageById[entry.id]?.let { value ->
                                    formatMileageWithUnit(value, distanceUnit)
                                },
                                cost = currencyFormatter.format(entry.cost),
                                compact = !wide,
                                onClick = { onEditEntry(entry.id) },
                                trailing = {
                                    IconButton(onClick = { entryPendingDelete = entry }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = stringResource(
                                                R.string.history_card_delete,
                                                formattedDate
                                            ),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    entryPendingDelete?.let { entry ->
        val deleteDate = remember(entry.date) {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(entry.date))
        }
        AlertDialog(
            onDismissRequest = { entryPendingDelete = null },
            shape = MileLogShapes.md,
            title = { Text(stringResource(R.string.history_delete_dialog_title)) },
            text = {
                Text(stringResource(R.string.history_delete_dialog_body, deleteDate))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntry(entry)
                        entryPendingDelete = null
                    },
                    modifier = Modifier.heightIn(min = spacing.touchTarget)
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { entryPendingDelete = null },
                    modifier = Modifier.heightIn(min = spacing.touchTarget)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun CategoryFilters(
    selected: FuelCategory?,
    onSelected: (FuelCategory?) -> Unit,
    sidePadding: Dp
) {
    val spacing = MaterialTheme.spacing
    val allFilterA11y = stringResource(R.string.history_filter_all_a11y)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = sidePadding, vertical = spacing.md),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelected(null) },
            label = { Text(stringResource(R.string.history_filter_all)) },
            shape = MileLogShapes.sm,
            colors = filterChipColors(),
            modifier = Modifier
                .heightIn(min = 44.dp)
                .semantics { contentDescription = allFilterA11y }
        )
        FuelCategory.entries.forEach { category ->
            val isSelected = selected == category
            val label = stringResource(category.labelRes)
            val categoryA11y = stringResource(R.string.history_filter_category_a11y, label)
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(if (isSelected) null else category) },
                label = { Text(label) },
                shape = MileLogShapes.sm,
                colors = filterChipColors(),
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .semantics { contentDescription = categoryA11y }
            )
        }
    }
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surface,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor = MaterialTheme.colorScheme.primary,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
)

@Composable
private fun LoadingBlock() {
    val label = stringResource(R.string.loading_generic)
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
private fun ErrorBlock(onRetry: () -> Unit) {
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
                text = stringResource(R.string.history_error_load),
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

@Composable
private fun EmptyBlock(
    filteredCategory: FuelCategory?,
    hasAnyEntries: Boolean,
    onAddEntry: () -> Unit,
    onClearFilter: () -> Unit
) {
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
            val categoryName = filteredCategory?.let { stringResource(it.labelRes) }
            Text(
                text = if (categoryName != null) {
                    stringResource(R.string.history_empty_title_filter, categoryName)
                } else {
                    stringResource(R.string.history_empty_title_all)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(spacing.sm))
            Text(
                text = if (categoryName != null) {
                    stringResource(R.string.history_empty_subtitle_filter, categoryName)
                } else {
                    stringResource(R.string.history_empty_subtitle_all)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(spacing.xl))
            if (categoryName != null && hasAnyEntries) {
                Button(onClick = onClearFilter, shape = MileLogShapes.sm) {
                    Text(stringResource(R.string.history_empty_cta_clear_filter))
                }
            } else {
                Button(onClick = onAddEntry, shape = MileLogShapes.sm) {
                    Text(stringResource(R.string.history_empty_cta_add))
                }
            }
        }
    }
}
