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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.domain.calculation.MonthlyFuelSpend
import com.github.mikephil.charting.charts.BarChart
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
 * @param spends List of [MonthlyFuelSpend] sorted chronologically.
 * @param modifier Modifier for the chart card container.
 */
@Composable
fun MonthlySpendChart(
    spends: List<MonthlyFuelSpend>,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Monthly Fuel Spend",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        Text(
            text = "Total spend (₹) by calendar month",
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
                    .height(240.dp)
                    .padding(start = 8.dp, end = 16.dp, bottom = 16.dp),
                factory = { context ->
                    BarChart(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        description.isEnabled = false
                        legend.isEnabled = false
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(false)
                        setPinchZoom(false)
                        setDrawGridBackground(false)
                        setDrawBarShadow(false)
                        setDrawValueAboveBar(true)
                        setExtraOffsets(4f, 8f, 4f, 8f)

                        // X axis (month labels along the bottom)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(false)
                        xAxis.granularity = 1f
                        xAxis.textColor = textColor
                        xAxis.textSize = 10f

                        // Left Y axis (cost in ₹)
                        axisLeft.textColor = textColor
                        axisLeft.gridColor = gridColor
                        axisLeft.textSize = 10f
                        axisLeft.setDrawAxisLine(false)
                        axisLeft.axisMinimum = 0f
                        axisLeft.valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return "₹" + NumberFormat.getIntegerInstance(Locale.getDefault()).format(value.toInt())
                            }
                        }

                        // Disable right Y axis
                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    val entries = spends.mapIndexed { index, item ->
                        BarEntry(index.toFloat(), item.totalCost.toFloat())
                    }

                    // X-axis month labels
                    chart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val idx = value.toInt()
                            return if (idx in spends.indices) {
                                spends[idx].label
                            } else ""
                        }
                    }
                    chart.xAxis.labelCount = minOf(spends.size, 6)

                    val dataSet = BarDataSet(entries, "Monthly Spend").apply {
                        color = barColor
                        setDrawValues(true)
                        valueTextSize = 9f
                        valueTextColor = textColor
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return "₹" + NumberFormat.getIntegerInstance(Locale.getDefault()).format(value.toInt())
                            }
                        }
                    }

                    val barData = BarData(dataSet).apply {
                        barWidth = if (spends.size == 1) 0.35f else 0.5f
                    }

                    chart.data = barData
                    chart.fitScreen()
                    chart.invalidate()
                }
            )
        }
    }
}
