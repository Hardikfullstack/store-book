package com.pdfscanner.editorapp.utils

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

    // Observable state for UI to show loading dialog
    var isAdLoading = mutableStateOf(false)
        private set

    private var timeoutJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Preloads an Interstitial Ad. Call this early (e.g., in a LaunchedEffect or when returning to
     * Home).
     */
    fun loadAd(context: Context, adUnitId: String?, placement: String) {
        val appConfig = (context.applicationContext as? com.pdfscanner.editorapp.ImageToPdfApplication)?.appConfig
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
                }
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
        onAdDismissed: () -> Unit
    ) {
        val appConfig = (activity.applicationContext as? com.pdfscanner.editorapp.ImageToPdfApplication)?.appConfig
        if (appConfig?.result?.google_ads_on_off != "on") {
            onAdDismissed()
            return
        }

        if (!isOn || adUnitId.isNullOrEmpty()) {
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
            timeoutJob = scope.launch {
                delay(3000) // 3 seconds timeout
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
                }
            )
        }
    }

    private fun showAd(
        activity: Activity,
        adUnitId: String,
        placement: String,
        onAdDismissed: () -> Unit
    ) {
        interstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        AnalyticsManager.logAdEvent("interstitial", placement, "dismissed")
                        interstitialAd = null
                        onAdDismissed()
                        // Preload the next ad instantly
                        loadAd(activity, adUnitId, placement)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        AnalyticsManager.logAdEvent("interstitial", placement, "failed_to_show")
                        interstitialAd = null
                        onAdDismissed()
                        // Try to load again
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
