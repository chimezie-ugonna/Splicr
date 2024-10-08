package com.splicr.app.viewModel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.splicr.app.R
import com.splicr.app.data.CanvasItemData
import com.splicr.app.utils.FirestoreQueryUtil.deleteFileByUrl
import com.splicr.app.utils.FirestoreQueryUtil.deleteMediaById
import com.splicr.app.utils.FirestoreQueryUtil.fetchMedia
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private var listenerRegistration: ListenerRegistration? = null

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
                        canvasItems = canvasItems.toMutableList().also {
                            it.remove(
                                item
                            )
                        }
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

    fun listenForRealTimeUpdates() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null && listenerRegistration == null) {
            listenerRegistration = db.collection("media").whereEqualTo("userId", currentUserId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Firestore", "Error fetching canvas items: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        viewModelScope.launch {
                            val newCanvasItems = snapshot.toObjects(CanvasItemData::class.java)
                            val updatedCanvasItems = canvasItems.toMutableList()
                            newCanvasItems.forEach { newItem ->
                                val existingItemIndex =
                                    updatedCanvasItems.indexOfFirst { it.id == newItem.id }
                                if (existingItemIndex != -1) {
                                    updatedCanvasItems[existingItemIndex] =
                                        updatedCanvasItems[existingItemIndex].copy(
                                            title = newItem.title,
                                            url = newItem.url,
                                            thumbnailUrl = newItem.thumbnailUrl,
                                            duration = newItem.duration,
                                            size = newItem.size,
                                            aspectRatioTypeKey = newItem.aspectRatioTypeKey,
                                            aspectRatioWidth = newItem.aspectRatioWidth,
                                            aspectRatioHeight = newItem.aspectRatioHeight
                                        )
                                } else {
                                    updatedCanvasItems.add(0, newItem)
                                }
                            }

                            val idsToRemove =
                                updatedCanvasItems.map { it.id } - newCanvasItems.map { it.id }
                                    .toSet()
                            updatedCanvasItems.removeAll { idsToRemove.contains(it.id) }

                            canvasItems = updatedCanvasItems
                        }
                    }
                }
        }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    fun showError(status: Boolean) {
        showError = status
    }

    fun showSuccess(status: Boolean) {
        showSuccess = status
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }

    fun resetItems() {
        lastVisibleDocument = null
        canvasItems = emptyList()
    }
}