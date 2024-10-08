package com.splicr.app.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.splicr.app.R

@Composable
fun CustomSnackBar(
    modifier: Modifier = Modifier,
    isError: Boolean = true,
    messageResource: Int,
    message: String = "",
    snackBarHostState: SnackbarHostState
) {
    SnackbarHost(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                top = WindowInsets.statusBars
                    .asPaddingValues()
                    .calculateTopPadding()
            ), hostState = snackBarHostState
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(MaterialTheme.shapes.large)
                .background(if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.inverseSurface)
                .padding(
                    all = dimensionResource(
                        id = R.dimen.spacingMd
                    )
                ), verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .width(20.dp)
                    .height(20.dp),
                painter = painterResource(id = if (isError) R.drawable.error_icon else R.drawable.checkmark_icon),
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacingXs)))

            Text(
                modifier = Modifier.weight(1f),
                text = if (messageResource != 0) stringResource(id = messageResource) else message,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start
            )

            val view = LocalView.current
            if (Build.VERSION.SDK_INT >= 30) {
                view.performHapticFeedback(if (isError) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.CONFIRM)
            }
        }
    }
}