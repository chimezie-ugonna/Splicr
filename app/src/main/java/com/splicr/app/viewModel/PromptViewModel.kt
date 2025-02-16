package com.splicr.app.viewModel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splicr.app.R
import com.splicr.app.data.PromptItemData
import com.splicr.app.utils.MediaConfigurationUtil
import com.splicr.app.utils.MediaConfigurationUtil.getAllVideoMetadata
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PromptViewModel : ViewModel() {
    var previousWaitingMessageResponse: String = ""
    var waitingMessageJob: Job? = null
    var typingEffectJob: Job? = null
    val promptValue = mutableStateOf(TextFieldValue(""))
    var mutableVideoUriString by mutableStateOf("")
        private set
    val hasPerformedBottomSheetHapticFeedback = mutableStateOf(false)
    var listItems by mutableStateOf(emptyList<PromptItemData>())
        private set
    private var areListItemsInitialized = false

    fun typingEffect(
        item: PromptItemData,
        isTyping: MutableState<Boolean>,
        hapticFeedback: HapticFeedback,
        onTypingComplete: () -> Unit = {}
    ) {
        if (typingEffectJob?.isActive != true) {
            typingEffectJob = viewModelScope.launch {
                if (!item.isAuthor) {
                    val messageToType = if (item.loadingMessage.value.isNotEmpty()) {
                        AnnotatedString(item.loadingMessage.value)
                    } else {
                        item.message
                    }
                    isTyping.value = true
                    for (i in 1..messageToType.length) {
                        delay((20..30).random().toLong())
                        item.displayedText.value = messageToType.subSequence(0, i)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    isTyping.value = false
                    onTypingComplete()
                }
            }
        }
    }

    fun initializeListItems(uploadFormatStringResource: Int, videoUri: String, context: Context) {
        if (!areListItemsInitialized) {
            updateListItems(uploadFormatStringResource, videoUri, context)
            areListItemsInitialized = true
        }
    }

    private fun updateListItems(
        uploadFormatStringResource: Int, videoUri: String, context: Context
    ) {
        if (mutableVideoUriString != videoUri) {
            mutableVideoUriString = videoUri

            val newList = if (uploadFormatStringResource == R.string.url_upload) {
                listOf(
                    PromptItemData(
                        message = AnnotatedString(text = context.getString(R.string.please_input_the_url_of_the_video_you_would_like_to_trim))
                    )
                )
            } else {
                val thumbnail =
                    if (videoUri != context.getString(R.string.empty) && videoUri.isNotEmpty()) {
                        getAllVideoMetadata(
                            context = context, videoUri = Uri.parse(videoUri)
                        )?.thumbnail
                    } else null
                val duration =
                    if (videoUri != context.getString(R.string.empty) && videoUri.isNotEmpty()) {
                        MediaConfigurationUtil.formatDuration(
                            durationMillis = getAllVideoMetadata(
                                context = context, videoUri = Uri.parse(videoUri)
                            )?.duration ?: 0, shouldBeInFullFormat = false
                        )
                    } else context.getString(R.string._0_00)

                listOf(
                    PromptItemData(
                        videoUriString = videoUri, thumbnailBitmap = thumbnail, duration = duration
                    ),
                    PromptItemData(message = AnnotatedString(text = context.getString(R.string.how_would_you_like_to_trim)))
                )
            }

            listItems = newList
        }
    }

    fun updateMutableVideoUriString(videoUriString: String) {
        mutableVideoUriString = videoUriString
    }

    fun addListItem(newItem: PromptItemData) {
        listItems += newItem
    }

    fun removeListItem(item: PromptItemData) {
        listItems = listItems.filterNot { it == item }
    }

    fun updateMessageForItem(position: Int, newMessage: String) {
        listItems = listItems.mapIndexed { index, item ->
            if (index == position) {
                item.copy(
                    loadingMessage = mutableStateOf(newMessage), hasTyped = mutableStateOf(false)
                )
            } else {
                item
            }
        }
    }
}