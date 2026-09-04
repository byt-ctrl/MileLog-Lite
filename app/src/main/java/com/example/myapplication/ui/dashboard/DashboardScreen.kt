package com.example.myapplication.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MileLogElevation
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.components.MileLogTopAppBar
import com.example.myapplication.ui.theme.level1Shadow
import com.example.myapplication.ui.theme.level2Shadow
import com.example.myapplication.ui.theme.spacing
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddEntry: () -> Unit,
    onViewHistory: () -> Unit,
    onViewCharts: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val spacing = MaterialTheme.spacing

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val odometerNumberFormatter = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    Scaffold(
        topBar = {
            MileLogTopAppBar()
        },
        floatingActionButton = {
            // Always available except during load/error — quick-add is the
            // primary task and stays predictable even on an empty state.
            if (!uiState.isLoading && uiState.errorMessage == null) {
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
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.dashboard_fab_add_entry)
                    )
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
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
        } else if (uiState.errorMessage != null) {
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
                        text = stringResource(uiState.errorMessage!!.messageRes),
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
        } else if (uiState.entryCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(spacing.xxl)
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_empty_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(spacing.sm))
                    Text(
                        text = stringResource(R.string.dashboard_empty_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(spacing.xl))
                    Button(onClick = onAddEntry) {
                        Text(stringResource(R.string.dashboard_empty_cta))
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.lg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(spacing.lg)
                ) {
                    StatCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        title = stringResource(R.string.dashboard_stat_latest_odometer_title),
                        value = uiState.latestOdometer?.let {
                            stringResource(
                                R.string.dashboard_stat_latest_odometer_unit,
                                odometerNumberFormatter.format(it)
                            )
                        } ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
                        subtitle = uiState.latestFuelCategory?.let {
                            stringResource(
                                R.string.dashboard_stat_latest_odometer_subtitle,
                                stringResource(it.labelRes)
                            )
                        }
                    )
                    StatCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        title = stringResource(R.string.dashboard_stat_total_cost_title),
                        value = currencyFormatter.format(uiState.totalCost),
                        subtitle = stringResource(
                            R.string.dashboard_stat_total_cost_subtitle,
                            uiState.entryCount
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(spacing.lg)
                ) {
                    StatCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        title = stringResource(R.string.dashboard_stat_avg_mileage_title),
                        value = uiState.averageMileage?.let {
                            stringResource(R.string.dashboard_stat_avg_mileage_value, it)
                        } ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
                        subtitle = stringResource(
                            R.string.dashboard_stat_avg_mileage_subtitle,
                            uiState.totalDistance
                        )
                    )
                    StatCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        title = stringResource(R.string.dashboard_stat_cost_per_km_title),
                        value = uiState.costPerKm?.let {
                            currencyFormatter.format(it)
                        } ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
                        subtitle = stringResource(
                            R.string.dashboard_stat_cost_per_km_subtitle,
                            uiState.totalDistance
                        )
                    )
                }

                Spacer(modifier = Modifier.height(spacing.sm))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(spacing.lg)
                ) {
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .level1Shadow(MileLogShapes.md),
                        onClick = onViewHistory,
                        shape = MileLogShapes.md,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = MileLogElevation.level1
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.lg),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.dashboard_nav_history_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Text(
                                text = stringResource(
                                    R.string.dashboard_nav_history_subtitle,
                                    uiState.entryCount
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .level1Shadow(MileLogShapes.md),
                        onClick = onViewCharts,
                        shape = MileLogShapes.md,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = MileLogElevation.level1
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.lg),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.dashboard_nav_charts_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Text(
                                text = stringResource(R.string.dashboard_nav_charts_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val spacing = MaterialTheme.spacing
    ElevatedCard(
        modifier = modifier.level1Shadow(MileLogShapes.md),
        shape = MileLogShapes.md,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = MileLogElevation.level1
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(spacing.sm))
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(spacing.sm))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}