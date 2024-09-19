package com.splicr.app.ui.screens

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.WindowManager
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
import com.splicr.app.R
import com.splicr.app.ui.theme.SplicrTheme
import com.splicr.app.utils.MediaConfigurationUtil.formatDuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FullScreenMediaPlayerScreen(
    isDarkTheme: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
    navController: NavHostController,
    videoUriString: String = "",
    currentPosition: Long = 0,
    isPlaying: Boolean = false,
    duration: Long = 0
) {
    SplicrTheme(darkTheme = isDarkTheme.value) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
            color = MaterialTheme.colorScheme.background
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
            val isPlaying2 = rememberSaveable { mutableStateOf(isPlaying) }
            val currentPosition2 = rememberSaveable { mutableLongStateOf(currentPosition) }
            val duration2 = rememberSaveable { mutableLongStateOf(duration) }
            val controlsVisible = rememberSaveable {
                mutableStateOf(true)
            }
            val scope = rememberCoroutineScope()

            LaunchedEffect(videoUri) {
                exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
                exoPlayer.prepare()

                // Restore playback position
                exoPlayer.seekTo(currentPosition2.longValue)

                // Control playback state
                if (isPlaying2.value) {
                    exoPlayer.play()
                } else {
                    exoPlayer.pause()
                }
            }

            DisposableEffect(Unit) {
                val window = (context as? Activity)?.window
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    window?.decorView?.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                } else {
                    // For API 30 and above, use WindowInsetsController
                    window?.setDecorFitsSystemWindows(false)
                    window?.insetsController?.hide(android.view.WindowInsets.Type.statusBars())
                }
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
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                        window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                        window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    } else {
                        window?.setDecorFitsSystemWindows(true)
                        window?.insetsController?.show(android.view.WindowInsets.Type.statusBars())
                    }
                    lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
                }
            }

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
                            isPlaying2.value = exoPlayer.isPlaying
                            if (exoPlayer.isPlaying) {
                                scope.launch {
                                    while (exoPlayer.isPlaying) {
                                        currentPosition2.longValue = exoPlayer.currentPosition
                                        duration2.longValue = exoPlayer.duration
                                        delay(1000) // Update position every second
                                    }
                                }
                            }
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == ExoPlayer.STATE_ENDED) {
                                currentPosition2.longValue = duration2.longValue
                                controlsVisible.value = true
                                isPlaying2.value = false // Update the state when playback ends
                            }
                        }
                    })
                }

                LaunchedEffect(controlsVisible.value, isPlaying2.value) {
                    if (controlsVisible.value && isPlaying2.value) {
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
                                        currentPosition2.longValue =
                                            (currentPosition2.longValue - 10000).coerceAtLeast(
                                                0
                                            )
                                        exoPlayer.seekTo(currentPosition2.longValue)
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
                                            isPlaying2.value = false
                                        } else {
                                            exoPlayer.play()
                                            isPlaying2.value = true
                                        }
                                    }
                                    .background(color = MaterialTheme.colorScheme.surface)
                                    .padding(all = dimensionResource(id = R.dimen.spacingXxxs))) {
                                    Image(
                                        modifier = Modifier
                                            .size(size = dimensionResource(id = R.dimen.spacingMd))
                                            .align(Alignment.Center), painter = painterResource(
                                            id = if (isPlaying2.value) {
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
                                        currentPosition2.longValue =
                                            (currentPosition2.longValue + 10000).coerceAtMost(
                                                duration2.longValue
                                            )
                                        exoPlayer.seekTo(currentPosition2.longValue)
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
                            if (duration2.longValue > 0) {
                                Slider(
                                    value = currentPosition2.longValue.toFloat(),
                                    onValueChange = {
                                        exoPlayer.seekTo(it.toLong())
                                        currentPosition2.longValue = it.toLong()
                                    },
                                    valueRange = 0f..duration2.longValue.toFloat(),
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
                                            append(formatDuration(durationMillis = currentPosition2.longValue, shouldBeInFullFormat = false))
                                        }
                                        append(" / ")
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                                            append(formatDuration(durationMillis = duration2.longValue, shouldBeInFullFormat = false))
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
                                        navController.previousBackStackEntry?.savedStateHandle?.set(
                                            "currentPosition", currentPosition2.longValue
                                        )
                                        navController.previousBackStackEntry?.savedStateHandle?.set(
                                            "isPlaying", isPlaying2.value
                                        )
                                        navController.popBackStack()
                                    },
                                    painter = painterResource(id = R.drawable.fullscreen_exit),
                                    contentDescription = stringResource(R.string.exit_fullscreen)
                                )
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
fun FullScreenMediaPlayerScreenPreview() {
    FullScreenMediaPlayerScreen(navController = rememberNavController())
}