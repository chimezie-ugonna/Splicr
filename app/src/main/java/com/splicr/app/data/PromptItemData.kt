package com.splicr.app.data

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.AnnotatedString
import com.splicr.app.viewModel.PromptViewModel

data class PromptItemData(
    val isAuthor: Boolean = false,
    val videoUriString: String? = null,
    val thumbnailBitmap: Bitmap? = null,
    val duration: String? = null,
    val message: AnnotatedString = AnnotatedString(text = ""),
    val showCanvasOptions: Boolean = false,
    val isLoading: Boolean = false,
    val trimRanges: List<TrimRangeData>? = null,
    val isProcessing: MutableState<Boolean>? = null,
    val viewModel: PromptViewModel? = null,
    val canvasChoiceList: List<AspectRatioChoiceItemData>? = null,
    val onClick: (Int) -> Unit = {}
)
