@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.splicr.app.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.splicr.app.R
import com.splicr.app.data.CanvasItemData
import com.splicr.app.ui.components.AppNameText
import com.splicr.app.ui.components.CustomBottomSheet
import com.splicr.app.ui.components.CustomSnackBar
import com.splicr.app.ui.components.CustomTopNavigationBar
import com.splicr.app.ui.components.PremiumText
import com.splicr.app.ui.components.PrimaryButton
import com.splicr.app.ui.components.ThumbnailImage
import com.splicr.app.ui.theme.SplicrTheme
import com.splicr.app.utils.MediaConfigurationUtil.convertDimensionsToAspectRatio
import com.splicr.app.utils.MediaConfigurationUtil.exportVideo
import com.splicr.app.utils.MediaConfigurationUtil.formatFileSize
import com.splicr.app.utils.MediaConfigurationUtil.getAllVideoMetadata
import com.splicr.app.utils.MediaConfigurationUtil.getOutputFilePath
import com.splicr.app.viewModel.MediaSplicedViewModel
import com.splicr.app.viewModel.SubscriptionStatus
import com.splicr.app.viewModel.SubscriptionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("DiscouragedApi")
@Composable
fun MediaSplicedScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
    navController: NavHostController,
    canvasItemData: CanvasItemData = CanvasItemData(),
    videoUriString: String = "",
    source: String = "",
    currentPosition: Long = 0,
    isPlaying: Boolean = false,
    mediaSplicedViewModel: MediaSplicedViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel()
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
                val filePath = remember {
                    mutableStateOf("")
                }

                LaunchedEffect(Unit) {
                    if (File(context.cacheDir, "temp_url_video.mp4").exists()) {
                        File(context.cacheDir, "temp_url_video.mp4").delete()
                    }
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
                        startOnClick = {
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                "currentPosition", currentPosition
                            )
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                "isPlaying", isPlaying
                            )
                            navController.popBackStack()
                        },
                        centerComposable = { AppNameText(modifier = Modifier.align(Alignment.Center)) },
                        endStringResource = R.string.done,
                        endOnClick = {
                            if (File(context.cacheDir, "temp_url_video.mp4").exists()) {
                                File(context.cacheDir, "temp_url_video.mp4").delete()
                            }
                            if (File(context.cacheDir, "temp_file_video.mp4").exists()) {
                                File(context.cacheDir, "temp_file_video.mp4").delete()
                            }
                            navController.popBackStack(
                                route = "HomeScreen/${false}", inclusive = false
                            )
                        })

                    val view = LocalView.current
                    val subscriptionStatus =
                        subscriptionViewModel.subscriptionStatus.observeAsState(initial = SubscriptionStatus.NONE)
                    val selectedItemIndex = rememberSaveable {
                        mutableIntStateOf(value = 0)
                    }
                    val sheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true,
                        confirmValueChange = { false })
                    val showExportingMediumBottomSheet = rememberSaveable {
                        mutableStateOf(false)
                    }
                    val showSavedMediumBottomSheet = rememberSaveable {
                        mutableStateOf(false)
                    }
                    val loaderDescription = rememberSaveable {
                        mutableIntStateOf(R.string.saving_your_medium_to_your_device_thank_you_for_your_patience)
                    }
                    val thumbnailBitmap = remember(videoUriString) {
                        mutableStateOf<Bitmap?>(null)
                    }

                    LaunchedEffect(videoUriString) {
                        if (canvasItemData.thumbnailUrl.isEmpty() && thumbnailBitmap.value == null) {
                            val bitmap = getAllVideoMetadata(
                                context = context, videoUri = Uri.parse(videoUriString)
                            )?.thumbnail

                            if (bitmap != null) {
                                thumbnailBitmap.value = bitmap
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(weight = 1f)
                            .verticalScroll(state = rememberScrollState())
                    ) {

                        CustomBottomSheet(
                            label = R.string.exporting_medium,
                            showBottomSheet = showExportingMediumBottomSheet,
                            isDarkTheme = isDarkTheme,
                            navController = navController,
                            sheetState = sheetState,
                            scope = scope,
                            loaderDescription = loaderDescription.intValue
                        )

                        CustomBottomSheet(
                            label = if (Firebase.auth.currentUser != null && source != "HomeScreen") R.string.saved_to_your_device_and_your_account else R.string.saved_to_your_device,
                            showBottomSheet = showSavedMediumBottomSheet,
                            isDarkTheme = isDarkTheme,
                            hasPerformedHapticFeedback = mediaSplicedViewModel.hasPerformedSavedMediumBottomSheetHapticFeedback,
                            navController = navController,
                            snackBarMessageResource = snackBarMessageResource,
                            snackBarHostState = snackBarHostState,
                            filePath = filePath.value,
                            canvasItemData = canvasItemData,
                            thumbnailBitmap = thumbnailBitmap.value,
                            scope = scope
                        )

                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 28.dp),
                            text = canvasItemData.title.ifEmpty { stringResource(id = R.string.unnamed) },
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        val aspectRatioTypeResource = remember {
                            context.resources.getIdentifier(
                                canvasItemData.aspectRatioTypeKey.lowercase(),
                                "string",
                                context.packageName
                            )
                        }

                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = dimensionResource(id = R.dimen.spacingXxxs)),
                            text = "${stringResource(id = aspectRatioTypeResource)} (${
                                convertDimensionsToAspectRatio(
                                    context = context,
                                    width = canvasItemData.aspectRatioWidth,
                                    height = canvasItemData.aspectRatioHeight
                                )
                            })",
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = dimensionResource(id = R.dimen.spacingXxxs)),
                            text = formatFileSize(canvasItemData.size),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )

                        ThumbnailImage(
                            thumbnailBitmap = thumbnailBitmap.value,
                            canvasItemData = canvasItemData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .padding(vertical = dimensionResource(id = R.dimen.spacingXl))
                                .clip(shape = MaterialTheme.shapes.extraSmall)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = MaterialTheme.shapes.extraSmall
                                )
                        )

                        Text(
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(top = dimensionResource(id = R.dimen.spacingXxxs)),
                            text = stringResource(R.string.export_quality),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Start
                        )

                        Column(
                            Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(top = dimensionResource(id = R.dimen.spacingXl)),
                            verticalArrangement = Arrangement.spacedBy(space = dimensionResource(id = R.dimen.spacingMd))
                        ) {
                            repeat(2) { index ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .background(color = Color.Transparent),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(Modifier
                                        .weight(1f)
                                        .wrapContentHeight()
                                        .padding(
                                            end = if (index == 1 && subscriptionStatus.value == SubscriptionStatus.NONE) dimensionResource(
                                                id = R.dimen.spacingXs
                                            ) else 0.dp
                                        )
                                        .clickable(interactionSource = remember {
                                            MutableInteractionSource()
                                        }, indication = null) {
                                            selectedItemIndex.intValue = index
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        }, horizontalArrangement = Arrangement.spacedBy(
                                        space = dimensionResource(
                                            id = R.dimen.spacingXs
                                        )
                                    ), verticalAlignment = Alignment.CenterVertically
                                    ) {
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
                                            modifier = Modifier.wrapContentSize(),
                                            text = if (index == 0) stringResource(R.string.normal_720p) else stringResource(
                                                R.string.ultra_4k
                                            ),
                                            color = MaterialTheme.colorScheme.tertiary,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal,
                                            textAlign = TextAlign.Start
                                        )
                                    }

                                    if (index == 1 && subscriptionStatus.value == SubscriptionStatus.NONE) {
                                        PremiumText(modifier = Modifier)
                                    }
                                }
                            }
                        }
                    }

                    PrimaryButton(
                        modifier = Modifier.padding(
                            top = dimensionResource(id = R.dimen.spacingMd)
                        ), textResource = if (selectedItemIndex.intValue == 1) {
                            if (subscriptionStatus.value != SubscriptionStatus.NONE) {
                                R.string.export
                            } else {
                                R.string.get_premium
                            }
                        } else {
                            R.string.export
                        }
                    ) {
                        val path = getOutputFilePath(
                            context = context,
                            filename = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(
                                Date()
                            )
                        )
                        filePath.value = "$path.mp4"
                        if (selectedItemIndex.intValue == 1) {
                            if (subscriptionStatus.value != SubscriptionStatus.NONE) {
                                export(
                                    context = context,
                                    resolution = "4k",
                                    inputUri = Uri.parse(canvasItemData.url.ifEmpty { videoUriString }),
                                    outputFilePath = filePath.value,
                                    thumbnailPath = "$path.jpg",
                                    showSavedMediumBottomSheet = showSavedMediumBottomSheet,
                                    showExportingMediumBottomSheet = showExportingMediumBottomSheet,
                                    scope = scope,
                                    snackBarMessage = snackBarMessage,
                                    snackBarIsError = snackBarIsError,
                                    snackBarHostState = snackBarHostState,
                                    snackBarMessageResource = snackBarMessageResource,
                                    source = source,
                                    loaderDescription = loaderDescription,
                                    canvasItemData = canvasItemData,
                                    thumbnailBitmap = thumbnailBitmap.value
                                )
                            } else {
                                navController.navigate("ManageSubscriptionScreen")
                            }
                        } else {
                            export(
                                context = context,
                                resolution = "720p",
                                inputUri = Uri.parse(canvasItemData.url.ifEmpty { videoUriString }),
                                outputFilePath = filePath.value,
                                thumbnailPath = "$path.jpg",
                                showSavedMediumBottomSheet = showSavedMediumBottomSheet,
                                showExportingMediumBottomSheet = showExportingMediumBottomSheet,
                                scope = scope,
                                snackBarMessage = snackBarMessage,
                                snackBarIsError = snackBarIsError,
                                snackBarHostState = snackBarHostState,
                                snackBarMessageResource = snackBarMessageResource,
                                source = source,
                                loaderDescription = loaderDescription,
                                canvasItemData = canvasItemData,
                                thumbnailBitmap = thumbnailBitmap.value
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

fun export(
    context: Context,
    resolution: String,
    inputUri: Uri,
    outputFilePath: String,
    thumbnailPath: String,
    showSavedMediumBottomSheet: MutableState<Boolean>,
    showExportingMediumBottomSheet: MutableState<Boolean>,
    scope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    snackBarMessageResource: MutableIntState,
    snackBarMessage: MutableState<String>,
    snackBarIsError: MutableState<Boolean>,
    source: String,
    loaderDescription: MutableIntState,
    canvasItemData: CanvasItemData,
    thumbnailBitmap: Bitmap?
) {
    loaderDescription.intValue =
        R.string.saving_your_medium_to_your_device_thank_you_for_your_patience
    showExportingMediumBottomSheet.value = true
    exportVideo(context = context,
        source = source,
        inputUri = inputUri,
        outputFilePath = outputFilePath,
        thumbnailPath = thumbnailPath,
        resolution = resolution,
        loaderDescription = loaderDescription,
        canvasItemData = canvasItemData,
        thumbnailBitmap = thumbnailBitmap,
        onCompletion = { success, errorMessageResource ->
            showExportingMediumBottomSheet.value = false
            if (success) {
                showSavedMediumBottomSheet.value = true
            } else {
                if (errorMessageResource != null) {
                    snackBarIsError.value = true
                    snackBarMessageResource.intValue = errorMessageResource
                    snackBarMessage.value = ""
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            ""
                        )
                    }
                }
            }
        })
}

@Composable
@PreviewLightDark
fun MediaSplicedScreenPreview() {
    MediaSplicedScreen(navController = rememberNavController())
}