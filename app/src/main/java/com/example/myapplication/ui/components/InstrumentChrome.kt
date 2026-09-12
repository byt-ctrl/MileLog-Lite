package com.example.myapplication.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.painterResource
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
 * cluster of the machine rather than a themed surface.
 *
 * The MileLog mark leads every bar: with the wordmark on root destinations,
 * beside the screen title on detail ones. Same object as the launcher icon.
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = spacing.xs)
                ) {
                    MileLogMark()
                    Spacer(Modifier.width(spacing.sm))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = ledger.chromeText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            actions()
            Spacer(Modifier.width(spacing.xs))
        }
    }
}

/** The product mark plus the name, as it appears on the chrome. */
@Composable
fun MileLogWordmark() {
    val ledger = MaterialTheme.ledger
    Row(verticalAlignment = Alignment.CenterVertically) {
        MileLogMark()
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

/**
 * The MileLog logo: the fuel drop with its amber band, the same vector the
 * launcher icon is built from. Decorative here, so it carries no description;
 * the wordmark or title beside it names the screen.
 */
@Composable
fun MileLogMark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.ic_milelog_mark),
        contentDescription = null,
        modifier = modifier.height(22.dp)
    )
}
