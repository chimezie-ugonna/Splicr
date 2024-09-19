package com.splicr.app.utils

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

object FirestoreQueryUtil {
    suspend fun fetchFAQs(lastVisibleDocument: QuerySnapshot? = null): QuerySnapshot? {
        val query =
            Firebase.firestore.collection("faqs").orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
        val paginatedQuery = lastVisibleDocument?.let {
            query.startAfter(it.documents.last())
        } ?: query

        return try {
            paginatedQuery.get().await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchMedia(lastVisibleDocument: QuerySnapshot? = null): QuerySnapshot? {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return null

        val query =
            Firebase.firestore.collection("media").orderBy("createdAt", Query.Direction.DESCENDING)
                .whereEqualTo("userId", currentUserId).limit(10)

        val paginatedQuery = lastVisibleDocument?.let {
            query.startAfter(it.documents.last())
        } ?: query

        return try {
            paginatedQuery.get().await()
        } catch (e: Exception) {
            e.printStackTrace()
            e.localizedMessage?.let { Log.d("fetchMedia", it) }
            null
        }
    }

    suspend fun deleteMediaById(mediaId: String): Result<Boolean> {
        return try {
            val documentRef = Firebase.firestore.collection("media").document(mediaId)
            Firebase.firestore.runTransaction { transaction ->
                val document = transaction.get(documentRef)
                if (document.exists() && document.getString("userId") == Firebase.auth.currentUser?.uid) {
                    transaction.delete(documentRef)
                }
            }.await()
            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun deleteFileByUrl(fileUrl: String): Result<Boolean> {
        return try {
            Firebase.storage.getReferenceFromUrl(fileUrl).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}