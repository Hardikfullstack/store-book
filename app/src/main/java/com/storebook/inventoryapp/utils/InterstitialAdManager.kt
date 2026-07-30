package com.storebook.inventoryapp.utils

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object InterstitialAdManager {
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingAd = false
    private const val TAG = "InterstitialAdManager"

    // Frequency capping
    private var lastAdShownTime: Long = 0
    private const val MIN_INTERVAL_MILLIS = 5 * 60 * 1000L
    private var sessionShowCount = 0
    private const val MAX_SHOWS_PER_SESSION = 4

    // Safe zones — screens where ads must not show
    private var isInBillingFlow = false
    private var isInAuthFlow = false

    // Observable state for UI to show loading dialog
    var isAdLoading = mutableStateOf(false)
        private set

    private var timeoutJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun enterBillingFlow() {
        isInBillingFlow = true
    }

    fun exitBillingFlow() {
        isInBillingFlow = false
    }

    fun enterAuthFlow() {
        isInAuthFlow = true
    }

    fun exitAuthFlow() {
        isInAuthFlow = false
    }

    private fun isFrequencyLimitReached(): Boolean {
        if (sessionShowCount >= MAX_SHOWS_PER_SESSION) return true
        val elapsed = System.currentTimeMillis() - lastAdShownTime
        return elapsed < MIN_INTERVAL_MILLIS
    }

    private fun isSafeZone(): Boolean = isInBillingFlow || isInAuthFlow

    private fun isPremium(context: Context): Boolean {
        val app = context.applicationContext as? com.storebook.inventoryapp.StoreBookApplication ?: return false
        val prefs = app.getSharedPreferences("storebook_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_premium", false)
    }

    /**
     * Preloads an Interstitial Ad. Call this early (e.g., in a LaunchedEffect or when returning to
     * Home).
     */
    fun loadAd(
        context: Context,
        adUnitId: String?,
        placement: String,
    ) {
        val appConfig = (context.applicationContext as? com.storebook.inventoryapp.StoreBookApplication)?.appConfig
        if (appConfig?.result?.google_ads_on_off != "on") return
        if (adUnitId.isNullOrEmpty() || interstitialAd != null || isLoadingAd) {
            return
        }

        isLoadingAd = true
        AnalyticsManager.logAdEvent("interstitial", placement, "request")
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    AnalyticsManager.logAdEvent("interstitial", placement, "failed_to_load")
                    interstitialAd = null
                    isLoadingAd = false
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    AnalyticsManager.logAdEvent("interstitial", placement, "loaded")
                    interstitialAd = ad
                    isLoadingAd = false
                }
            },
        )
    }

    /**
     * Shows the preloaded Interstitial Ad if `isOn` is true. Always executes `onAdDismissed`
     * regardless of whether the ad was shown or not.
     */
    fun showAdIfRequested(
        activity: Activity,
        adUnitId: String?,
        isOn: Boolean,
        placement: String,
        onAdDismissed: () -> Unit,
    ) {
        val appConfig = (activity.applicationContext as? com.storebook.inventoryapp.StoreBookApplication)?.appConfig
        if (appConfig?.result?.google_ads_on_off != "on") {
            onAdDismissed()
            return
        }

        if (!isOn || adUnitId.isNullOrEmpty()) {
            onAdDismissed()
            return
        }

        // Premium bypass — never show ads for subscribed users
        if (isPremium(activity)) {
            AnalyticsManager.logAdEvent("interstitial", placement, "skipped_premium")
            onAdDismissed()
            return
        }

        // Safe zone guard — do not show during billing or auth flows
        if (isSafeZone()) {
            AnalyticsManager.logAdEvent("interstitial", placement, "skipped_safe_zone")
            onAdDismissed()
            return
        }

        // Frequency cap guard — limit to 4 per session with 5-minute minimum interval
        if (isFrequencyLimitReached()) {
            AnalyticsManager.logAdEvent("interstitial", placement, "skipped_frequency_cap")
            onAdDismissed()
            return
        }

        if (interstitialAd != null) {
            showAd(activity, adUnitId, placement, onAdDismissed)
        } else {
            // Ad NOT ready, show loading dialog and attempt to load it with a timeout
            isAdLoading.value = true

            // Start a timeout job
            timeoutJob?.cancel()
            timeoutJob =
                scope.launch {
                    delay(3000)
                    if (isAdLoading.value) {
                        isAdLoading.value = false
                        AnalyticsManager.logAdEvent("interstitial", placement, "loading_timeout")
                        onAdDismissed()
                    }
                }

            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                activity,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        if (isAdLoading.value) {
                            timeoutJob?.cancel()
                            isAdLoading.value = false
                            AnalyticsManager.logAdEvent("interstitial", placement, "failed_to_load_on_request")
                            onAdDismissed()
                        }
                    }

                    override fun onAdLoaded(ad: InterstitialAd) {
                        if (isAdLoading.value) {
                            timeoutJob?.cancel()
                            isAdLoading.value = false
                            interstitialAd = ad
                            showAd(activity, adUnitId, placement, onAdDismissed)
                        }
                    }
                },
            )
        }
    }

    private fun showAd(
        activity: Activity,
        adUnitId: String,
        placement: String,
        onAdDismissed: () -> Unit,
    ) {
        // Update frequency cap counters when showing
        lastAdShownTime = System.currentTimeMillis()
        sessionShowCount++

        interstitialAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    AnalyticsManager.logAdEvent("interstitial", placement, "dismissed")
                    interstitialAd = null
                    onAdDismissed()
                    loadAd(activity, adUnitId, placement)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    AnalyticsManager.logAdEvent("interstitial", placement, "failed_to_show")
                    interstitialAd = null
                    onAdDismissed()
                    // Decrement counter since ad was not actually shown
                    sessionShowCount--
                    loadAd(activity, adUnitId, placement)
                }

                override fun onAdShowedFullScreenContent() {
                    AnalyticsManager.logAdEvent("interstitial", placement, "show")
                    interstitialAd = null
                }

                override fun onAdImpression() {
                    AnalyticsManager.logAdEvent("interstitial", placement, "impression")
                }
            }

        interstitialAd?.show(activity)
    }
}
