@file:OptIn(ExperimentalPermissionsApi::class)

package com.splicr.app.ui.screens

import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.splicr.app.R
import com.splicr.app.ui.components.AppNameText
import com.splicr.app.ui.components.CustomSnackBar
import com.splicr.app.ui.components.CustomTopNavigationBar
import com.splicr.app.ui.components.PremiumText
import com.splicr.app.ui.components.PrimaryButton
import com.splicr.app.ui.theme.SplicrTheme
import com.splicr.app.viewModel.SubscriptionStatus
import com.splicr.app.viewModel.SubscriptionViewModel


@Composable
fun ChooseUploadFormatScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }, navController: NavHostController, subscriptionViewModel: SubscriptionViewModel = viewModel()
) {
    SplicrTheme(isSystemInDarkTheme = isDarkTheme.value) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = dimensionResource(
                            id = R.dimen.spacingXl
                        ), end = dimensionResource(
                            id = R.dimen.spacingXl
                        )
                    )
            ) {
                val context = LocalContext.current
                val snackBarHostState = remember {
                    SnackbarHostState()
                }
                val scope = rememberCoroutineScope()
                val snackBarMessageResource = remember {
                    mutableIntStateOf(0)
                }
                val snackBarMessage = remember {
                    mutableStateOf("")
                }
                val snackBarIsError = remember {
                    mutableStateOf(true)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = 72.dp, bottom = dimensionResource(
                                id = R.dimen.spacingXl
                            )
                        )
                ) {
                    CustomTopNavigationBar(modifier = Modifier.fillMaxWidth(),
                        startImageResource = R.drawable.back,
                        startStringResource = R.string.go_back,
                        startOnClick = { navController.popBackStack() },
                        centerComposable = { AppNameText(modifier = Modifier.align(Alignment.Center)) })

                    val topPadding = 28.dp
                    val subscriptionStatus =
                        subscriptionViewModel.subscriptionStatus.observeAsState(initial = SubscriptionStatus.NONE)
                    val view = LocalView.current
                    val selectedItemIndex = rememberSaveable {
                        mutableIntStateOf(value = 0)
                    }
                    val selectedUploadFormat = rememberSaveable {
                        mutableIntStateOf(value = R.string.device_upload)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(weight = 1f)
                            .verticalScroll(state = rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = topPadding),
                            text = stringResource(R.string.select_your_upload_format),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Column(
                            Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(top = topPadding),
                            verticalArrangement = Arrangement.spacedBy(space = dimensionResource(id = R.dimen.spacingSm))
                        ) {
                            repeat(2) { index ->
                                Row(modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .clip(shape = MaterialTheme.shapes.medium)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .background(color = Color.Transparent)
                                    .clickable {
                                        selectedItemIndex.intValue = index
                                        selectedUploadFormat.intValue =
                                            if (index == 0) R.string.device_upload else R.string.url_upload
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    }
                                    .padding(all = dimensionResource(id = R.dimen.spacingMd)),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = if (index == selectedItemIndex.intValue) Modifier
                                            .size(
                                                18.dp
                                            )
                                            .clip(CircleShape)
                                        else Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                shape = CircleShape
                                            )
                                            .background(color = Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (index == selectedItemIndex.intValue) {
                                            Image(
                                                painter = painterResource(id = R.drawable.check),
                                                contentDescription = null
                                            )
                                        }
                                    }

                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .padding(
                                                start = dimensionResource(
                                                    id = R.dimen.spacingXs
                                                ),
                                                end = if (index == 1 && subscriptionStatus.value == SubscriptionStatus.NONE) dimensionResource(
                                                    id = R.dimen.spacingXs
                                                ) else 0.dp
                                            ),
                                        text = if (index == 0) stringResource(R.string.device_upload) else stringResource(
                                            R.string.url_upload
                                        ),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        textAlign = TextAlign.Start
                                    )

                                    if (index == 1 && subscriptionStatus.value == SubscriptionStatus.NONE) {
                                        PremiumText(modifier = Modifier)
                                    }
                                }
                            }
                        }
                    }

                    val videoPickerLauncher =
                        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(),
                            onResult = { uri ->
                                if (uri != null) {
                                    navController.navigate(
                                        "PromptScreen/${selectedUploadFormat.intValue}/${
                                            Uri.encode(
                                                uri.toString()
                                            )
                                        }"
                                    )
                                }
                            })

                    PrimaryButton(
                        modifier = Modifier.padding(
                            top = dimensionResource(id = R.dimen.spacingMd)
                        ),
                        textResource = if (selectedUploadFormat.intValue == R.string.url_upload) {
                            if (subscriptionStatus.value != SubscriptionStatus.NONE) {
                                R.string.Continue
                            } else {
                                R.string.get_premium
                            }
                        } else {
                            R.string.Continue
                        }
                    ) {
                        if (selectedUploadFormat.intValue == R.string.url_upload) {
                            if (subscriptionStatus.value != SubscriptionStatus.NONE) {
                                navController.navigate(
                                    "PromptScreen/${selectedUploadFormat.intValue}/${
                                        context.getString(
                                            R.string.empty
                                        )
                                    }"
                                )
                            } else {
                                navController.navigate("ManageSubscriptionScreen")
                            }
                        } else {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.VideoOnly
                                )
                            )
                        }
                    }
                }

                CustomSnackBar(
                    messageResource = snackBarMessageResource.intValue,
                    isError = snackBarIsError.value,
                    message = snackBarMessage.value,
                    snackBarHostState = snackBarHostState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
@PreviewLightDark
fun ChooseUploadFormatScreenPreview() {
    ChooseUploadFormatScreen(navController = rememberNavController())
}