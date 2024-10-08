package com.splicr.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.splicr.app.R
import com.splicr.app.ui.components.CustomTopNavigationBar
import com.splicr.app.ui.theme.SplicrTheme

@Composable
fun FeedbackSentScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }, navController: NavHostController
) {
    SplicrTheme(isSystemInDarkTheme = isDarkTheme.value) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
            color = MaterialTheme.colorScheme.background
        ) {

            Box(modifier = Modifier.fillMaxSize()) {

                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(
                            Alignment.Center
                        )
                        .padding(all = dimensionResource(id = R.dimen.spacingXl)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = Modifier
                            .width(150.dp)
                            .height(93.67.dp),
                        painter = painterResource(id = R.drawable.sent_mail),
                        contentDescription = null
                    )

                    Text(
                        text = stringResource(R.string.your_feedback_is_in_the_cyber_postbox_and_we_re_all_thumbs_up_for_your_input),
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(top = dimensionResource(id = R.dimen.spacingXl)),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }

                CustomTopNavigationBar(modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(
                        vertical = 72.dp, horizontal = dimensionResource(id = R.dimen.spacingXl)
                    ),
                    startImageResource = R.drawable.back,
                    startStringResource = R.string.go_back,
                    startOnClick = { navController.popBackStack() })
            }
        }
    }
}

@Composable
@PreviewLightDark
fun FeedbackSentScreenPreview() {
    FeedbackSentScreen(navController = rememberNavController())
}