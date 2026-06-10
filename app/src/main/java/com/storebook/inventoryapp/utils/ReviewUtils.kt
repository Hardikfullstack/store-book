package com.storebook.inventoryapp.utils

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory

object ReviewUtils {
    fun launchInAppReview(activity: Activity, onComplete: () -> Unit = {}) {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown.
                    onComplete()
                }
            } else {
                // There was some problem, continue regardless of the result.
                onComplete()
            }
        }
    }
}
