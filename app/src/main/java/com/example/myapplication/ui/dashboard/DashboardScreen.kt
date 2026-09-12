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
import com.example.myapplication.domain.calculation.FillupMileage
import com.example.myapplication.ui.components.GaugeScaleLabels
import com.example.myapplication.ui.components.InstrumentBand
import com.example.myapplication.ui.components.InstrumentBar
import com.example.myapplication.ui.components.LedgerPanel
import com.example.myapplication.ui.components.LedgerRow
import com.example.myapplication.ui.components.MileageGauge
import com.example.myapplication.ui.components.MileageTrendBars
import com.example.myapplication.ui.components.ReadoutItem
import com.example.myapplication.ui.components.ReadoutStrip
import com.example.myapplication.ui.components.SectionHeader
import com.example.myapplication.ui.components.TrendPoint
import com.example.myapplication.ui.components.formatOne
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
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")) }
    val integer = remember { NumberFormat.getIntegerInstance(Locale.getDefault()) }
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
                integer = integer,
                roomyReadouts = roomyReadouts
            )

            when {
                uiState.isLoading -> LoadingBlock()

                uiState.errorMessage != null -> ErrorBlock(onRetry = viewModel::retry)

                else -> Content(
                    uiState = uiState,
                    currency = currency,
                    integer = integer,
                    dateFormat = dateFormat,
                    trendDateFormat = trendDateFormat,
                    onAddEntry = onAddEntry,
                    onViewHistory = onViewHistory,
                    onViewCharts = onViewCharts,
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
    integer: NumberFormat,
    roomyReadouts: Boolean
) {
    val ledger = MaterialTheme.ledger
    val spacing = MaterialTheme.spacing
    val average = uiState.averageMileage
    val latest = uiState.latestMileage

    InstrumentBand {
        Text(
            text = stringResource(R.string.dashboard_binnacle_title),
            style = MaterialTheme.typography.headlineMedium,
            color = ledger.chromeText
        )
        Spacer(Modifier.height(spacing.xs))
        Text(
            text = uiState.latestFuelCategory?.let { category ->
                stringResource(
                    R.string.dashboard_binnacle_subtitle,
                    stringResource(category.labelRes)
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
                text = average?.let { formatOne(it) } ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
                style = MaterialTheme.typography.displayLarge,
                color = ledger.chromeText
            )
            Text(
                text = stringResource(R.string.dashboard_gauge_unit),
                style = MaterialTheme.typography.titleMedium,
                color = ledger.chromeReadout,
                modifier = Modifier.padding(start = spacing.sm, bottom = 6.dp)
            )
        }

        Spacer(Modifier.height(spacing.md))

        MileageGauge(
            value = average,
            ceiling = GAUGE_CEILING,
            contentDescription = gaugeA11y(average, latest)
        )

        Spacer(Modifier.height(spacing.sm))

        GaugeScaleLabels(ceiling = GAUGE_CEILING)

        Spacer(Modifier.height(spacing.sm))

        Text(
            text = gaugeNote(average, latest),
            style = MaterialTheme.typography.labelSmall,
            color = ledger.chromeTextMuted
        )

        Spacer(Modifier.height(spacing.lg))

        ReadoutStrip(
            items = readouts(uiState, currency, integer),
            compact = !roomyReadouts
        )
    }
}

@Composable
private fun readouts(
    uiState: DashboardUiState,
    currency: NumberFormat,
    integer: NumberFormat
): List<ReadoutItem> = listOf(
    ReadoutItem(
        label = stringResource(R.string.dashboard_stat_latest_odometer_title),
        value = uiState.latestOdometer?.let {
            stringResource(R.string.dashboard_stat_latest_odometer_unit, integer.format(it))
        } ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
        note = stringResource(R.string.dashboard_stat_total_cost_subtitle, uiState.entryCount)
    ),
    ReadoutItem(
        label = stringResource(R.string.dashboard_stat_cost_per_km_title),
        value = uiState.costPerKm?.let { currency.format(it) }
            ?: stringResource(R.string.dashboard_stat_latest_odometer_empty),
        note = stringResource(R.string.dashboard_stat_cost_per_km_subtitle, uiState.totalDistance)
    ),
    ReadoutItem(
        label = stringResource(R.string.dashboard_stat_total_cost_title),
        value = currency.format(uiState.totalCost),
        note = stringResource(R.string.dashboard_stat_total_cost_subtitle, uiState.entryCount)
    )
)

@Composable
private fun gaugeNote(average: Double?, latest: Double?): String = when {
    average == null -> stringResource(R.string.dashboard_gauge_note_no_average)
    latest == null -> stringResource(R.string.dashboard_gauge_note_plain, formatOne(average))
    abs(latest - average) < 0.05 -> stringResource(R.string.dashboard_gauge_note_level)
    latest > average -> stringResource(
        R.string.dashboard_gauge_note_above,
        formatOne(latest),
        formatOne(abs(latest - average))
    )
    else -> stringResource(
        R.string.dashboard_gauge_note_below,
        formatOne(latest),
        formatOne(abs(latest - average))
    )
}

@Composable
private fun gaugeA11y(average: Double?, latest: Double?): String = when {
    average == null -> stringResource(
        R.string.dashboard_gauge_a11y_empty,
        GAUGE_CEILING.toInt()
    )
    latest == null -> stringResource(
        R.string.dashboard_gauge_a11y,
        formatOne(average),
        stringResource(R.string.dashboard_stat_latest_odometer_empty),
        GAUGE_CEILING.toInt()
    )
    else -> stringResource(
        R.string.dashboard_gauge_a11y,
        formatOne(average),
        formatOne(latest),
        GAUGE_CEILING.toInt()
    )
}

@Composable
private fun Content(
    uiState: DashboardUiState,
    currency: NumberFormat,
    integer: NumberFormat,
    dateFormat: SimpleDateFormat,
    trendDateFormat: SimpleDateFormat,
    onAddEntry: () -> Unit,
    onViewHistory: () -> Unit,
    onViewCharts: () -> Unit,
    wideLedger: Boolean
) {
    val ledger = MaterialTheme.ledger
    val spacing = MaterialTheme.spacing

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
                EmptyLedger(onAddEntry = onAddEntry)
            } else {
                LedgerPanel {
                    uiState.recentFillups.forEachIndexed { index, fillup ->
                        if (index > 0) {
                            HorizontalDivider(color = ledger.rule, thickness = 1.dp)
                        }
                        LedgerRow(
                            date = dateFormat.format(Date(fillup.entry.date)),
                            odometer = stringResource(
                                R.string.dashboard_ledger_odometer_value,
                                integer.format(fillup.entry.odometer)
                            ),
                            liters = stringResource(
                                R.string.dashboard_ledger_liters_value,
                                fillup.entry.liters
                            ),
                            mileage = fillup.mileageKmPerL?.let { value ->
                                stringResource(R.string.dashboard_ledger_mileage_value, formatOne(value))
                            },
                            cost = currency.format(fillup.entry.cost),
                            compact = !wideLedger,
                            modifier = Modifier.padding(horizontal = spacing.md)
                        )
                    }
                }
            }
        }

        if (uiState.trendFillups.size >= 2) {
            TrendSection(
                fillups = uiState.trendFillups,
                average = uiState.averageMileage,
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
    dateFormat: SimpleDateFormat,
    onViewCharts: () -> Unit
) {
    val spacing = MaterialTheme.spacing

    val values = fillups.mapNotNull { it.mileageKmPerL }
    val mean = average ?: values.average()
    val ceiling = max(TREND_FLOOR_CEILING, ceil((values.maxOrNull() ?: 0.0) / 2.0) * 2.0)
    val points = fillups.mapNotNull { fillup ->
        fillup.mileageKmPerL?.let { value ->
            TrendPoint(
                label = dateFormat.format(Date(fillup.entry.date)),
                value = value,
                delta = value - mean
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.dashboard_trend_title),
            note = stringResource(R.string.dashboard_trend_note, formatOne(mean)),
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
private fun EmptyLedger(onAddEntry: () -> Unit) {
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
                text = stringResource(R.string.dashboard_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(spacing.lg))
            Button(onClick = onAddEntry) {
                Text(stringResource(R.string.dashboard_empty_cta))
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
