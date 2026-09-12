package com.example.myapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.DataMono
import com.example.myapplication.ui.theme.ledger
import com.example.myapplication.ui.theme.spacing

/**
 * The instrument bar. Dark in every appearance, because it is the backlit
 * cluster of the machine rather than a themed surface. Carries the wordmark on
 * root destinations and a screen title plus back control on detail ones.
 */
@Composable
fun InstrumentBar(
    title: String,
    modifier: Modifier = Modifier,
    wordmark: Boolean = false,
    onNavigateUp: (() -> Unit)? = null,
    backContentDescription: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val ledger = MaterialTheme.ledger
    val spacing = MaterialTheme.spacing

    Surface(
        modifier = modifier,
        color = ledger.chrome,
        contentColor = ledger.chromeText
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onNavigateUp != null && backContentDescription != null) {
                IconButton(
                    onClick = onNavigateUp,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = backContentDescription,
                        tint = ledger.chromeText
                    )
                }
            } else {
                Spacer(Modifier.width(spacing.md))
            }

            if (wordmark) {
                MileLogWordmark()
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = ledger.chromeText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = spacing.xs)
                )
            }

            Spacer(Modifier.weight(1f))
            actions()
            Spacer(Modifier.width(spacing.xs))
        }
    }
}

@Composable
fun MileLogWordmark() {
    val ledger = MaterialTheme.ledger
    Row(verticalAlignment = Alignment.CenterVertically) {
        BrandMark()
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        Text(
            text = stringResource(R.string.app_name).uppercase(),
            fontFamily = DataMono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp,
            color = ledger.chromeText
        )
    }
}

/** A four-tick gauge face with the amber band on the leading edge. */
@Composable
private fun BrandMark() {
    val ledger = MaterialTheme.ledger
    Canvas(modifier = Modifier.size(width = 22.dp, height = 14.dp)) {
        val stroke = 1.dp.toPx()
        val corner = CornerRadius(2.dp.toPx())

        drawRoundRect(
            color = ledger.chromeMarker.copy(alpha = 0.85f),
            topLeft = Offset(stroke, stroke),
            size = Size(size.width * 0.4f - stroke, size.height - stroke * 2),
            cornerRadius = corner
        )

        val divisions = 4
        for (i in 1 until divisions) {
            val x = size.width * i / divisions
            drawLine(
                color = ledger.chromeRule,
                start = Offset(x, stroke * 2),
                end = Offset(x, size.height - stroke * 2),
                strokeWidth = stroke
            )
        }

        drawRoundRect(
            color = ledger.chromeRule,
            size = size,
            cornerRadius = corner,
            style = Stroke(stroke)
        )
    }
}
