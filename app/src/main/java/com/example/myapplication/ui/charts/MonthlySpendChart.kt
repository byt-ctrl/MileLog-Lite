package com.example.myapplication.ui.charts

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.domain.calculation.CategoryMonthlySpendSeries
import com.example.myapplication.domain.calculation.MonthlyFuelSpend
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.util.Locale

/**
 * A reusable Composable that displays a monthly fuel spend bar chart
 * grouped by calendar month using MPAndroidChart.
 *
 * When [categorySpends] is non-empty, one bar is drawn per fuel category per
 * month (grouped, side-by-side) so the spend mix across categories is visible.
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
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()
    val tertiaryColor = MaterialTheme.colorScheme.tertiary.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    val categoryColors = listOf(primaryColor, secondaryColor, tertiaryColor)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Monthly fuel spend chart showing total cost in rupees by calendar month" }
    ) {
        Text(
            text = "Monthly Fuel Spend",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        Text(
            text = if (categorySpends.isEmpty()) {
                "Total spend (₹) by calendar month"
            } else {
                "Total spend (₹) by calendar month, split by fuel type"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        if (spends.isEmpty()) {
            Text(
                text = "No fuel entries recorded yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(start = 8.dp, end = 16.dp, bottom = 16.dp),
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

                        // X axis (month labels along the bottom)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(false)
                        xAxis.granularity = 1f
                        xAxis.textColor = textColor
                        xAxis.textSize = 12f

                        // Left Y axis (cost in ₹)
                        axisLeft.textColor = textColor
                        axisLeft.gridColor = gridColor
                        axisLeft.textSize = 12f
                        axisLeft.setDrawAxisLine(false)
                        axisLeft.axisMinimum = 0f
                        axisLeft.valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return "₹" + NumberFormat.getIntegerInstance(Locale.getDefault())
                                    .format(value.toInt())
                            }
                        }

                        // Disable right Y axis
                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    // X-axis month labels
                    chart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val idx = value.toInt()
                            return if (idx in spends.indices) spends[idx].label else ""
                        }
                    }
                    chart.xAxis.labelCount = minOf(spends.size, 4)

                    if (categorySpends.isEmpty()) {
                        val entries = spends.mapIndexed { index, item ->
                            BarEntry(index.toFloat(), item.totalCost.toFloat())
                        }
                        val dataSet = BarDataSet(entries, "Total spend").apply {
                            color = primaryColor
                            setDrawValues(true)
                            valueTextSize = 11f
                            valueTextColor = textColor
                            valueFormatter = object : ValueFormatter() {
                                override fun getFormattedValue(value: Float): String =
                                    "₹" + NumberFormat.getIntegerInstance(Locale.getDefault())
                                        .format(value.toInt())
                            }
                        }
                        val barData = BarData(dataSet).apply {
                            barWidth = if (spends.size == 1) 0.35f else 0.5f
                        }
                        chart.data = barData
                    } else {
                        val dataSets = categorySpends.mapIndexed { idx, series ->
                            val color = categoryColors[
                                FuelCategory.entries.indexOf(series.category)
                                    .coerceIn(0, categoryColors.lastIndex)
                            ]
                            val entries = series.values.mapIndexed { index, value ->
                                BarEntry(index.toFloat(), value.toFloat())
                            }
                            BarDataSet(entries, series.category.displayName).apply {
                                this.color = color
                                setDrawValues(false)
                            }
                        }
                        val groupCount = spends.size
                        val seriesCount = dataSets.size
                        val barData = BarData(*dataSets.toTypedArray()).apply {
                            val groupSpace = 0.18f
                            val barSpace = 0.02f
                            val barWidth = 0.80f / seriesCount.coerceAtLeast(1)
                            this.barWidth = barWidth
                            // groupBars sets the offsets so groups are centered per x-tick
                            chart.groupBars(0f, groupSpace, barSpace)
                        }
                        chart.data = barData
                        chart.setVisibleXRangeMaximum(
                            (groupCount.toFloat() + 0.5f).coerceAtLeast(1f)
                        )
                    }
                    chart.fitScreen()
                    chart.invalidate()
                }
            )
        }
    }
}
