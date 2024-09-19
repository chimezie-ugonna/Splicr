@file:OptIn(ExperimentalMaterial3Api::class)

package com.splicr.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.splicr.app.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    textResource: Int,
    textColor: Color? = null,
    textStyle: TextStyle? = null,
    textFontWeight: FontWeight? = null,
    leadingImageResource: Int? = null,
    leadingDisabledImageResource: Int? = null,
    leadingImageContentDescriptionResource: Int? = null,
    trailingImageResource: Int? = null,
    trailingDisabledImageResource: Int? = null,
    trailingImageContentDescriptionResource: Int? = null,
    uiEnabled: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        Button(onClick = onClick,
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(all = dimensionResource(id = R.dimen.spacingMd)),
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(MaterialTheme.shapes.small)
                .background(
                    brush = if (enabled) {
                        if (uiEnabled) {
                            Brush.linearGradient(
                                0f to MaterialTheme.colorScheme.primary, 1f to Color(0xFF758F3D)
                            )
                        } else {
                            Brush.linearGradient(
                                0f to MaterialTheme.colorScheme.secondary,
                                1f to MaterialTheme.colorScheme.secondary
                            )
                        }
                    } else Brush.linearGradient(
                        0f to MaterialTheme.colorScheme.secondary,
                        1f to MaterialTheme.colorScheme.secondary
                    )
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, disabledContainerColor = Color.Transparent
            ),
            enabled = enabled,
            interactionSource = if (!uiEnabled) remember {
                NoRippleInteractionSource()
            } else remember {
                MutableInteractionSource()
            }) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingImageResource != null) {
                    Image(
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen.spacingLg))
                            .height(dimensionResource(id = R.dimen.spacingLg)),
                        painter = painterResource(id = (if (!uiEnabled && leadingDisabledImageResource != null) leadingDisabledImageResource else leadingImageResource)),
                        contentDescription = if (leadingImageContentDescriptionResource == null) null else stringResource(
                            id = leadingImageContentDescriptionResource
                        )
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacingXs)))
                }

                Text(
                    text = stringResource(id = textResource),
                    modifier = Modifier.align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center,
                    color = if (!uiEnabled) MaterialTheme.colorScheme.onBackground else textColor
                        ?: MaterialTheme.colorScheme.background,
                    style = textStyle ?: MaterialTheme.typography.labelMedium,
                    fontWeight = textFontWeight ?: FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (trailingImageResource != null) {
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacingXs)))
                    Image(
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen.spacingLg))
                            .height(dimensionResource(id = R.dimen.spacingLg)),
                        painter = painterResource(id = (if (!uiEnabled && trailingDisabledImageResource != null) trailingDisabledImageResource else trailingImageResource)),
                        contentDescription = if (trailingImageContentDescriptionResource == null) null else stringResource(
                            id = trailingImageContentDescriptionResource
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SecondaryButton(
    modifier: Modifier = Modifier,
    textResource: Int,
    textColor: Color? = null,
    textStyle: TextStyle? = null,
    textFontWeight: FontWeight? = null,
    leadingImageResource: Int? = null,
    leadingImageContentDescriptionResource: Int? = null,
    trailingImageResource: Int? = null,
    trailingImageContentDescriptionResource: Int? = null,
    onClick: () -> Unit
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        Button(
            onClick = onClick,
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(all = dimensionResource(id = R.dimen.spacingMd)),
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingImageResource != null) {
                    Image(
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen.spacingLg))
                            .height(dimensionResource(id = R.dimen.spacingLg)),
                        painter = painterResource(id = leadingImageResource),
                        contentDescription = if (leadingImageContentDescriptionResource == null) null else stringResource(
                            id = leadingImageContentDescriptionResource
                        )
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacingXs)))
                }

                Text(
                    text = stringResource(id = textResource),
                    modifier = Modifier.align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center,
                    color = textColor ?: MaterialTheme.colorScheme.tertiary,
                    style = textStyle ?: MaterialTheme.typography.labelMedium,
                    fontWeight = textFontWeight ?: FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (trailingImageResource != null) {
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacingXs)))
                    Image(
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen.spacingLg))
                            .height(dimensionResource(id = R.dimen.spacingLg)),
                        painter = painterResource(id = trailingImageResource),
                        contentDescription = if (trailingImageContentDescriptionResource == null) null else stringResource(
                            id = trailingImageContentDescriptionResource
                        )
                    )
                }
            }
        }
    }
}

class NoRippleInteractionSource : MutableInteractionSource {

    override val interactions: Flow<Interaction> = emptyFlow()

    override suspend fun emit(interaction: Interaction) {}

    override fun tryEmit(interaction: Interaction) = true
}