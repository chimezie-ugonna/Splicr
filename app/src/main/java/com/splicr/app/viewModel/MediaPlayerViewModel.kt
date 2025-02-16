package com.splicr.app.viewModel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MediaPlayerViewModel(application: Application) : AndroidViewModel(application) {
    val exoPlayer = ExoPlayer.Builder(application.applicationContext).build()
    val isPlaying = mutableStateOf(false)
    val currentPosition = mutableLongStateOf(0L)
    val duration = mutableLongStateOf(0L)
    val controlsVisible = mutableStateOf(true)
    val isFullscreen = mutableStateOf(false)

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                viewModelScope.launch {
                    while (duration.longValue <= 0) {
                        if (exoPlayer.duration != C.TIME_UNSET) {
                            duration.longValue = exoPlayer.duration
                            break
                        }
                        delay(500)
                    }
                }
            } else if (playbackState == Player.STATE_ENDED) {
                isPlaying.value = false
                currentPosition.longValue = duration.longValue
                controlsVisible.value = true
            }
        }

        override fun onIsPlayingChanged(isCurrentlyPlaying: Boolean) {
            isPlaying.value = isCurrentlyPlaying
            if (isCurrentlyPlaying) {
                viewModelScope.launch {
                    while (isPlaying.value) {
                        currentPosition.longValue = exoPlayer.currentPosition
                        delay(1000)
                    }
                }
            }
        }
    }

    init {
        exoPlayer.addListener(listener)
    }

    fun loadVideo(videoUri: Uri) {
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.removeListener(listener)
        exoPlayer.release()
    }
}