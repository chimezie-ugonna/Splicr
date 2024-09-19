package com.splicr.app.ui.screens

import android.content.pm.ActivityInfo
import android.net.Uri
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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
import com.splicr.app.utils.ScreenOrientationUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MediaPlayerScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
    navController: NavHostController,
    videoUriString: String = "",
    canvasItemData: CanvasItemData = CanvasItemData()
) {
    ScreenOrientationUtil.SetScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    SplicrTheme(darkTheme = isDarkTheme.value) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 72.dp
                    )
            ) {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                val videoUri = remember {
                    Uri.parse(videoUriString)
                }
                val exoPlayer = remember {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(videoUri))
                        prepare()
                        playWhenReady = true
                    }
                }
                val isPlaying = rememberSaveable { mutableStateOf(false) }
                val controlsVisible = rememberSaveable {
                    mutableStateOf(true)
                }
                val currentPosition = rememberSaveable { mutableLongStateOf(0L) }
                val currentPositionState =
                    navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Long>("currentPosition")
                        ?.observeAsState()
                val isPlayingState =
                    navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("isPlaying")
                        ?.observeAsState()

                LaunchedEffect(videoUri) {
                    // Only set up the player if the video URI changes
                    exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
                    exoPlayer.prepare()

                    // Restore playback position
                    currentPositionState?.value?.let {
                        currentPosition.longValue = it
                        exoPlayer.seekTo(currentPosition.longValue)
                    }

                    // Restore playback state
                    isPlayingState?.value?.let {
                        isPlaying.value = it
                        if (isPlaying.value) exoPlayer.play() else exoPlayer.pause()
                    }
                }

                val duration = rememberSaveable { mutableLongStateOf(0L) }
                val scope = rememberCoroutineScope()

                DisposableEffect(Unit) {
                    val lifecycleObserver = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_PAUSE -> {
                                // Pause playback when the screen is not in the foreground
                                exoPlayer.pause()
                            }

                            Lifecycle.Event.ON_DESTROY -> {
                                // Release the player when the composable is destroyed
                                exoPlayer.release()
                            }

                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            "currentPosition", currentPosition.longValue
                        )
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            "isPlaying", isPlaying.value
                        )
                    }
                }

                CustomTopNavigationBar(modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.spacingXl)
                    ),
                    startImageResource = R.drawable.back,
                    startStringResource = R.string.go_back,
                    startOnClick = { navController.popBackStack() },
                    centerComposable = { AppNameText(modifier = Modifier.align(Alignment.Center)) },
                    endStringResource = R.string.done,
                    endOnClick = {
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
                            }/MediaPlayerScreen/${
                                currentPosition.longValue
                            }/${
                                isPlaying.value
                            }"
                        )
                    })

                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = dimensionResource(
                            id = R.dimen.spacingXl
                        )
                    )
                    .clickable(interactionSource = remember {
                        MutableInteractionSource()
                    }, indication = null) {
                        controlsVisible.value = !controlsVisible.value
                    }) {

                    LaunchedEffect(exoPlayer) {
                        exoPlayer.addListener(object : Player.Listener {
                            override fun onIsPlayingChanged(state: Boolean) {
                                isPlaying.value = exoPlayer.isPlaying
                                if (exoPlayer.isPlaying) {
                                    scope.launch {
                                        while (exoPlayer.isPlaying) {
                                            currentPosition.longValue = exoPlayer.currentPosition
                                            duration.longValue = exoPlayer.duration
                                            delay(1000) // Update position every second
                                        }
                                    }
                                }
                            }

                            override fun onPlaybackStateChanged(playbackState: Int) {
                                if (playbackState == ExoPlayer.STATE_ENDED) {
                                    currentPosition.longValue = duration.longValue
                                    controlsVisible.value = true
                                    isPlaying.value = false // Update the state when playback ends
                                }
                            }
                        })
                    }

                    LaunchedEffect(controlsVisible.value, isPlaying.value) {
                        if (controlsVisible.value && isPlaying.value) {
                            delay(3000)
                            controlsVisible.value = false
                        }
                    }

                    AndroidView(
                        factory = {
                            PlayerView(context).apply {
                                player = exoPlayer
                                useController = false
                            }
                        }, modifier = Modifier.fillMaxSize()
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.BottomCenter)
                    ) {
                        AnimatedVisibility(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            visible = controlsVisible.value,
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
                                            currentPosition.longValue =
                                                (currentPosition.longValue - 10000).coerceAtLeast(
                                                    0
                                                )
                                            exoPlayer.seekTo(currentPosition.longValue)
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
                                            if (exoPlayer.playbackState == Player.STATE_ENDED) {
                                                exoPlayer.seekTo(0)
                                            }
                                            if (exoPlayer.isPlaying) {
                                                exoPlayer.pause()
                                                isPlaying.value = false
                                            } else {
                                                exoPlayer.play()
                                                isPlaying.value = true
                                            }
                                        }
                                        .background(color = MaterialTheme.colorScheme.surface)
                                        .padding(all = dimensionResource(id = R.dimen.spacingXxxs))) {
                                        Image(
                                            modifier = Modifier
                                                .size(size = dimensionResource(id = R.dimen.spacingMd))
                                                .align(Alignment.Center),
                                            painter = painterResource(
                                                id = if (isPlaying.value) {
                                                    R.drawable.pause
                                                } else {
                                                    R.drawable.play
                                                }
                                            ),
                                            contentDescription = stringResource(R.string.play_medium)
                                        )
                                    }

                                    Box(modifier = Modifier
                                        .size(size = 32.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            currentPosition.longValue =
                                                (currentPosition.longValue + 10000).coerceAtMost(
                                                    duration.longValue
                                                )
                                            exoPlayer.seekTo(currentPosition.longValue)
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
                                if (duration.longValue > 0) {
                                    Slider(
                                        value = currentPosition.longValue.toFloat(),
                                        onValueChange = {
                                            exoPlayer.seekTo(it.toLong())
                                            currentPosition.longValue = it.toLong()
                                        },
                                        valueRange = 0f..duration.longValue.toFloat(),
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
                                                        durationMillis = currentPosition.longValue,
                                                        shouldBeInFullFormat = false
                                                    )
                                                )
                                            }
                                            append(" / ")
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                                                append(
                                                    formatDuration(
                                                        durationMillis = duration.longValue,
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
                                            navController.navigate(
                                                "FullScreenMediaPlayerScreen/${
                                                    Uri.encode(
                                                        videoUriString
                                                    )
                                                }/${
                                                    currentPosition.longValue
                                                }/${
                                                    isPlaying.value
                                                }/${
                                                    duration.longValue
                                                }"
                                            )
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
    }
}

@Composable
@PreviewLightDark
fun MediaPlayerScreenPreview() {
    MediaPlayerScreen(navController = rememberNavController())
}