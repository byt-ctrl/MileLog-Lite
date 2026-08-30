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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.R
import com.example.myapplication.data.local.FuelCategory
import com.example.myapplication.domain.calculation.CategoryMileageSeries
import com.example.myapplication.domain.calculation.FillupMileage
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
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
 * The combined (all-categories) trend is rendered as the primary line, and one
 * additional overlay line is drawn per fuel category in [categorySeries] so
 * per-category performance can be compared at a glance.
 *
 * @param fillups List of [FillupMileage] sorted by odometer ascending. The first entry
 *   (with null mileage) is skipped since we can't compute mileage without a prior reading.
 * @param categorySeries Per-category mileage overlays. Only categories with at least
 *   one mileage-bearing entry are drawn.
 * @param modifier Modifier for the chart card container.
 */
@Composable
fun MileageTrendChart(
    fillups: List<FillupMileage>,
    categorySeries: List<CategoryMileageSeries> = emptyList(),
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()
    val tertiaryColor = MaterialTheme.colorScheme.tertiary.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    // Distinct color per category. Order matches FuelCategory.entries.
    val categoryColors = listOf(primaryColor, secondaryColor, tertiaryColor)

    val combinedLabel = stringResource(R.string.charts_combined_label)
    val categoryLabels: Map<FuelCategory, String> = buildMap {
        FuelCategory.entries.forEach { category ->
            put(category, stringResource(category.labelRes))
        }
    }
    val trendA11y = stringResource(R.string.charts_trend_a11y)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = trendA11y }
    ) {
        Text(
            text = stringResource(R.string.charts_trend_card_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        Text(
            text = if (categorySeries.isEmpty()) {
                stringResource(R.string.charts_trend_card_subtitle_single)
            } else {
                stringResource(R.string.charts_trend_card_subtitle_split)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(start = 8.dp, end = 16.dp, bottom = 16.dp),
            factory = { context ->
                LineChart(context).apply {
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
                    legend.form = Legend.LegendForm.LINE
                    setTouchEnabled(true)
                    isDragEnabled = true
                    setScaleEnabled(false)
                    setPinchZoom(false)
                    setDrawGridBackground(false)
                    setExtraOffsets(4f, 4f, 4f, 16f)

                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.setDrawGridLines(false)
                    xAxis.granularity = 1f
                    xAxis.textColor = textColor
                    xAxis.textSize = 12f

                    axisLeft.textColor = textColor
                    axisLeft.gridColor = gridColor
                    axisLeft.textSize = 12f
                    axisLeft.setDrawAxisLine(false)

                    axisRight.isEnabled = false
                }
            },
            update = { chart ->
                val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

                val dataSets = mutableListOf<LineDataSet>()

                val combinedPoints = fillups.filter { it.mileageKmPerL != null }
                val perCategoryPoints: List<List<FillupMileage>> = categorySeries.map { series ->
                    series.fillups.filter { it.mileageKmPerL != null }
                }

                if (combinedPoints.size >= 2) {
                    val entries = combinedPoints.mapIndexed { index, fillup ->
                        Entry(index.toFloat(), fillup.mileageKmPerL!!.toFloat())
                    }
                    dataSets += LineDataSet(entries, combinedLabel).apply {
                        color = primaryColor
                        setCircleColor(primaryColor)
                        lineWidth = 3f
                        circleRadius = 4f
                        setDrawCircleHole(true)
                        circleHoleRadius = 2f
                        setDrawValues(true)
                        valueTextSize = 11f
                        valueTextColor = textColor
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String =
                                String.format(Locale.getDefault(), "%.1f", value)
                        }
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        setDrawFilled(false)
                    }
                }

                categorySeries.forEachIndexed { index, series ->
                    val points = perCategoryPoints[index]
                    if (points.size < 2) return@forEachIndexed
                    val color = categoryColors[FuelCategory.entries.indexOf(series.category)
                        .coerceIn(0, categoryColors.lastIndex)]
                    val categoryLabel = categoryLabels[series.category] ?: series.category.displayName
                    val entries = points.mapIndexed { idx, fillup ->
                        Entry(idx.toFloat(), fillup.mileageKmPerL!!.toFloat())
                    }
                    dataSets += LineDataSet(entries, categoryLabel).apply {
                        this.color = color
                        setCircleColor(color)
                        lineWidth = 1.75f
                        circleRadius = 3f
                        setDrawCircleHole(true)
                        circleHoleRadius = 1.5f
                        enableDashedLine(8f, 6f, 0f)
                        setDrawValues(false)
                        mode = LineDataSet.Mode.LINEAR
                        setDrawFilled(false)
                    }
                }

                chart.xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val idx = value.toInt()
                        return if (idx in combinedPoints.indices) {
                            dateFormat.format(Date(combinedPoints[idx].entry.date))
                        } else ""
                    }
                }
                chart.xAxis.labelCount = minOf(combinedPoints.size.coerceAtLeast(1), 4)

                chart.data = if (dataSets.isNotEmpty()) LineData(*dataSets.toTypedArray()) else null
                chart.invalidate()
            }
        )
    }
}