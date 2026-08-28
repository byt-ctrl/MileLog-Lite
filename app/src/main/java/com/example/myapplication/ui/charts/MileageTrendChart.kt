package com.example.myapplication.ui.charts

import android.graphics.Color
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
import com.example.myapplication.domain.calculation.FillupMileage
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A reusable Composable that displays a mileage trend line chart (km/L per fill-up)
 * in chronological order using MPAndroidChart.
 *
 * @param fillups List of [FillupMileage] sorted by odometer ascending. The first entry
 *   (with null mileage) is skipped since we can't compute mileage without a prior reading.
 * @param modifier Modifier for the chart card container.
 */
@Composable
fun MileageTrendChart(
    fillups: List<FillupMileage>,
    modifier: Modifier = Modifier
) {
    // Only include entries that have a computed mileage value (skip the first entry)
    val chartPoints = fillups.filter { it.mileageKmPerL != null }

    val lineColor = MaterialTheme.colorScheme.primary.toArgb()
    val fillColor = MaterialTheme.colorScheme.primaryContainer.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Mileage Trend",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        Text(
            text = "km/L per fill-up",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        if (chartPoints.size < 2) {
            Text(
                text = "Add at least 3 entries to see the mileage trend.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(start = 8.dp, end = 16.dp, bottom = 16.dp),
                factory = { context ->
                    LineChart(context).apply {
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
                        setExtraOffsets(4f, 4f, 4f, 8f)

                        // X axis (dates along the bottom)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(false)
                        xAxis.granularity = 1f
                        xAxis.textColor = textColor
                        xAxis.textSize = 12f

                        // Left Y axis (km/L)
                        axisLeft.textColor = textColor
                        axisLeft.gridColor = gridColor
                        axisLeft.textSize = 12f
                        axisLeft.setDrawAxisLine(false)

                        // Disable right Y axis
                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

                    // Build entries: x = index, y = mileage km/L
                    val entries = chartPoints.mapIndexed { index, fillup ->
                        Entry(index.toFloat(), fillup.mileageKmPerL!!.toFloat())
                    }

                    // X-axis date labels
                    chart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val idx = value.toInt()
                            return if (idx in chartPoints.indices) {
                                dateFormat.format(Date(chartPoints[idx].entry.date))
                            } else ""
                        }
                    }
                    chart.xAxis.labelCount = minOf(chartPoints.size, 5)

                    val dataSet = LineDataSet(entries, "Mileage").apply {
                        color = lineColor
                        setCircleColor(lineColor)
                        lineWidth = 2.5f
                        circleRadius = 4f
                        setDrawCircleHole(true)
                        circleHoleRadius = 2f
                        setDrawValues(true)
                        valueTextSize = 11f
                        valueTextColor = textColor
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return String.format(Locale.getDefault(), "%.1f", value)
                            }
                        }
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        setDrawFilled(true)
                        setFillColor(fillColor)
                        fillAlpha = 50
                    }

                    chart.data = LineData(dataSet)
                    chart.invalidate()
                }
            )
        }
    }
}
