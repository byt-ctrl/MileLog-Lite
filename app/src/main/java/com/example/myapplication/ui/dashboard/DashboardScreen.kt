package com.example.myapplication.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.domain.calculation.FillupMileage
import com.example.myapplication.domain.conversion.DistanceConverter
import com.example.myapplication.domain.conversion.DistanceUnit
import com.example.myapplication.ui.components.GaugeScaleLabels
import com.example.myapplication.ui.components.InstrumentBand
import com.example.myapplication.ui.components.InstrumentBar
import com.example.myapplication.ui.components.LedgerColumnLabels
import com.example.myapplication.ui.components.LedgerColumnWeights
import com.example.myapplication.ui.components.LedgerHeaderRow
import com.example.myapplication.ui.components.LedgerPanel
import com.example.myapplication.ui.components.LedgerRow
import com.example.myapplication.ui.components.MileageGauge
import com.example.myapplication.ui.components.MileageTrendBars
import com.example.myapplication.ui.components.ReadoutItem
import com.example.myapplication.ui.components.ReadoutStrip
import com.example.myapplication.ui.components.SectionHeader
import com.example.myapplication.ui.components.TrendPoint
import com.example.myapplication.ui.components.distanceLabel
import com.example.myapplication.ui.components.formatDistanceWithUnit
import com.example.myapplication.ui.components.formatMileageWithUnit
import com.example.myapplication.ui.components.formatOne
import com.example.myapplication.ui.components.formatTick
import com.example.myapplication.ui.components.mileageLabel
import com.example.myapplication.ui.components.withUnit
import com.example.myapplication.ui.navigation.LocalShellLayout
import com.example.myapplication.ui.navigation.ShellLayout
import com.example.myapplication.ui.theme.MicroLabelStyle
import com.example.myapplication.ui.theme.MileLogWindow
import com.example.myapplication.ui.theme.ledger
import com.example.myapplication.ui.theme.spacing
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

/** Top of the mileage dial. A car's plausible ceiling, not a data maximum. */
private const val GAUGE_CEILING = 30.0

private const val TREND_FLOOR_CEILING = 20.0

/**
 * Fuel log.
 *
 * A Monitor surface, read in the order a driver checks it: the instrument gives
 * the current reading, then the ledger gives the record, then the trend gives
 * the direction. The instrument is dark in both appearances because it is the
 * machine; only the logbook below it follows the theme.
 */
@Composable
fun DashboardScreen(
    onAddEntry: () -> Unit,
    onViewHistory: () -> Unit,
    onViewCharts: () -> Unit,
    onAddVehicle: () -> Unit,
    onEditEntry: (Long) -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")) }
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }
    val trendDateFormat = remember { SimpleDateFormat("d MMM", Locale.getDefault()) }
    val shellLayout = LocalShellLayout.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Content width decides the table and readout layouts; the shell decides
        // whether this screen owns a top bar. They are different questions.
        val roomyReadouts = maxWidth >= MileLogWindow.medium

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (shellLayout == ShellLayout.Compact) {
                InstrumentBar(title = "", wordmark = true)
            }

            Binnacle(
                uiState = uiState,
                currency = currency,
                roomyReadouts = roomyReadouts
            )

            when {
                uiState.isLoading -> LoadingBlock()

                uiState.errorMessage != null -> ErrorBlock(onRetry = viewModel::retry)

                else -> Content(
                    uiState = uiState,
                    currency = currency,
                    dateFormat = dateFormat,
                    trendDateFormat = trendDateFormat,
                    onAddEntry = onAddEntry,
                    onAddVehicle = onAddVehicle,
                    onViewHistory = onViewHistory,
                    onViewCharts = onViewCharts,
                    onEditEntry = onEditEntry,
                    wideLedger = roomyReadouts
                )
            }
        }
    }
}

@Composable
private fun Binnacle(
    uiState: DashboardUiState,
    currency: NumberFormat,
    roomyReadouts: Boolean
) {
    val ledger = MaterialTheme.ledger
    val spacing = MaterialTheme.spacing
    val unit = uiState.distanceUnit
    val average = uiState.averageMileage
    val latest = uiState.latestMileage
    // The dial keeps its 0..30 km/L shape in either unit, so the ticks, the
    // marker and the printed number always describe the same position.
    val ceiling = DistanceConverter.convertMileage(GAUGE_CEILING, unit)

    InstrumentBand {
        Text(
            text = stringResource(R.string.dashboard_binnacle_title),
            style = MaterialTheme.typography.headlineMedium,
            color = ledger.chromeText
        )
        Spacer(Modifier.height(spacing.xs))
        Text(
            text = uiState.vehicle?.let { vehicle ->
                stringResource(
                    R.string.dashboard_binnacle_subtitle,
                    vehicle.name,
                    stringResource(FuelCategory.fromDisplayName(vehicle.fuelType).labelRes)
                )
            } ?: stringResource(R.string.dashboard_binnacle_subtitle_empty),
            style = MaterialTheme.typography.bodySmall,
            color = ledger.chromeTextMuted
        )

        Spacer(Modifier.height(spacing.xl))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = stringResource(R.string.dashboard_gauge_label).uppercase(),
                style = MicroLabelStyle,
                color = ledger.chromeTextMuted,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.dashboard_gauge_scale_note),
                style = MaterialTheme.typography.labelSmall,
                color = ledger.chromeTextMuted
            )
        }

        Spacer(Modifier.height(spacing.sm))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = average?.let { formatOne(DistanceConverter.convertMileage(it, unit)) }
                    ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
                style = MaterialTheme.typography.displayLarge,
                color = ledger.chromeText
            )
            Text(
                text = unit.mileageLabel(),
                style = MaterialTheme.typography.titleMedium,
                color = ledger.chromeReadout,
                modifier = Modifier.padding(start = spacing.sm, bottom = 6.dp)
            )
        }

        Spacer(Modifier.height(spacing.md))

        MileageGauge(
            value = average?.let { DistanceConverter.convertMileage(it, unit) },
            ceiling = ceiling,
            contentDescription = gaugeA11y(average, latest, ceiling, unit)
        )

        Spacer(Modifier.height(spacing.sm))

        GaugeScaleLabels(ceiling = ceiling)

        Spacer(Modifier.height(spacing.sm))

        Text(
            text = gaugeNote(average, latest, unit),
            style = MaterialTheme.typography.labelSmall,
            color = ledger.chromeTextMuted
        )

        Spacer(Modifier.height(spacing.lg))

        ReadoutStrip(
            items = readouts(uiState, currency),
            compact = !roomyReadouts
        )
    }
}

@Composable
private fun readouts(
    uiState: DashboardUiState,
    currency: NumberFormat
): List<ReadoutItem> {
    val unit = uiState.distanceUnit
    return listOf(
        ReadoutItem(
            label = stringResource(R.string.dashboard_stat_latest_odometer_title),
            value = uiState.latestOdometer?.let { formatDistanceWithUnit(it.toDouble(), unit) }
                ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
            note = stringResource(R.string.dashboard_stat_total_cost_subtitle, uiState.entryCount)
        ),
        ReadoutItem(
            label = stringResource(
                R.string.dashboard_stat_cost_per_distance_title,
                unit.distanceLabel()
            ),
            value = uiState.costPerKm?.let { costPerKm ->
                currency.format(DistanceConverter.convertCostPerDistance(costPerKm, unit))
            } ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
            note = stringResource(
                R.string.dashboard_stat_cost_per_distance_subtitle,
                formatDistanceWithUnit(uiState.totalDistance.toDouble(), unit)
            )
        ),
        ReadoutItem(
            label = stringResource(R.string.dashboard_stat_total_cost_title),
            value = currency.format(uiState.totalCost),
            note = stringResource(R.string.dashboard_stat_total_cost_subtitle, uiState.entryCount)
        )
    )
}

@Composable
private fun gaugeNote(average: Double?, latest: Double?, unit: DistanceUnit): String = when {
    average == null -> stringResource(R.string.dashboard_gauge_note_no_average)
    latest == null -> stringResource(R.string.dashboard_gauge_note_plain)
    else -> {
        val averageInUnit = DistanceConverter.convertMileage(average, unit)
        val latestInUnit = DistanceConverter.convertMileage(latest, unit)
        val latestText = formatMileageWithUnit(latest, unit)
        when {
            abs(latestInUnit - averageInUnit) < 0.05 -> stringResource(R.string.dashboard_gauge_note_level)
            latestInUnit > averageInUnit -> stringResource(
                R.string.dashboard_gauge_note_above,
                latestText,
                formatOne(abs(latestInUnit - averageInUnit))
            )
            else -> stringResource(
                R.string.dashboard_gauge_note_below,
                latestText,
                formatOne(abs(latestInUnit - averageInUnit))
            )
        }
    }
}

@Composable
private fun gaugeA11y(
    average: Double?,
    latest: Double?,
    ceiling: Double,
    unit: DistanceUnit
): String {
    val scale = stringResource(
        R.string.dashboard_gauge_scale_range,
        withUnit(formatTick(ceiling), unit.mileageLabel())
    )
    return when {
        average == null -> stringResource(R.string.dashboard_gauge_a11y_empty, scale)
        latest == null -> stringResource(
            R.string.dashboard_gauge_a11y,
            formatMileageWithUnit(average, unit),
            stringResource(R.string.dashboard_stat_latest_odometer_empty),
            scale
        )
        else -> stringResource(
            R.string.dashboard_gauge_a11y,
            formatMileageWithUnit(average, unit),
            formatMileageWithUnit(latest, unit),
            scale
        )
    }
}

@Composable
private fun Content(
    uiState: DashboardUiState,
    currency: NumberFormat,
    dateFormat: SimpleDateFormat,
    trendDateFormat: SimpleDateFormat,
    onAddEntry: () -> Unit,
    onAddVehicle: () -> Unit,
    onViewHistory: () -> Unit,
    onViewCharts: () -> Unit,
    onEditEntry: (Long) -> Unit,
    wideLedger: Boolean
) {
    val ledger = MaterialTheme.ledger
    val spacing = MaterialTheme.spacing
    val unit = uiState.distanceUnit

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.xxl)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(
                title = stringResource(R.string.dashboard_ledger_title),
                note = stringResource(R.string.dashboard_ledger_note, uiState.entryCount),
                action = {
                    TextButton(onClick = onViewHistory) {
                        Text(stringResource(R.string.dashboard_ledger_action))
                    }
                }
            )
            Spacer(Modifier.height(spacing.md))

            if (uiState.recentFillups.isEmpty()) {
                EmptyLedger(
                    hasVehicle = uiState.vehicle != null,
                    onAddEntry = onAddEntry,
                    onAddVehicle = onAddVehicle
                )
            } else {
                LedgerPanel {
                    // Row captions once the ledger is showing its full column
                    // set; the compact layout labels every figure inline.
                    if (wideLedger) {
                        LedgerHeaderRow(
                            labels = LedgerColumnLabels.map { stringResource(it) },
                            weights = LedgerColumnWeights,
                            reserveTrailing = true,
                            modifier = Modifier.padding(
                                horizontal = spacing.md,
                                vertical = spacing.md
                            )
                        )
                    }
                    uiState.recentFillups.forEachIndexed { index, fillup ->
                        if (index > 0) {
                            HorizontalDivider(color = ledger.rule, thickness = 1.dp)
                        }
                        val formattedDate = dateFormat.format(Date(fillup.entry.date))
                        val editA11y = stringResource(
                            R.string.dashboard_ledger_edit_a11y,
                            formattedDate
                        )
                        LedgerRow(
                            date = formattedDate,
                            odometer = formatDistanceWithUnit(
                                fillup.entry.odometer.toDouble(),
                                unit
                            ),
                            liters = stringResource(
                                R.string.dashboard_ledger_liters_value,
                                fillup.entry.liters
                            ),
                            mileage = fillup.mileageKmPerL?.let { value ->
                                formatMileageWithUnit(value, unit)
                            },
                            cost = currency.format(fillup.entry.cost),
                            compact = !wideLedger,
                            modifier = Modifier.padding(horizontal = spacing.md),
                            trailing = {
                                // The ledger opens the log; this is the one
                                // place the first screen can change a row
                                // without a detour through History.
                                TextButton(
                                    onClick = { onEditEntry(fillup.entry.id) },
                                    modifier = Modifier
                                        .heightIn(min = spacing.touchTarget)
                                        .semantics { contentDescription = editA11y }
                                ) {
                                    Text(stringResource(R.string.dashboard_ledger_edit))
                                }
                            }
                        )
                    }
                }
            }
        }

        if (uiState.trendFillups.size >= 2) {
            TrendSection(
                fillups = uiState.trendFillups,
                average = uiState.averageMileage,
                unit = unit,
                dateFormat = trendDateFormat,
                onViewCharts = onViewCharts
            )
        }
    }
}

@Composable
private fun TrendSection(
    fillups: List<FillupMileage>,
    average: Double?,
    unit: DistanceUnit,
    dateFormat: SimpleDateFormat,
    onViewCharts: () -> Unit
) {
    val spacing = MaterialTheme.spacing

    val values = fillups.mapNotNull { it.mileageKmPerL }
        .map { DistanceConverter.convertMileage(it, unit) }
    val mean = average?.let { DistanceConverter.convertMileage(it, unit) } ?: values.average()
    val ceiling = max(TREND_FLOOR_CEILING, ceil((values.maxOrNull() ?: 0.0) / 2.0) * 2.0)
    val points = fillups.mapNotNull { fillup ->
        fillup.mileageKmPerL?.let { value ->
            val converted = DistanceConverter.convertMileage(value, unit)
            TrendPoint(
                label = dateFormat.format(Date(fillup.entry.date)),
                value = converted,
                delta = converted - mean
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.dashboard_trend_title),
            note = stringResource(
                R.string.dashboard_trend_note,
                withUnit(formatOne(mean), unit.mileageLabel())
            ),
            action = {
                TextButton(onClick = onViewCharts) {
                    Text(stringResource(R.string.dashboard_trend_action))
                }
            }
        )
        Spacer(Modifier.height(spacing.lg))
        MileageTrendBars(
            points = points,
            average = mean,
            ceiling = ceiling,
            // A five-bar chart stretched across a tablet says less, not more.
            modifier = Modifier.widthIn(max = 720.dp)
        )
    }
}

@Composable
private fun EmptyLedger(
    hasVehicle: Boolean,
    onAddEntry: () -> Unit,
    onAddVehicle: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    LedgerPanel {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.dashboard_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(spacing.sm))
            Text(
                text = if (hasVehicle) {
                    stringResource(R.string.dashboard_empty_subtitle)
                } else {
                    stringResource(R.string.settings_vehicle_empty)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(spacing.lg))
            Button(onClick = if (hasVehicle) onAddEntry else onAddVehicle) {
                Text(
                    stringResource(
                        if (hasVehicle) R.string.dashboard_empty_cta
                        else R.string.dashboard_empty_vehicle_cta
                    )
                )
            }
        }
    }
}

@Composable
private fun LoadingBlock() {
    val label = stringResource(R.string.loading_generic)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.xxxl)
            .semantics(mergeDescendants = true) { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorBlock(onRetry: () -> Unit) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.dashboard_error_load),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(spacing.lg))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}
