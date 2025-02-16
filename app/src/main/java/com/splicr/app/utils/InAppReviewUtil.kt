package com.splicr.app.utils

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory

object InAppReviewUtil {

    fun triggerInAppReviewAutomatically(context: Context) {
        val manager = ReviewManagerFactory.create(context)
        val request = manager.requestReviewFlow()

        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val activity = context as? Activity
                if (activity != null) {
                    manager.launchReviewFlow(activity, reviewInfo)
                }
            }
        }
    }
}