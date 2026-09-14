package com.example.myapplication.ui.charts

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.R
import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.domain.calculation.CategoryMonthlySpendSeries
import com.example.myapplication.domain.calculation.MonthlyFuelSpend
import com.example.myapplication.ui.theme.MileLogElevation
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.theme.ledger
import com.example.myapplication.ui.theme.level1Shadow
import com.example.myapplication.ui.theme.spacing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Gap between month groups, and between bars inside a group. */
private const val GROUP_SPACE = 0.18f
private const val BAR_SPACE = 0.02f

/**
 * A reusable Composable that displays a monthly fuel spend bar chart
 * grouped by calendar month using MPAndroidChart.
 *
 * When [categorySpends] is non-empty, one bar is drawn per fuel category per
 * month (grouped, side-by-side) so the spend mix across categories is visible.
 *
 * Product default: costs always display in INR (₹), independent of device locale.
 *
 * @param spends List of [MonthlyFuelSpend] sorted chronologically.
 * @param categorySpends Per-category spend aligned to [spends] by index.
 * @param modifier Modifier for the chart card container.
 */
@Composable
fun MonthlySpendChart(
    spends: List<MonthlyFuelSpend>,
    categorySpends: List<CategoryMonthlySpendSeries> = emptyList(),
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    // One stable token per fuel type, so a bar can never be mistaken for
    // another fuel. Every category is mapped, so the lookups below cannot miss.
    val categoryColors: Map<FuelCategory, Int> = mapOf(
        FuelCategory.PETROL to MaterialTheme.ledger.chartPetrol.toArgb(),
        FuelCategory.DIESEL to MaterialTheme.ledger.chartDiesel.toArgb(),
        FuelCategory.CNG to MaterialTheme.ledger.chartCng.toArgb()
    )
    // The "Total spend" series exists only when the log holds a single fuel
    // category, so it never shares a chart with a per-category bar: the logbook
    // primary reads as the page's own accent rather than as a fuel.
    val totalBarColor = MaterialTheme.colorScheme.primary.toArgb()

    // Tooltip palette: the marker reads as an instrument readout, not a series.
    val markerSurface = MaterialTheme.ledger.chrome.toArgb()
    val markerRule = MaterialTheme.ledger.chromeRule.toArgb()
    val markerTitleColor = MaterialTheme.ledger.chromeTextMuted.toArgb()
    val markerValueColor = MaterialTheme.ledger.chromeText.toArgb()

    // Product default: INR (₹) — see note above.
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
    val currencySymbol = runCatching {
        Currency.getInstance(Locale.forLanguageTag("en-IN")).symbol
    }.getOrDefault("¤")

    val totalLabel = stringResource(R.string.charts_spend_total_label)
    val categoryLabels: Map<FuelCategory, String> = buildMap {
        FuelCategory.entries.forEach { category ->
            put(category, stringResource(category.labelRes))
        }
    }
    // Screen readers get the chart summary plus the tap affordance hint.
    val spendA11y = stringResource(R.string.charts_spend_a11y) +
        ". " + stringResource(R.string.charts_touch_hint)
    val spacing = MaterialTheme.spacing

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .level1Shadow(MileLogShapes.md)
            .semantics { contentDescription = spendA11y },
        shape = MileLogShapes.md,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = MileLogElevation.level1
        )
    ) {
        Text(
            text = stringResource(R.string.charts_spend_card_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = spacing.lg, top = spacing.lg, end = spacing.lg)
        )
        Text(
            text = if (categorySpends.isEmpty()) {
                stringResource(R.string.charts_spend_card_subtitle_single, currencySymbol)
            } else {
                stringResource(R.string.charts_spend_card_subtitle_split, currencySymbol)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = spacing.lg, bottom = spacing.sm)
        )

        if (spends.isEmpty()) {
            Text(
                text = stringResource(R.string.charts_spend_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(spacing.lg)
            )
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(start = spacing.sm, end = spacing.lg, bottom = spacing.lg),
                factory = { context ->
                    BarChart(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        description.isEnabled = false
                        legend.isEnabled = true
                        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                        legend.orientation = Legend.LegendOrientation.HORIZONTAL
                        legend.setDrawInside(false)
                        legend.textSize = 11f
                        legend.textColor = textColor
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(false)
                        setPinchZoom(false)
                        setDrawGridBackground(false)
                        setDrawBarShadow(false)
                        setDrawValueAboveBar(true)
                        setExtraOffsets(4f, 8f, 4f, 16f)

                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(false)
                        xAxis.granularity = 1f
                        xAxis.textColor = textColor
                        xAxis.textSize = 12f

                        axisLeft.textColor = textColor
                        axisLeft.gridColor = gridColor
                        axisLeft.textSize = 12f
                        axisLeft.setDrawAxisLine(false)
                        axisLeft.axisMinimum = 0f
                        axisLeft.valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String =
                                currencyFormatter.format(value.toDouble())
                        }

                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    chart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val idx = value.toInt()
                            return if (idx in spends.indices) spends[idx].label else ""
                        }
                    }
                    chart.xAxis.labelCount = minOf(spends.size, 4)

                    // Holds the per-category sets when grouped, so the marker
                    // can resolve a tapped dataSetIndex back to its month.
                    var groupedDataSets: List<BarDataSet> = emptyList()

                    if (categorySpends.isEmpty()) {
                        val entries = spends.mapIndexed { index, item ->
                            BarEntry(index.toFloat(), item.totalCost.toFloat())
                        }
                        val dataSet = BarDataSet(entries, totalLabel).apply {
                            color = totalBarColor
                            setDrawValues(true)
                            valueTextSize = 11f
                            valueTextColor = textColor
                            valueFormatter = object : ValueFormatter() {
                                override fun getFormattedValue(value: Float): String =
                                    currencyFormatter.format(value.toDouble())
                            }
                        }
                        val barData = BarData(dataSet).apply {
                            barWidth = if (spends.size == 1) 0.35f else 0.5f
                        }
                        chart.data = barData
                    } else {
                        val dataSets = categorySpends.map { series ->
                            val color = categoryColors.getValue(series.category)
                            val label = categoryLabels[series.category] ?: series.category.displayName
                            val entries = series.values.mapIndexed { index, value ->
                                BarEntry(index.toFloat(), value.toFloat())
                            }
                            BarDataSet(entries, label).apply {
                                this.color = color
                                setDrawValues(false)
                            }
                        }
                        groupedDataSets = dataSets
                        val seriesCount = dataSets.size
                        val barData = BarData(*dataSets.toTypedArray())
                        chart.data = barData

                        if (seriesCount >= 2) {
                            // BarData.groupBars() throws unless it holds at
                            // least two data sets, so a log with a single fuel
                            // category must not call it at all. It also has to
                            // run after the data is attached, because it reads
                            // the sets back off the chart.
                            barData.barWidth = 0.80f / seriesCount
                            chart.groupBars(0f, GROUP_SPACE, BAR_SPACE)
                        } else {
                            barData.barWidth = if (spends.size == 1) 0.35f else 0.5f
                        }
                    }

                    val grouped = categorySpends.isNotEmpty()

                    // Built every pass so it reads this pass's data lists rather
                    // than the first composition's, which `factory` would have
                    // captured.
                    val marker = ChartValueMarkerView(
                        context = chart.context,
                        chromeColor = markerSurface,
                        ruleColor = markerRule,
                        titleColor = markerTitleColor,
                        valueColor = markerValueColor,
                        content = { entry: Entry, highlight: Highlight ->
                            val setIndex = highlight.dataSetIndex
                            val series = if (grouped) categorySpends.getOrNull(setIndex) else null
                            // groupBars() rewrites each entry's x-value, so
                            // highlight.x is no longer the month index. The
                            // entry's position inside its own data set is.
                            val monthIndex = if (grouped) {
                                groupedDataSets.getOrNull(setIndex)?.getEntryIndex(entry) ?: -1
                            } else {
                                highlight.x.toInt()
                            }
                            ChartValueMarkerView.Content(
                                label = spends.getOrNull(monthIndex)?.label ?: "",
                                series = series?.let {
                                    categoryLabels[it.category] ?: it.category.displayName
                                },
                                value = currencyFormatter.format(entry.y.toDouble())
                            )
                        }
                    )
                    marker.setChartView(chart)
                    chart.setMarker(marker)
                    chart.setHighlightPerTapEnabled(true)

                    // Settle the viewport after the bar widths are decided.
                    // A setVisibleXRangeMaximum() call used to sit here too,
                    // but fitScreen() overrode it on the same pass, so the
                    // chart always showed every month regardless.
                    chart.fitScreen()
                    chart.invalidate()
                }
            )
        }
    }
}