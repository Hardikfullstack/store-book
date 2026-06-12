package com.storebook.inventoryapp.utils

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import android.content.Context

/**
 * AnalyticsManager is a common utility to log events to Firebase Analytics. It provides a central
 * place to manage all event names and parameters.
 */
object AnalyticsManager {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    /** Initializes Firebase Analytics. Should be called once at app startup. */
    fun init(context: Context) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        }
    }

    /** Logs a general event with optional parameters. */
    fun logEvent(eventName: String, params: Bundle? = null) {
        firebaseAnalytics?.logEvent(eventName, params)
    }

    fun logAdEvent(adType: String, placement: String, action: String) {
        val bundle = Bundle().apply {
            putString("ad_type", adType)
            putString("placement", placement)
            putString("action", action) // e.g., "request", "show", "click", "failed"
        }
        logEvent("ad_performance", bundle)
    }

    /** Logs an event with a flexible payload map. */
    fun logEventWithAction(
        eventName: String,
        screenName: String,
        action: String,
        extraParams: Map<String, Any>? = null
    ) {
        val bundle = Bundle().apply {
            putString("screen", screenName)
            putString("action", action)
            extraParams?.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                }
            }
        }
        logEvent(eventName, bundle)
    }
}
