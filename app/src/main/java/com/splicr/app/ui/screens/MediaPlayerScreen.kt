package com.splicr.app.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.gson.Gson
import com.splicr.app.R
import com.splicr.app.data.CanvasItemData
import com.splicr.app.ui.components.AppNameText
import com.splicr.app.ui.components.CustomTopNavigationBar
import com.splicr.app.ui.theme.SplicrTheme
import com.splicr.app.utils.MediaConfigurationUtil.formatDuration
import com.splicr.app.viewModel.MediaPlayerViewModel
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun MediaPlayerScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
    navController: NavHostController,
    videoUriString: String = "",
    showDone: Boolean = true,
    mediaPlayerViewModel: MediaPlayerViewModel = viewModel(),
    canvasItemData: CanvasItemData = CanvasItemData()
) {
    SplicrTheme(isSystemInDarkTheme = isDarkTheme.value) {
        val context = LocalContext.current
        val activity = LocalActivity.current
        LaunchedEffect(mediaPlayerViewModel.isFullscreen.value) {
            toggleOrientation(
                activity = activity, isLandscape = mediaPlayerViewModel.isFullscreen.value
            )
        }
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .clickable(interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null) {
                    mediaPlayerViewModel.controlsVisible.value =
                        !mediaPlayerViewModel.controlsVisible.value
                }) {

                val lifecycleOwner = rememberUpdatedState(newValue = LocalLifecycleOwner.current)
                LaunchedEffect(videoUriString) {
                    if (mediaPlayerViewModel.exoPlayer.mediaItemCount == 0) {
                        mediaPlayerViewModel.loadVideo(Uri.parse(videoUriString))
                    }
                }
                LaunchedEffect(key1 = showDone) {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "returnedFromProcessing", showDone
                    )
                }

                DisposableEffect(Unit) {
                    val lifecycleObserver = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_PAUSE -> {
                                if (!(context as Activity).isChangingConfigurations) {
                                    mediaPlayerViewModel.exoPlayer.pause()
                                }
                            }

                            else -> {}
                        }
                    }

                    lifecycleOwner.value.lifecycle.addObserver(lifecycleObserver)

                    onDispose {
                        lifecycleOwner.value.lifecycle.removeObserver(lifecycleObserver)
                    }
                }

                LaunchedEffect(
                    mediaPlayerViewModel.controlsVisible.value, mediaPlayerViewModel.isPlaying.value
                ) {
                    if (mediaPlayerViewModel.controlsVisible.value && mediaPlayerViewModel.isPlaying.value) {
                        delay(3000)
                        mediaPlayerViewModel.controlsVisible.value = false
                    }
                }

                AndroidView(
                    factory = {
                        PlayerView(context).apply {
                            player = mediaPlayerViewModel.exoPlayer
                            useController = false
                        }
                    }, modifier = Modifier.fillMaxSize()
                )

                AnimatedVisibility(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.TopCenter),
                    visible = mediaPlayerViewModel.controlsVisible.value,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    CustomTopNavigationBar(modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                0f to MaterialTheme.colorScheme.surface.copy(
                                    alpha = 0.3f
                                ), 1f to MaterialTheme.colorScheme.surface.copy(
                                    alpha = 0.7f
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(
                            all = dimensionResource(id = R.dimen.spacingXl)
                        ),
                        startImageResource = R.drawable.back,
                        startStringResource = R.string.go_back,
                        startOnClick = { navController.popBackStack() },
                        centerComposable = { AppNameText(modifier = Modifier.align(Alignment.Center)) },
                        endStringResource = if (showDone) R.string.done else null,
                        endOnClick = if (showDone) {
                            {
                                navController.navigate(
                                    route = "${if (Firebase.auth.currentUser != null) "NameYourProjectScreen" else "MediaSplicedScreen"}/${
                                        Uri.encode(
                                            Gson().toJson(
                                                canvasItemData
                                            )
                                        )
                                    }/${
                                        Uri.encode(
                                            videoUriString
                                        )
                                    }${if (Firebase.auth.currentUser != null) "" else "/MediaPlayerScreen"}"
                                )
                            }
                        } else {
                            {}
                        })
                }

                AnimatedVisibility(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.BottomCenter),
                    visible = mediaPlayerViewModel.controlsVisible.value,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    0f to MaterialTheme.colorScheme.surface.copy(
                                        alpha = 0.3f
                                    ), 1f to MaterialTheme.colorScheme.surface.copy(
                                        alpha = 0.7f
                                    )
                                )
                            )
                            .navigationBarsPadding()
                            .padding(all = dimensionResource(id = R.dimen.spacingXl)),
                        verticalArrangement = Arrangement.spacedBy(
                            space = dimensionResource(
                                id = R.dimen.spacingMd
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {

                            Box(modifier = Modifier
                                .size(size = 32.dp)
                                .clip(CircleShape)
                                .clickable {
                                    mediaPlayerViewModel.currentPosition.longValue =
                                        (mediaPlayerViewModel.currentPosition.longValue - 10000).coerceAtLeast(
                                            0
                                        )
                                    mediaPlayerViewModel.exoPlayer.seekTo(
                                        mediaPlayerViewModel.currentPosition.longValue
                                    )
                                }
                                .background(color = MaterialTheme.colorScheme.surface)
                                .padding(all = dimensionResource(id = R.dimen.spacingXxxs))) {
                                Image(
                                    modifier = Modifier
                                        .size(size = dimensionResource(id = R.dimen.spacingMd))
                                        .align(Alignment.Center),
                                    painter = painterResource(id = R.drawable.fast_rewind),
                                    contentDescription = stringResource(R.string.fast_rewind)
                                )
                            }

                            Box(modifier = Modifier
                                .size(size = 32.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (mediaPlayerViewModel.exoPlayer.playbackState == Player.STATE_ENDED) {
                                        mediaPlayerViewModel.exoPlayer.seekTo(0)
                                    }
                                    if (mediaPlayerViewModel.isPlaying.value) {
                                        mediaPlayerViewModel.exoPlayer.pause()
                                        mediaPlayerViewModel.isPlaying.value = false
                                    } else {
                                        mediaPlayerViewModel.exoPlayer.play()
                                        mediaPlayerViewModel.isPlaying.value = true
                                    }
                                }
                                .background(color = MaterialTheme.colorScheme.surface)
                                .padding(all = dimensionResource(id = R.dimen.spacingXxxs))) {
                                Image(
                                    modifier = Modifier
                                        .size(size = dimensionResource(id = R.dimen.spacingMd))
                                        .align(Alignment.Center), painter = painterResource(
                                        id = if (mediaPlayerViewModel.isPlaying.value) {
                                            R.drawable.pause
                                        } else {
                                            R.drawable.play
                                        }
                                    ), contentDescription = stringResource(R.string.play_medium)
                                )
                            }

                            Box(modifier = Modifier
                                .size(size = 32.dp)
                                .clip(CircleShape)
                                .clickable {
                                    mediaPlayerViewModel.currentPosition.longValue =
                                        (mediaPlayerViewModel.currentPosition.longValue + 10000).coerceAtMost(
                                            mediaPlayerViewModel.duration.longValue
                                        )
                                    mediaPlayerViewModel.exoPlayer.seekTo(
                                        mediaPlayerViewModel.currentPosition.longValue
                                    )
                                }
                                .background(color = MaterialTheme.colorScheme.surface)
                                .padding(all = dimensionResource(id = R.dimen.spacingXxxs))) {
                                Image(
                                    modifier = Modifier
                                        .size(size = dimensionResource(id = R.dimen.spacingMd))
                                        .align(Alignment.Center),
                                    painter = painterResource(id = R.drawable.fast_forward),
                                    contentDescription = stringResource(R.string.fast_forward)
                                )
                            }
                        }
                        if (mediaPlayerViewModel.duration.longValue > 0) {
                            Slider(
                                value = mediaPlayerViewModel.currentPosition.longValue.toFloat(),
                                onValueChange = {
                                    mediaPlayerViewModel.exoPlayer.seekTo(it.toLong())
                                    mediaPlayerViewModel.currentPosition.longValue = it.toLong()
                                },
                                valueRange = 0f..mediaPlayerViewModel.duration.longValue.toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.tertiary
                                )
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(
                                            formatDuration(
                                                durationMillis = mediaPlayerViewModel.currentPosition.longValue,
                                                shouldBeInFullFormat = false
                                            )
                                        )
                                    }
                                    append(" / ")
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                                        append(
                                            formatDuration(
                                                durationMillis = mediaPlayerViewModel.duration.longValue,
                                                shouldBeInFullFormat = false
                                            )
                                        )
                                    }
                                },
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp
                            )

                            Spacer(modifier = Modifier.weight(weight = 1f))

                            Image(modifier = Modifier
                                .size(size = dimensionResource(id = R.dimen.spacingMd))
                                .clickable(interactionSource = remember {
                                    MutableInteractionSource()
                                }, indication = null) {
                                    mediaPlayerViewModel.isFullscreen.value =
                                        !mediaPlayerViewModel.isFullscreen.value
                                },
                                painter = painterResource(id = R.drawable.fullscreen),
                                contentDescription = stringResource(R.string.enter_fullscreen)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun toggleOrientation(activity: Activity?, isLandscape: Boolean) {
    activity?.requestedOrientation = if (isLandscape) {
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    } else {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}

@Composable
@PreviewLightDark
fun MediaPlayerScreenPreview() {
    MediaPlayerScreen(navController = rememberNavController())
}