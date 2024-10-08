package com.splicr.app.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.QuerySnapshot
import com.splicr.app.data.FAQItemData
import com.splicr.app.utils.FirestoreQueryUtil.fetchFAQs
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    val commentValue = mutableStateOf(TextFieldValue(""))
    val hasPerformedFaqsBottomSheetHapticFeedback = mutableStateOf(false)
    val hasPerformedShareYourFeedbackBottomSheetHapticFeedback = mutableStateOf(false)
    val hasPerformedContactSupportBottomSheetHapticFeedback = mutableStateOf(false)
    val hasPerformedReAuthenticateAccountBottomSheetHapticFeedback = mutableStateOf(false)

    var faqs by mutableStateOf<List<FAQItemData>>(emptyList())
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    var isEmpty by mutableStateOf(false)
        private set

    private var lastVisibleDocument: QuerySnapshot? = null

    fun loadFAQs() {
        viewModelScope.launch {
            isLoadingMore = true
            val snapshot = fetchFAQs(lastVisibleDocument)
            if (snapshot != null && !snapshot.isEmpty) {
                val newFaqs = snapshot.toObjects(FAQItemData::class.java)

                val updatedFaqs = mutableListOf<FAQItemData>()
                updatedFaqs.addAll(faqs)

                for (newFaq in newFaqs) {
                    if (!updatedFaqs.any { it.question == newFaq.question && it.answer == newFaq.answer }) {
                        if (newFaq.question.isNotEmpty() && newFaq.answer.isNotEmpty()) {
                            updatedFaqs.add(newFaq)
                        }
                    }
                }

                faqs = updatedFaqs

                lastVisibleDocument = snapshot
            } else {
                isEmpty = faqs.isEmpty()
            }
            isLoadingMore = false
        }
    }

    fun resetFAQs() {
        lastVisibleDocument = null
        faqs = emptyList()
    }
}