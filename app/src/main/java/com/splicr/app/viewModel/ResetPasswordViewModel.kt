package com.splicr.app.viewModel

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel

class ResetPasswordViewModel : ViewModel() {
    val emailValue = mutableStateOf(TextFieldValue(""))
    val emailErrorMessageResource = mutableIntStateOf(0)
}