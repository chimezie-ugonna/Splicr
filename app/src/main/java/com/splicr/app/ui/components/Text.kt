package com.splicr.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.splicr.app.R

val TextUnit.nonScaledSp
    @Composable get() = (this.value / LocalDensity.current.fontScale).sp

@Composable
fun AppNameText(modifier: Modifier) {
    Text(
        text = stringResource(id = R.string.in_app_name),
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall,
        fontSize = MaterialTheme.typography.titleSmall.fontSize.nonScaledSp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun PremiumText(modifier: Modifier) {
    Row(
        modifier = modifier.wrapContentSize(),
        horizontalArrangement = Arrangement.spacedBy(
            space = dimensionResource(
                id = R.dimen.spacingXs
            )
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(size = dimensionResource(id = R.dimen.spacingMd)),
            painter = painterResource(id = R.drawable.premium_diamond),
            contentDescription = null
        )

        Text(
            modifier = Modifier.wrapContentSize(),
            text = stringResource(R.string.premium),
            color = MaterialTheme.colorScheme.surfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Start
        )
    }
}