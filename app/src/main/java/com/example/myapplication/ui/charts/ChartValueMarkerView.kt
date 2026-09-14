package com.example.myapplication.ui.charts

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import com.example.myapplication.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import kotlin.math.roundToInt

/**
 * MPAndroidChart tooltip that reports the exact value of a tapped bar or line
 * point.
 *
 * [MarkerView] is a [android.widget.RelativeLayout] that inflates its layout
 * into itself, so the chart composables build a fresh instance inside the
 * `AndroidView` `update` block rather than in `factory`. `update` runs on every
 * recomposition, which keeps [content] describing the data currently drawn; a
 * marker created once in `factory` would keep reading the first composition's
 * series forever.
 *
 * @param chromeColor Fill colour of the bubble. Uses the instrument surface so
 *   the tooltip reads as a readout rather than as a series colour.
 * @param ruleColor 1dp border colour of the bubble.
 * @param titleColor Colour of the title line (muted on the instrument).
 * @param valueColor Colour of the value line (the bright instrument text).
 * @param content Resolves the strings to show for a tapped entry. The marker
 *   carries no chart knowledge; the caller supplies the label, the optional
 *   series name, and the already-formatted value.
 */
class ChartValueMarkerView(
    context: Context,
    chromeColor: Int,
    ruleColor: Int,
    titleColor: Int,
    valueColor: Int,
    private val content: (Entry, Highlight) -> Content
) : MarkerView(context, R.layout.chart_marker_view) {

    /**
     * Strings the marker renders for one highlight. [series] is null when the
     * tapped data set is the combined total, in which case the title is just
     * the axis label.
     */
    data class Content(
        val label: String,
        val series: String?,
        val value: String
    )

    private val titleView: TextView = findViewById(R.id.chart_marker_title)
    private val valueView: TextView = findViewById(R.id.chart_marker_value)

    init {
        val density = resources.displayMetrics.density
        // Rounded instrument surface: solid fill plus a hairline rule border.
        val bubble = GradientDrawable().apply {
            cornerRadius = 4f * density
            setColor(chromeColor)
            setStroke((1f * density).roundToInt(), ruleColor)
        }
        findViewById<View>(R.id.chart_marker_root).background = bubble
        titleView.setTextColor(titleColor)
        valueView.setTextColor(valueColor)
    }

    override fun refreshContent(e: Entry, highlight: Highlight) {
        // Super measures and lays the view out using the text set on the
        // previous highlight (or nothing on first show).
        super.refreshContent(e, highlight)

        val resolved = content(e, highlight)
        titleView.text = if (resolved.series == null) {
            resolved.label
        } else {
            context.getString(R.string.charts_marker_title, resolved.label, resolved.series)
        }
        valueView.text = resolved.value

        // The text just changed, so measure again before reading width/height:
        // otherwise the offset below is computed from the stale bubble size.
        measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        layout(0, 0, measuredWidth, measuredHeight)

        // Centre the bubble over the tapped point, lifted clear of it by a gap.
        val gapPx = 8f * resources.displayMetrics.density
        setOffset(-width / 2f, -height.toFloat() - gapPx)
    }
}
