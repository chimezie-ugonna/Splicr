package com.splicr.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.splicr.app.R

@Composable
fun Modifier.customModifier(
    index: Int,
    listSize: Int,
    topPadding: Dp = dimensionResource(id = R.dimen.spacingXs),
    bottomPadding: Dp = 0.dp
): Modifier {
    return this
        .padding(top = if (index == 0) topPadding else 0.dp, bottom = bottomPadding)
        .clip(
            if (listSize == 1) {
                MaterialTheme.shapes.small
            } else if (listSize > 1 && index == 0) {
                RoundedCornerShape(
                    topStart = dimensionResource(
                        id = R.dimen.spacingXs
                    ), topEnd = dimensionResource(
                        id = R.dimen.spacingXs
                    )
                )
            } else if (listSize > 1 && index == listSize - 1) {
                RoundedCornerShape(
                    bottomStart = dimensionResource(
                        id = R.dimen.spacingXs
                    ), bottomEnd = dimensionResource(
                        id = R.dimen.spacingXs
                    )
                )
            } else {
                RoundedCornerShape(size = 0.dp)
            }
        )
}