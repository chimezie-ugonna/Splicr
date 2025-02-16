package com.splicr.app.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel

class NameYourProjectViewModel : ViewModel() {
    val projectNameValue = mutableStateOf(TextFieldValue(""))
}