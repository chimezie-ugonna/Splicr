package com.splicr.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splicr.app.R

@Composable
fun CustomListItem(
    modifier: Modifier = Modifier,
    showDivider: Boolean,
    textStringResource: Int?,
    textString: String,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    textStartPadding: Dp = dimensionResource(id = R.dimen.spacingMd),
    subTextString: String?,
    showArrow: Boolean,
    leadingIcon: Int? = null,
    leadingIconWidth: Dp = 28.dp,
    leadingIconHeight: Dp = 28.dp,
    leadingIconAlignment: Alignment.Vertical = Alignment.CenterVertically
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface),
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = dimensionResource(id = R.dimen.spacingMd)),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (leadingIcon != null) {
                Image(
                    painter = painterResource(id = leadingIcon),
                    modifier = Modifier
                        .width(leadingIconWidth)
                        .height(leadingIconHeight)
                        .align(leadingIconAlignment),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .padding(
                        start = if (leadingIcon != null) textStartPadding else 0.dp,
                        end = if (showArrow) dimensionResource(id = R.dimen.spacingMd) else 0.dp
                    )
            ) {
                Text(
                    text = if (textStringResource != null) stringResource(textStringResource) else textString,
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (subTextString != null) {
                    Text(
                        text = subTextString,
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.spacingXxxs)),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (showArrow) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = null
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(
                    start = dimensionResource(id = R.dimen.spacingMd)
                ), thickness = 0.5.dp, color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun CustomDropDownListItem(
    modifier: Modifier = Modifier, showDivider: Boolean, textString: String, subTextString: String
) {
    val expanded = rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded.value = !expanded.value },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = dimensionResource(id = R.dimen.spacingMd)),
            verticalAlignment = Alignment.Top
        ) {

            Column(
                Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .padding(
                        end = dimensionResource(id = R.dimen.spacingMd)
                    )
            ) {
                Text(
                    text = textString,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Start
                )

                AnimatedVisibility(
                    visible = expanded.value, enter = expandVertically(), exit = shrinkVertically()
                ) {
                    Text(
                        text = subTextString,
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.spacingXxxs)),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }

            val rotationAngle by animateFloatAsState(
                targetValue = if (expanded.value) 90f else 0f, label = "Rotate Icon"
            )
            Image(
                painter = painterResource(id = R.drawable.arrow_right),
                contentDescription = null,
                Modifier.rotate(rotationAngle)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(
                    start = dimensionResource(id = R.dimen.spacingMd)
                ), thickness = 0.5.dp, color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}