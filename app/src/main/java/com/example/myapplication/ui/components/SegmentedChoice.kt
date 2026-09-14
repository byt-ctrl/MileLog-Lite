package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MileLogShapes
import com.example.myapplication.ui.theme.ledger
import com.example.myapplication.ui.theme.spacing

/**
 * A segmented radio group: every option visible at once, the selected one
 * carried by a filled surface and by the radio role rather than by colour
 * alone.
 *
 * Three screens ask the same question of a small closed set (fuel type on the
 * entry sheet, fuel type and distance unit on the vehicle form), so the control
 * lives here rather than being written out three times.
 */
@Composable
fun <T> SegmentedChoice(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val ledger = MaterialTheme.ledger

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MileLogShapes.sm)
            .background(ledger.rule)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEach { option ->
            Segment(
                text = label(option),
                isSelected = option == selected,
                onSelect = { onSelect(option) }
            )
        }
    }
}

@Composable
private fun RowScope.Segment(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .weight(1f)
            .clip(MileLogShapes.sm)
            .background(if (isSelected) colors.primary else colors.surface)
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onSelect
            )
            .heightIn(min = MaterialTheme.spacing.touchTargetMin)
            .padding(horizontal = MaterialTheme.spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) colors.onPrimary else colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
