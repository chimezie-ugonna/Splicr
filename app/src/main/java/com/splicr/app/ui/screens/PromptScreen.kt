@file:OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)

package com.splicr.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.splicr.app.R
import com.splicr.app.data.PromptItemData
import com.splicr.app.ui.components.AppNameText
import com.splicr.app.ui.components.CustomBottomSheet
import com.splicr.app.ui.components.CustomSnackBar
import com.splicr.app.ui.components.CustomTextField
import com.splicr.app.ui.components.CustomTopNavigationBar
import com.splicr.app.ui.components.PromptItem
import com.splicr.app.ui.theme.SplicrTheme
import com.splicr.app.utils.SplicrBrainUtil.converse
import com.splicr.app.viewModel.PromptViewModel
import com.splicr.app.viewModel.SubscriptionStatus
import com.splicr.app.viewModel.SubscriptionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PromptScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
    navController: NavHostController,
    uploadFormatStringResource: Int = R.string.device_upload,
    videoUriString: String = "",
    promptViewModel: PromptViewModel = viewModel(),
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
                val snackBarMessageResource = remember {
                    mutableIntStateOf(0)
                }
                val snackBarMessage = remember {
                    mutableStateOf("")
                }
                val snackBarIsError = remember {
                    mutableStateOf(true)
                }
                val scope = rememberCoroutineScope()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = 72.dp
                        )
                        .imePadding()
                ) {
                    val showBottomSheet = rememberSaveable {
                        mutableStateOf(false)
                    }
                    val subscriptionStatus =
                        subscriptionViewModel.subscriptionStatus.observeAsState(initial = SubscriptionStatus.NONE)
                    CustomBottomSheet(
                        label = R.string.voice_prompt,
                        showBottomSheet = showBottomSheet,
                        isDarkTheme = isDarkTheme,
                        hasPerformedHapticFeedback = promptViewModel.hasPerformedBottomSheetHapticFeedback,
                        navController = navController
                    )

                    CustomTopNavigationBar(modifier = Modifier.fillMaxWidth(),
                        startImageResource = R.drawable.back,
                        startStringResource = R.string.go_back,
                        startOnClick = { navController.popBackStack() },
                        centerComposable = { AppNameText(modifier = Modifier.align(Alignment.Center)) })

                    promptViewModel.updateListItems(
                        uploadFormatStringResource = uploadFormatStringResource,
                        videoUri = videoUriString,
                        context = context
                    )

                    val listState = rememberLazyListState()
                    val isScrolledToBottom by remember {
                        derivedStateOf {
                            promptViewModel.listItems.isEmpty() || (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == promptViewModel.listItems.size - 1)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = dimensionResource(id = R.dimen.spacingSm)),
                            verticalArrangement = Arrangement.spacedBy(
                                space = dimensionResource(
                                    id = R.dimen.spacingSm
                                )
                            )
                        ) {
                            itemsIndexed(promptViewModel.listItems) { index, item ->
                                PromptItem(
                                    modifier = when (index) {
                                        0 -> {
                                            Modifier.padding(
                                                top = dimensionResource(
                                                    id = R.dimen.spacingSm
                                                )
                                            )
                                        }

                                        promptViewModel.listItems.size - 1 -> {
                                            Modifier.padding(
                                                bottom = dimensionResource(
                                                    id = R.dimen.spacingSm
                                                )
                                            )
                                        }

                                        else -> {
                                            Modifier
                                        }
                                    },
                                    isAuthor = item.isAuthor,
                                    message = item.message,
                                    showCanvasOptions = item.showCanvasOptions,
                                    videoUriString = item.videoUriString,
                                    isLoading = item.isLoading,
                                    navController = navController,
                                    trimRanges = item.trimRanges,
                                    isProcessing = item.isProcessing,
                                    listState = listState,
                                    viewModel = item.viewModel,
                                    thumbnailBitmap = item.thumbnailBitmap,
                                    aspectRatioChoiceList = item.canvasChoiceList,
                                    duration = item.duration,
                                    onClick = item.onClick
                                )
                            }
                        }

                        if (!isScrolledToBottom) {
                            Box(modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = dimensionResource(id = R.dimen.spacingXl))
                                .clip(CircleShape)
                                .clickable {
                                    scope.launch {
                                        listState.animateScrollToItem(promptViewModel.listItems.size - 1)
                                    }
                                }
                                .background(color = MaterialTheme.colorScheme.primary)
                                .padding(all = dimensionResource(id = R.dimen.spacingSm))) {
                                Image(
                                    modifier = Modifier.align(Alignment.Center),
                                    painter = painterResource(id = R.drawable.arrow_down),
                                    contentDescription = stringResource(
                                        R.string.scroll_to_the_bottom
                                    )
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(vertical = dimensionResource(id = R.dimen.spacingSm)),
                        horizontalArrangement = Arrangement.spacedBy(
                            space = dimensionResource(
                                id = R.dimen.spacingSm
                            )
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isProcessing = rememberSaveable {
                            mutableStateOf(false)
                        }
                        val isRecording = rememberSaveable { mutableStateOf(false) }
                        val lifecycleOwner = LocalLifecycleOwner.current
                        val hapticFeedback = LocalHapticFeedback.current

                        val speechRecognizer = remember {
                            SpeechRecognizer.createSpeechRecognizer(context)
                        }
                        val readOnly = rememberSaveable {
                            mutableStateOf(false)
                        }
                        val isBlinking = rememberSaveable { mutableStateOf(false) }
                        val promptFocusRequester = remember {
                            FocusRequester()
                        }
                        LaunchedEffect(Unit) {
                            delay(100)
                            promptFocusRequester.requestFocus()
                        }


                        DisposableEffect(key1 = lifecycleOwner) {
                            val observer = LifecycleEventObserver { _, event ->
                                if (event == Lifecycle.Event.ON_DESTROY) {
                                    speechRecognizer.destroy()
                                }
                            }

                            lifecycleOwner.lifecycle.addObserver(observer)

                            onDispose {
                                lifecycleOwner.lifecycle.removeObserver(observer)
                            }
                        }

                        val intent = remember {
                            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                )
                                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                            }
                        }

                        val recognitionListener = remember {
                            object : RecognitionListener {
                                override fun onReadyForSpeech(params: Bundle?) {}
                                override fun onBeginningOfSpeech() {}
                                override fun onRmsChanged(rmsdB: Float) {}
                                override fun onBufferReceived(buffer: ByteArray?) {}
                                override fun onEndOfSpeech() {
                                    recording(
                                        context = context,
                                        action = context.getString(R.string.stop),
                                        readOnly = readOnly,
                                        isRecording = isRecording,
                                        isBlinking = isBlinking,
                                        intent = intent,
                                        speechRecognizer = speechRecognizer,
                                        hapticFeedback = hapticFeedback
                                    )
                                }

                                override fun onError(error: Int) {}
                                override fun onResults(results: Bundle?) {}
                                override fun onPartialResults(partialResults: Bundle?) {
                                    val matches =
                                        partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                    matches?.let {
                                        val newText = it.joinToString(" ")
                                        promptViewModel.promptValue.value = TextFieldValue(
                                            newText,
                                            selection = TextRange(newText.length)  // Set the cursor at the end
                                        )
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }

                                override fun onEvent(eventType: Int, params: Bundle?) {}
                            }
                        }

                        speechRecognizer.setRecognitionListener(recognitionListener)

                        val audioPermissionState =
                            rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO) {
                                if (it) {
                                    recording(
                                        context = context,
                                        action = context.getString(R.string.start),
                                        readOnly = readOnly,
                                        isRecording = isRecording,
                                        isBlinking = isBlinking,
                                        intent = intent,
                                        speechRecognizer = speechRecognizer,
                                        hapticFeedback = hapticFeedback
                                    )
                                } else {
                                    snackBarIsError.value = true
                                    snackBarMessageResource.intValue =
                                        R.string.without_microphone_access_the_voice_recording_feature_won_t_work_please_grant_the_permission_to_use_this_feature
                                    snackBarMessage.value = ""
                                    scope.launch { snackBarHostState.showSnackbar("") }
                                }
                            }

                        val transition = rememberInfiniteTransition(label = "BlinkAnimation")
                        val primaryColor = MaterialTheme.colorScheme.primary

                        val alpha by transition.animateFloat(
                            initialValue = 1f, targetValue = 0f, animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 500),
                                repeatMode = RepeatMode.Reverse
                            ), label = "BlinkAnimation"
                        )

                        CustomTextField(
                            modifier = Modifier.weight(1f),
                            placeHolderResource = R.string.enter_prompt,
                            maxHeight = 120.dp,
                            value = promptViewModel.promptValue,
                            initialShape = MaterialTheme.shapes.extraLarge,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            readOnly = readOnly,
                            singleLine = false,
                            placeHolderMaxLines = Int.MAX_VALUE,
                            focusRequester = promptFocusRequester
                        )

                        if (isBlinking.value) {
                            Box(modifier = Modifier
                                .size(size = dimensionResource(id = R.dimen.spacingXl))
                                .clickable {
                                    recording(
                                        context = context,
                                        action = context.getString(R.string.stop),
                                        readOnly = readOnly,
                                        isRecording = isRecording,
                                        isBlinking = isBlinking,
                                        intent = intent,
                                        speechRecognizer = speechRecognizer,
                                        hapticFeedback = hapticFeedback
                                    )
                                }
                                .background(
                                    MaterialTheme.colorScheme.onBackground, shape = CircleShape
                                ), contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(size = dimensionResource(id = R.dimen.spacingSm))
                                        .background(
                                            MaterialTheme.colorScheme.error.copy(alpha = if (isBlinking.value) alpha else 1f),
                                            shape = MaterialTheme.shapes.extraSmall
                                        )
                                )
                            }
                        } else {
                            Image(modifier = Modifier
                                .size(size = dimensionResource(id = R.dimen.spacingXl))
                                .alpha(if (!isProcessing.value) 1f else 0.3f)
                                .then(if (!isProcessing.value) {
                                    Modifier.clickable(
                                        interactionSource = remember {
                                            MutableInteractionSource()
                                        }, indication = null
                                    ) {
                                        if (promptViewModel.promptValue.value.text.isEmpty()) {
                                            if (subscriptionStatus.value == SubscriptionStatus.NONE) {
                                                showBottomSheet.value = true
                                            } else {
                                                when {
                                                    audioPermissionState.status.isGranted -> {
                                                        recording(
                                                            context = context,
                                                            action = context.getString(R.string.start),
                                                            readOnly = readOnly,
                                                            isRecording = isRecording,
                                                            isBlinking = isBlinking,
                                                            intent = intent,
                                                            speechRecognizer = speechRecognizer,
                                                            hapticFeedback = hapticFeedback
                                                        )
                                                    }

                                                    audioPermissionState.status.shouldShowRationale -> {
                                                        snackBarIsError.value = true
                                                        snackBarMessageResource.intValue =
                                                            R.string.we_need_access_to_your_microphone_to_enable_voice_recording_please_grant_the_permission_to_use_this_feature
                                                        snackBarMessage.value = ""
                                                        scope.launch {
                                                            snackBarHostState.showSnackbar(
                                                                ""
                                                            )
                                                        }
                                                    }

                                                    else -> {
                                                        audioPermissionState.launchPermissionRequest()
                                                    }
                                                }
                                            }
                                        } else {
                                            val promptValue =
                                                promptViewModel.promptValue.value.text.trim()
                                            promptViewModel.addListItem(
                                                PromptItemData(
                                                    isAuthor = true,
                                                    message = AnnotatedString(text = promptValue)
                                                )
                                            )
                                            scope.launch {
                                                listState.animateScrollToItem(
                                                    promptViewModel.listItems.size - 1
                                                )
                                            }
                                            promptViewModel.promptValue.value = TextFieldValue("")
                                            converse(
                                                context = context,
                                                isProcessing = isProcessing,
                                                promptValue = promptValue,
                                                scope = scope,
                                                subscriptionStatus = subscriptionStatus,
                                                uploadFormatStringResource = uploadFormatStringResource,
                                                navController = navController,
                                                primaryColor = primaryColor,
                                                promptViewModel = promptViewModel,
                                                listState = listState
                                            )
                                        }
                                    }
                                } else {
                                    Modifier
                                }),
                                painter = painterResource(id = if (promptViewModel.promptValue.value.text.isNotEmpty()) R.drawable.send else R.drawable.microphone),
                                contentDescription = stringResource(R.string.initiate_voice_prompt))
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

fun recording(
    context: Context,
    action: String,
    readOnly: MutableState<Boolean>,
    isBlinking: MutableState<Boolean>,
    isRecording: MutableState<Boolean>,
    hapticFeedback: HapticFeedback,
    speechRecognizer: SpeechRecognizer,
    intent: Intent
) {
    if (action == context.getString(R.string.start)) {
        readOnly.value = true
        isBlinking.value = true
        speechRecognizer.startListening(intent)
        isRecording.value = true
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    } else {
        if (isBlinking.value && isRecording.value) {
            readOnly.value = false
            isBlinking.value = false
            speechRecognizer.stopListening()
            isRecording.value = false
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}

@Composable
@PreviewLightDark
fun PromptScreenPreview() {
    PromptScreen(navController = rememberNavController())
}