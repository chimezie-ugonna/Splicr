package com.splicr.app.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MediaSplicedViewModel : ViewModel() {
    val hasPerformedSavedMediumBottomSheetHapticFeedback = mutableStateOf(false)
}