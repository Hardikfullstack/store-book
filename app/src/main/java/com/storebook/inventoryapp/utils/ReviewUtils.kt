package com.storebook.inventoryapp.utils

import android.app.Activity
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.play.core.review.ReviewManagerFactory

object ReviewUtils {
    // e10-s4 gating params: show after N=7 days of use OR M=50 transactions, whichever comes first
    private const val MIN_DAYS_SINCE_INSTALL = 7
    private const val MIN_TRANSACTIONS = 50L
    private const val PREFS_NAME = "review_gating"
    private const val KEY_REVIEW_SHOWN = "review_already_shown"
    private const val KEY_INSTALL_TIME = "install_time_ms"

    /**
     * Returns true only when gating criteria are met (N days since install OR M transactions),
     * AND the review dialog hasn't been shown before in this app lifecycle.
     * Calling code should check canPromptForReview(context) before calling launchInAppReview().
     */
    fun canPromptForReview(context: Context): Boolean {
        return try {
            val prefs = getEncryptedPrefs(context)
            val hasShown = prefs.getBoolean(KEY_REVIEW_SHOWN, false)
            if (hasShown) {
                return false
            }

            // Criterion 1: N days since install
            var installTimeMs = prefs.getLong(KEY_INSTALL_TIME, -1L)
            if (installTimeMs == -1L) {
                val installTime = context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
                installTimeMs = installTime
                prefs.edit().putLong(KEY_INSTALL_TIME, installTime).apply()
            }
            val daysSinceInstall = (System.currentTimeMillis() - installTimeMs) / (1000 * 60 * 60 * 24)

            // Criterion 2: M transactions — read from SecureSharedPreferences set by SalesViewModel
            val txnCount = prefs.getLong("total_transactions", 0L)

            // Gating met if EITHER condition satisfied
            daysSinceInstall >= MIN_DAYS_SINCE_INSTALL || txnCount >= MIN_TRANSACTIONS
        } catch (e: Exception) {
            // If any preference read fails, default to not showing review (fail-safe)
            false
        }
    }

    private fun getEncryptedPrefs(context: Context): android.content.SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to plain SharedPrefs if encryption unavailable
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Mark review as shown so it won't prompt again during this app session.
     * Call after launchInAppReview completes (regardless of user action).
     */
    fun markReviewShown(context: Context) {
        try {
            getEncryptedPrefs(context).edit().putBoolean(KEY_REVIEW_SHOWN, true).apply()
        } catch (_: Exception) { /* ignore */ }
    }

    fun launchInAppReview(
        activity: Activity,
        onComplete: () -> Unit = {},
    ) {
        // e10-s4 GATE: Don't prompt review unless gating criteria met
        val canPrompt = try {
            canPromptForReview(activity)
        } catch (_: Exception) {
            false
        }

        if (!canPrompt) {
            onComplete()
            return
        }

        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown.
                    markReviewShown(activity)
                    onComplete()
                }
            } else {
                // There was some problem, continue regardless of the result.
                onComplete()
            }
        }
    }
}
