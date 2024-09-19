package com.splicr.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CustomTopNavigationBar(
    modifier: Modifier = Modifier,
    startImageResource: Int? = null,
    startStringResource: Int? = null,
    startOnClick: () -> Unit = { },
    startComposable: (@Composable (BoxScope.() -> Unit))? = null,
    centerStringResource: Int? = null,
    centerComposable: (@Composable (BoxScope.() -> Unit))? = null,
    endImageResource: Int? = null,
    endStringResource: Int? = null,
    endOnClick: () -> Unit = { },
    endComposable: (@Composable (BoxScope.() -> Unit))? = null
) {

    Box(
        modifier = modifier
    ) {
        if (startImageResource != null) {
            Image(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { startOnClick() },
                painter = painterResource(id = startImageResource),
                contentDescription = if (startStringResource != null) stringResource(
                    id = startStringResource
                ) else null
            )
        } else if (startStringResource != null) {
            Text(modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable(interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null) {
                    startOnClick()
                },
                text = stringResource(id = startStringResource),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal
            )
        } else if (startComposable != null) {
            startComposable()
        }

        if (centerStringResource != null) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = stringResource(id = centerStringResource),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        } else if (centerComposable != null) {
            centerComposable()
        }

        if (endImageResource != null) {
            Image(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { endOnClick() },
                painter = painterResource(id = endImageResource),
                contentDescription = if (endStringResource != null) stringResource(
                    id = endStringResource
                ) else null
            )
        } else if (endStringResource != null) {
            Text(modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable(interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null) {
                    endOnClick()
                },
                text = stringResource(id = endStringResource),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal
            )
        } else if (endComposable != null) {
            endComposable()
        }
    }

}