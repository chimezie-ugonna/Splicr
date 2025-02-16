package com.splicr.app.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.QuerySnapshot
import com.splicr.app.R
import com.splicr.app.data.CanvasItemData
import com.splicr.app.utils.FirestoreQueryUtil.deleteFileByUrl
import com.splicr.app.utils.FirestoreQueryUtil.deleteMediaById
import com.splicr.app.utils.FirestoreQueryUtil.fetchMedia
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    var canvasItems by mutableStateOf(emptyList<CanvasItemData>())
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    var isDeleting by mutableStateOf(false)
        private set

    var showError by mutableStateOf(false)
        private set

    var showSuccess by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    var isEmpty by mutableStateOf(false)
        private set

    private var lastVisibleDocument: QuerySnapshot? = null

    fun loadItems() {
        viewModelScope.launch {
            isLoadingMore = true
            val snapshot = fetchMedia(lastVisibleDocument)
            if (snapshot != null && !snapshot.isEmpty) {
                val newCanvasItems = snapshot.toObjects(CanvasItemData::class.java)
                val existingIds = canvasItems.map { it.id }.toSet()
                val filteredNewItems = newCanvasItems.filterNot { newItem ->
                    existingIds.contains(newItem.id)
                }
                canvasItems += filteredNewItems
                lastVisibleDocument = snapshot
            } else {
                isEmpty = canvasItems.isEmpty()
            }
            isLoadingMore = false
        }
    }

    fun deleteItem(item: CanvasItemData, context: Context) {
        viewModelScope.launch {
            isDeleting = true
            showError = false
            showSuccess = false
            errorMessage = ""
            deleteFileByUrl(item.url).onSuccess {
                deleteFileByUrl(item.thumbnailUrl).onSuccess {
                    deleteMediaById(documentId = item.id).onSuccess {
                        canvasItems = canvasItems.filterNot { it.id == item.id }
                        showSuccess = true
                        isEmpty = canvasItems.isEmpty()
                    }.onFailure {
                        showError = true
                        errorMessage = it.localizedMessage?.toString()
                            ?: context.getString(R.string.an_unexpected_error_occurred)
                    }
                }.onFailure {
                    showError = true
                    errorMessage = it.localizedMessage?.toString()
                        ?: context.getString(R.string.an_unexpected_error_occurred)
                }
            }.onFailure {
                showError = true
                errorMessage = it.localizedMessage?.toString()
                    ?: context.getString(R.string.an_unexpected_error_occurred)
            }
            isDeleting = false
        }
    }

    fun showError(status: Boolean) {
        showError = status
    }

    fun showSuccess(status: Boolean) {
        showSuccess = status
    }

    fun addItem(item: CanvasItemData) {
        canvasItems = listOf(item) + canvasItems
    }

    fun resetItems() {
        lastVisibleDocument = null
        canvasItems = emptyList()
    }
}