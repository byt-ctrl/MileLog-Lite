package com.example.myapplication.ui.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.ui.components.InstrumentBar
import com.example.myapplication.ui.theme.MicroLabelStyle
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.theme.MileLogWindow
import com.example.myapplication.ui.theme.spacing
import com.example.myapplication.ui.theme.touchTargetMinHeight

/**
 * Charts/Insights screen hosting the mileage trend line chart and the
 * monthly fuel spend bar chart. Charts refresh automatically as entries
 * are added, edited, or deleted.
 *
 * The empty-state threshold is owned here: charts only render when both
 * the mileage series (≥2 mileage-bearing fill-ups) and the monthly spend
 * series (≥1 entry) are present. Each chart composable is responsible for
 * its own "not enough data" message if it can't fill a view; the screen
 * level threshold prevents the conflicting thresholds previously found in
 * the audit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    onAddEntry: () -> Unit = {},
    viewModel: ChartsViewModel = viewModel(factory = ChartsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = MaterialTheme.spacing

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            InstrumentBar(title = stringResource(R.string.charts_title))
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                val loadingLabel = stringResource(R.string.charts_loading)
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
                            text = stringResource(uiState.errorMessage!!.messageRes),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(spacing.lg))
                        Button(
                            onClick = viewModel::retry,
                            shape = MileLogShapes.sm,
                            modifier = Modifier.touchTargetMinHeight()
                        ) {
                            Text(stringResource(R.string.action_retry))
                        }
                    }
                }
            }

            uiState.entryCount < 2 -> {
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
                            text = stringResource(R.string.charts_empty_title),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(spacing.sm))
                        Text(
                            text = stringResource(R.string.charts_empty_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(spacing.xl))
                        Button(
                            onClick = onAddEntry,
                            shape = MileLogShapes.sm,
                            modifier = Modifier.touchTargetMinHeight()
                        ) {
                            Text(stringResource(R.string.dashboard_empty_cta))
                        }
                    }
                }
            }

            else -> {
                ChartsContent(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

/**
 * The two plots, side by side once the window can hold two readable plots and
 * stacked below that. Width is capped for the same reason the ledger is: a
 * five-point line stretched across a tablet reads worse, not better.
 */
@Composable
private fun ChartsContent(
    uiState: ChartsUiState,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val hasMileage = uiState.fillups.count { it.mileageKmPerL != null } >= 2

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val sideBySide = maxWidth >= MileLogWindow.wide

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            uiState.vehicle?.let { vehicle ->
                Text(
                    text = stringResource(
                        R.string.vehicle_active_note,
                        vehicle.name,
                        stringResource(FuelCategory.fromDisplayName(vehicle.fuelType).labelRes)
                    ),
                    style = MicroLabelStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .widthIn(max = MileLogWindow.contentMaxWidth)
                        .fillMaxWidth()
                        .padding(start = spacing.lg, end = spacing.lg, top = spacing.lg)
                )
            }

            if (sideBySide) {
                Row(
                    modifier = Modifier
                        .widthIn(max = MileLogWindow.contentMaxWidth)
                        .fillMaxWidth()
                        .padding(spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(spacing.lg)
                ) {
                    if (hasMileage) {
                        MileageTrendChart(
                            fillups = uiState.fillups,
                            categorySeries = uiState.categoryMileageSeries,
                            distanceUnit = uiState.distanceUnit,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    MonthlySpendChart(
                        spends = uiState.monthlySpends,
                        categorySpends = uiState.categoryMonthlySpends,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .widthIn(max = MileLogWindow.contentMaxWidth)
                        .fillMaxWidth()
                        .padding(spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.lg)
                ) {
                    if (hasMileage) {
                        MileageTrendChart(
                            fillups = uiState.fillups,
                            categorySeries = uiState.categoryMileageSeries,
                            distanceUnit = uiState.distanceUnit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    MonthlySpendChart(
                        spends = uiState.monthlySpends,
                        categorySpends = uiState.categoryMonthlySpends,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}