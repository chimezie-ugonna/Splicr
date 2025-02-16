package com.splicr.app.data

import android.graphics.Bitmap

data class MediaMetadataData(
    val width: Int?,
    val height: Int?,
    val duration: Long?,
    val bitrate: Int?,
    val location: String?,
    val mimeType: String?,
    val frameRate: Float?,
    val rotation: Int?,
    val title: String?,
    val artist: String?,
    val album: String?,
    val genre: String?,
    val date: String?,
    val numTracks: Int?,
    val hasAudio: String?,
    val composer: String?,
    val discNumber: Int?,
    val writer: String?,
    val albumArtist: String?,
    val year: Int?,
    val fileSize: Long?,
    val thumbnail: Bitmap?
)
