package com.example.myapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.DataTextStyle
import com.example.myapplication.ui.theme.DataTextStyleSmall
import com.example.myapplication.ui.theme.MicroLabelStyle
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.theme.MileLogWindow
import com.example.myapplication.ui.theme.ledger
import com.example.myapplication.ui.theme.spacing

/**
 * A full-bleed instrument band whose content is capped and centred.
 *
 * The dark material has to reach both window edges or it stops reading as the
 * machine's casing, but the readings inside should not smear across a tablet.
 * Background full-bleed, content capped: two containers, which is why this
 * exists rather than being inlined three times.
 */
@Composable
fun InstrumentBand(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val ledger = MaterialTheme.ledger
    val spacing = MaterialTheme.spacing

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ledger.chrome),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = MileLogWindow.contentMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = spacing.lg, vertical = spacing.lg),
            content = content
        )
    }
}

/**
 * The logbook's content frame.
 *
 * The paper reaches both window edges, but the ledger, the forms and the
 * settings rows stop at [MileLogWindow.contentMaxWidth] and centre inside it.
 * A wide window then gets a readable measure instead of a stretched one: the
 * rules and the figures below are already capped this way, and without this
 * frame a 1600dp desktop window would draw a settings row nearly the full
 * width of the glass.
 *
 * The instrument is deliberately not wrapped: its dark casing has to reach the
 * window edges or it stops reading as the machine ([InstrumentBand] caps its
 * own content instead).
 */
@Composable
fun LogbookContent(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = MileLogWindow.contentMaxWidth)
                .fillMaxWidth(),
            verticalArrangement = verticalArrangement,
            content = content
        )
    }
}

/** A logbook panel: surface fill, hairline edge, small radius, no shadow. */
@Composable
fun LedgerPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val ledger = MaterialTheme.ledger
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MileLogShapes.md)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, ledger.rule, MileLogShapes.md),
        content = content
    )
}

/** Title, optional note and action, closed by a rule. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    note: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    val ledger = MaterialTheme.ledger
    val spacing = MaterialTheme.spacing

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            action?.invoke()
        }
        note?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Spacer(Modifier.height(spacing.sm))
        HorizontalDivider(color = ledger.ruleStrong, thickness = 1.dp)
    }
}

/** Column captions for the wide ledger, in display order. */
val LedgerColumnLabels = listOf(
    R.string.ledger_column_date,
    R.string.ledger_column_odometer,
    R.string.ledger_column_litres,
    R.string.ledger_column_mileage,
    R.string.ledger_column_cost
)

/** Relative column widths, positionally matched to [LedgerColumnLabels]. */
val LedgerColumnWeights = listOf(1.3f, 1.1f, 0.8f, 1f, 1.15f)

/**
 * Width reserved for a row's trailing control. Fixed, so the column captions
 * stay over their columns whether a screen puts an icon, a link or nothing
 * beside them.
 */
val LedgerTrailingWidth: Dp = 76.dp

/** Column captions for the wide ledger. */
@Composable
fun LedgerHeaderRow(
    labels: List<String>,
    weights: List<Float>,
    modifier: Modifier = Modifier,
    reserveTrailing: Boolean = false
) {
    val ledger = MaterialTheme.ledger
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = MaterialTheme.spacing.sm)
    ) {
        Row(modifier = Modifier.weight(1f)) {
            labels.forEachIndexed { index, label ->
                Text(
                    text = label.uppercase(),
                    style = MicroLabelStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = if (index == 0) TextAlign.Start else TextAlign.End,
                    modifier = Modifier.weight(weights[index])
                )
            }
        }
        if (reserveTrailing) {
            Spacer(Modifier.width(LedgerTrailingWidth))
        }
    }
    HorizontalDivider(color = ledger.ruleStrong, thickness = 1.dp)
}

/**
 * One fill-up in the ledger.
 *
 * The row states the same facts in every width: when, how far, how much fuel,
 * what it returned, what it cost. [compact] stacks them into two scannable
 * lines for phones; otherwise they sit in aligned columns with the figures
 * right-aligned so they compare straight down the page.
 */
@Composable
fun LedgerRow(
    date: String,
    odometer: String,
    liters: String,
    mileage: String?,
    cost: String,
    compact: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.colorScheme
    val rowModifier = Modifier
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(vertical = spacing.md)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (compact) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .then(rowModifier)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "$odometer  ·  $liters",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    ReadingText(
                        value = mileage,
                        style = DataTextStyle,
                        color = colors.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = cost,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .then(rowModifier)
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = odometer,
                    style = DataTextStyle,
                    color = colors.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.1f)
                )
                Text(
                    text = liters,
                    style = DataTextStyle,
                    color = colors.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.8f)
                )
                ReadingText(
                    value = mileage,
                    style = DataTextStyle,
                    color = colors.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = cost,
                    style = DataTextStyle,
                    color = colors.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.15f)
                )
            }
        }

        trailing?.let { control ->
            Box(
                modifier = Modifier.width(LedgerTrailingWidth),
                contentAlignment = Alignment.CenterEnd
            ) {
                control()
            }
        }
    }
}

/** One column in [MileageTrendBars]. [delta] is the signed gap to the average. */
data class TrendPoint(
    val label: String,
    val value: Double,
    val delta: Double
)

/**
 * Mileage by fill-up.
 *
 * The axis keeps its zero baseline, which is the honest choice and also the
 * reason a four-bar chart of 17.0..19.4 km/L tells you nothing on its own. The
 * dashed rule at the [average] and the signed delta under each value carry the
 * change the bar heights cannot.
 */
@Composable
fun MileageTrendBars(
    points: List<TrendPoint>,
    average: Double,
    ceiling: Double,
    modifier: Modifier = Modifier
) {
    val ledger = MaterialTheme.ledger
    val colors = MaterialTheme.colorScheme
    val spacing = MaterialTheme.spacing
    val trackHeight = 132.dp
    val barShape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                points.forEach { point ->
                    val fraction = if (ceiling > 0.0) {
                        (point.value / ceiling).coerceIn(0.0, 1.0).toFloat()
                    } else {
                        0f
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(trackHeight),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fraction)
                                    .background(colors.primary.copy(alpha = 0.82f), barShape)
                            )
                        }
                        Spacer(Modifier.height(spacing.sm))
                        Text(
                            text = formatOne(point.value),
                            style = DataTextStyleSmall,
                            color = colors.onSurface
                        )
                        Text(
                            text = formatSigned(point.delta),
                            style = DataTextStyleSmall.copy(fontSize = 11.sp),
                            color = if (point.delta >= 0.0) ledger.good else ledger.fuel
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .align(Alignment.TopStart)
            ) {
                val stroke = 1.dp.toPx()
                if (ceiling > 0.0) {
                    val y = (size.height * (1f - (average / ceiling).toFloat()))
                        .coerceIn(0f, size.height)
                    drawLine(
                        color = ledger.ruleStrong,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = stroke,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(4.dp.toPx(), 4.dp.toPx())
                        )
                    )
                }
                drawLine(
                    color = ledger.ruleStrong,
                    start = Offset(0f, size.height - stroke / 2f),
                    end = Offset(size.width, size.height - stroke / 2f),
                    strokeWidth = stroke
                )
            }
        }
    }
}

internal fun formatOne(value: Double): String = String.format(java.util.Locale.getDefault(), "%.1f", value)

internal fun formatSigned(value: Double): String {
    val sign = if (value >= 0.0) "+" else "-"
    return sign + String.format(java.util.Locale.getDefault(), "%.1f", kotlin.math.abs(value))
}
