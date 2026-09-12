package com.example.myapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.DataTextStyle
import com.example.myapplication.ui.theme.MicroLabelStyle
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.theme.ledger
import com.example.myapplication.ui.theme.spacing

/** One instrument reading: micro-label, monospace value, optional context. */
data class ReadoutItem(
    val label: String,
    val value: String,
    val note: String? = null
)

/**
 * The mileage gauge. A real 0..[ceiling] scale with ticks, a translucent fill
 * for the current value, and an amber marker, so the number has position on the
 * dial rather than only magnitude. Callers supply the accessible reading; the
 * ticks and marker are decorative.
 */
@Composable
fun MileageGauge(
    value: Double?,
    ceiling: Double,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val ledger = MaterialTheme.ledger
    val fraction = if (value != null && ceiling > 0.0) {
        (value / ceiling).coerceIn(0.0, 1.0).toFloat()
    } else {
        0f
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            )
    ) {
        val stroke = 1.dp.toPx()
        val corner = CornerRadius(2.dp.toPx())

        if (fraction > 0f) {
            drawRect(
                color = ledger.chromeReadout.copy(alpha = 0.30f),
                size = Size(size.width * fraction, size.height)
            )
        }

        val divisions = 12
        for (i in 1 until divisions) {
            val x = size.width * i / divisions
            drawLine(
                color = ledger.chromeRule,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = stroke
            )
        }

        drawRoundRect(
            color = ledger.chromeRule,
            size = size,
            cornerRadius = corner,
            style = Stroke(stroke)
        )

        if (fraction > 0f) {
            val markerWidth = 2.dp.toPx()
            val markerX = (size.width * fraction - markerWidth / 2f)
                .coerceIn(0f, size.width - markerWidth)
            drawRect(
                color = ledger.chromeMarker,
                topLeft = Offset(markerX, 0f),
                size = Size(markerWidth, size.height)
            )
        }
    }
}

/** The 0 / mid / top labels under a [MileageGauge]. */
@Composable
fun GaugeScaleLabels(
    ceiling: Double,
    modifier: Modifier = Modifier
) {
    val ledger = MaterialTheme.ledger
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf(0.0, ceiling / 2.0, ceiling).forEach { tick ->
            Text(
                text = formatTick(tick),
                style = MicroLabelStyle,
                color = ledger.chromeTextMuted
            )
        }
    }
}

private fun formatTick(value: Double): String =
    if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(java.util.Locale.getDefault(), "%.1f", value)
    }

/**
 * The readout strip under the gauge.
 *
 * [compact] renders the ruled list used on phones (label and value on one line,
 * context beneath), which keeps the instrument shallow so the ledger below it
 * stays reachable. Otherwise the readings sit side by side in a bordered strip.
 */
@Composable
fun ReadoutStrip(
    items: List<ReadoutItem>,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val ledger = MaterialTheme.ledger
    val spacing = MaterialTheme.spacing

    if (compact) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, ledger.chromeRule, MileLogShapes.sm)
                .clip(MileLogShapes.sm)
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(color = ledger.chromeRule, thickness = 1.dp)
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.md, vertical = spacing.sm)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = item.label.uppercase(),
                            style = MicroLabelStyle,
                            color = ledger.chromeTextMuted,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = item.value,
                            style = DataTextStyle,
                            color = ledger.chromeText
                        )
                    }
                    item.note?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.labelSmall,
                            color = ledger.chromeTextMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .border(1.dp, ledger.chromeRule, MileLogShapes.sm)
                .clip(MileLogShapes.sm)
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(ledger.chromeRule)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(spacing.md)
                ) {
                    Text(
                        text = item.label.uppercase(),
                        style = MicroLabelStyle,
                        color = ledger.chromeTextMuted
                    )
                    Spacer(Modifier.height(spacing.xs))
                    Text(
                        text = item.value,
                        style = DataTextStyle,
                        color = ledger.chromeText
                    )
                    item.note?.let { note ->
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = note,
                            style = MaterialTheme.typography.labelSmall,
                            color = ledger.chromeTextMuted,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}
