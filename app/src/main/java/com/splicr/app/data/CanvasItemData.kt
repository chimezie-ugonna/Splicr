package com.splicr.app.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

data class CanvasItemData(
    var userId: String = Firebase.auth.currentUser?.uid ?: "",
    var id: String = "",
    var title: String = "",
    var url: String = "",
    var thumbnailUrl: String = "",
    var duration: Long = 0,
    var size: Long = 0,
    var aspectRatioTypeKey: String = "",
    var aspectRatioWidth: Int = 0,
    var aspectRatioHeight: Int = 0,
    val createdAt: Timestamp = Timestamp.now()
)
