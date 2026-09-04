package com.example.myapplication.ui.history

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.data.local.FuelEntry
import com.example.myapplication.ui.theme.MileLogElevation
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.theme.level1Shadow
import com.example.myapplication.ui.theme.level2Shadow
import com.example.myapplication.ui.theme.spacing
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * History screen listing all fuel entries, most recent first.
 *
 * Tapping an entry opens it in edit mode; the FAB opens a new entry form.
 * The top-bar download icon exports all entries to a CSV file via the
 * system document-creation picker.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onEditEntry: (Long) -> Unit,
    onAddEntry: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val entries = uiState.entries
    var entryPendingDelete by remember { mutableStateOf<FuelEntry?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val context = LocalContext.current
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

    // When the ViewModel has prepared the CSV, launch the picker so the user
    // chooses where to save it.
    LaunchedEffect(uiState.exportReady) {
        val csv = uiState.exportReady ?: return@LaunchedEffect
        exportLauncher.launch("milelog_fuel_entries.csv")
        viewModel.clearExportReady()
    }

    // Surface export outcomes (success/failure) via a snackbar.
    LaunchedEffect(uiState.exportMessage) {
        val key = uiState.exportMessage ?: return@LaunchedEffect
        val text = when (key) {
            HistoryMessage.EXPORT_SUCCESS ->
                context.getString(key.messageRes, uiState.exportMessageCount)
            HistoryMessage.EXPORT_WRITE_FAILED -> {
                val detail = uiState.exportMessageDetail.orEmpty()
                context.getString(key.messageRes, detail)
            }
            else -> context.getString(key.messageRes)
        }
        snackbarHostState.showSnackbar(text)
        viewModel.consumeExportMessage()
    }

    // Undo affordance for the most recent delete. The snackbar carries a
    // localized label and an Undo action that re-inserts the entry.
    LaunchedEffect(uiState.lastDeleted) {
        val deleted = uiState.lastDeleted ?: return@LaunchedEffect
        val undoLabel = context.getString(R.string.action_undo)
        val result = snackbarHostState.showSnackbar(
            message = context.getString(R.string.history_entry_deleted),
            actionLabel = undoLabel,
            withDismissAction = true,
            duration = androidx.compose.material3.SnackbarDuration.Short
        )
        when (result) {
            androidx.compose.material3.SnackbarResult.ActionPerformed ->
                viewModel.undoDelete()
            androidx.compose.material3.SnackbarResult.Dismissed ->
                viewModel.consumeDeleted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_navigate_back)
                        )
                    }
                },
                actions = {
                    if (!uiState.isLoading && uiState.errorMessage == null) {
                        IconButton(onClick = viewModel::exportEntries) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = stringResource(R.string.history_export_action)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEntry,
                modifier = Modifier.level2Shadow(FloatingActionButtonDefaults.shape),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = MileLogElevation.level2,
                    pressedElevation = MileLogElevation.level2,
                    focusedElevation = MileLogElevation.level2,
                    hoveredElevation = MileLogElevation.level2
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.history_fab_add_entry)
                )
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                val loadingLabel = stringResource(R.string.loading_generic)
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

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
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
                        Spacer(modifier = Modifier.height(spacing.lg))
                        Button(onClick = viewModel::retry) {
                            Text(stringResource(R.string.action_retry))
                        }
                    }
                }
            }

            entries.isEmpty() -> {
                val filterCategory = uiState.selectedCategory
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (filterCategory != null) {
                                stringResource(R.string.history_empty_title_filter, stringResource(filterCategory.labelRes))
                            } else {
                                stringResource(R.string.history_empty_title_all)
                            },
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(spacing.sm))
                        Text(
                            text = if (filterCategory != null) {
                                stringResource(R.string.history_empty_subtitle_filter, stringResource(filterCategory.labelRes))
                            } else {
                                stringResource(R.string.history_empty_subtitle_all)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(spacing.xl))
                        if (filterCategory != null && uiState.totalEntryCount > 0) {
                            Button(onClick = { viewModel.setCategoryFilter(null) }) {
                                Text(stringResource(R.string.history_empty_cta_clear_filter))
                            }
                        } else {
                            Button(onClick = onAddEntry) {
                                Text(stringResource(R.string.history_empty_cta_add))
                            }
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    CategoryFilterChips(
                        selected = uiState.selectedCategory,
                        onSelected = viewModel::setCategoryFilter
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(spacing.lg)
                    ) {
                        items(entries, key = { it.id }) { entry ->
                            FuelEntryCard(
                                entry = entry,
                                currencyFormatter = currencyFormatter,
                                onClick = { onEditEntry(entry.id) },
                                onDeleteClick = { entryPendingDelete = entry }
                            )
                        }
                    }
                }
            }
        }
    }

    entryPendingDelete?.let { entry ->
        val deleteDateFormatter = remember(entry.date) {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        }
        AlertDialog(
            onDismissRequest = { entryPendingDelete = null },
            shape = MileLogShapes.md,
            title = { Text(stringResource(R.string.history_delete_dialog_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.history_delete_dialog_body,
                        deleteDateFormatter.format(Date(entry.date))
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntry(entry)
                        entryPendingDelete = null
                    }
                ) {
                    Text(
                        stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { entryPendingDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

/**
 * Horizontally-scrolling row of [FilterChip]s: an "All" chip plus one per
 * [FuelCategory]. Tapping the active chip clears the filter; tapping another
 * chip switches the filter. Selected chips show a leading check icon per
 * Material 3 idiom.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterChips(
    selected: FuelCategory?,
    onSelected: (FuelCategory?) -> Unit
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = spacing.lg, end = spacing.lg, top = spacing.md, bottom = spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        val allFilterLabel = stringResource(R.string.history_filter_all)
        val allFilterA11y = stringResource(R.string.history_filter_all_a11y)
        FilterChip(
            selected = selected == null,
            onClick = { onSelected(null) },
            label = {
                Text(
                    allFilterLabel,
                    modifier = Modifier.heightIn(min = spacing.touchTarget)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            },
            leadingIcon = if (selected == null) {
                {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else null,
            modifier = Modifier
                .heightIn(min = spacing.touchTarget)
                .semantics {
                    contentDescription = allFilterA11y
                }
        )
        FuelCategory.entries.forEach { category ->
            val isSelected = selected == category
            val categoryLabel = stringResource(category.labelRes)
            val categoryA11y = stringResource(
                R.string.history_filter_category_a11y,
                categoryLabel
            )
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(if (isSelected) null else category) },
                label = {
                    Text(
                        categoryLabel,
                        modifier = Modifier.heightIn(min = spacing.touchTarget)
                            .wrapContentHeight(Alignment.CenterVertically)
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null,
                modifier = Modifier
                    .heightIn(min = spacing.touchTarget)
                    .semantics {
                        contentDescription = categoryA11y
                    }
            )
        }
    }
}

/**
 * Card rendering a single fuel entry with its details.
 *
 * The card body (date/odometer/fuel/cost) is the clickable target for edit;
 * the delete IconButton sits beside it as an independent control. The two
 * never share a clickable parent, which keeps TalkBack's double-tap model
 * honest — one focus, one action.
 */
@Composable
private fun FuelEntryCard(
    entry: FuelEntry,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val dateFormatter = remember(entry.date) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }
    val formattedDate = remember(entry.date) { dateFormatter.format(Date(entry.date)) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .level1Shadow(MileLogShapes.md),
        shape = MileLogShapes.md,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = MileLogElevation.level1
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
                    .padding(start = spacing.lg, top = spacing.lg, bottom = spacing.lg, end = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.history_row_odometer, entry.odometer),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(
                        R.string.history_row_fuel_cost,
                        entry.liters,
                        currencyFormatter.format(entry.cost)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(
                        R.string.history_card_delete,
                        formattedDate
                    )
                )
            }
        }
    }
}