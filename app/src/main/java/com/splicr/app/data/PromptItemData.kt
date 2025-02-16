package com.splicr.app.data

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString

data class PromptItemData(
    val isAuthor: Boolean = false,
    val videoUriString: String? = null,
    val thumbnailBitmap: Bitmap? = null,
    val duration: String? = null,
    val message: AnnotatedString = AnnotatedString(text = ""),
    val showCanvasOptions: Boolean = false,
    val isLoading: Boolean = false,
    val loadingMessage: MutableState<String> = mutableStateOf(""),
    val hasTyped: MutableState<Boolean> = mutableStateOf(false),
    val trimRanges: List<TrimRangeData>? = null,
    val displayedText: MutableState<AnnotatedString> = mutableStateOf(AnnotatedString(text = "")),
    val canvasChoiceList: List<AspectRatioChoiceItemData>? = null,
    val onClick: ((Int) -> Unit)? = null
)
