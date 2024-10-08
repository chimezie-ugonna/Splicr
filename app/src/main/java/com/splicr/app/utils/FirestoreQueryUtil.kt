package com.splicr.app.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.splicr.app.R
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
            null
        }
    }

    suspend fun deleteUserDataAndAccount(context: Context): Result<Boolean> {
        val user =
            Firebase.auth.currentUser ?: return Result.failure(Exception(context.getString(R.string.no_account_signed_in)))
        val userId = user.uid
        val firestore = Firebase.firestore

        // Step 1: Delete files from the media collection
        try {
            val mediaQuery =
                firestore.collection("media").whereEqualTo("userId", userId).get().await()
            for (document in mediaQuery.documents) {
                val url = document.getString("url")
                val thumbnailUrl = document.getString("thumbnailUrl")
                if (url != null) {
                    deleteFileByUrl(url)
                }
                if (thumbnailUrl != null) {
                    deleteFileByUrl(thumbnailUrl)
                }
                firestore.collection("media").document(document.id).delete().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }

        // Step 2: Delete documents from the subscriptions collection
        try {
            val subscriptionQuery =
                firestore.collection("subscriptions").whereEqualTo("userId", userId).get().await()
            for (document in subscriptionQuery.documents) {
                firestore.collection("subscriptions").document(document.id).delete().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }

        // Step 3: Delete the user account
        try {
            user.delete().await()
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            return Result.failure(e)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }

        return Result.success(true)
    }

    suspend fun deleteMediaById(documentId: String): Result<Boolean> {
        return try {
            Firebase.firestore.collection("media").document(documentId).delete().await()
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
            e.printStackTrace()
            Result.failure(e)
        }
    }
}