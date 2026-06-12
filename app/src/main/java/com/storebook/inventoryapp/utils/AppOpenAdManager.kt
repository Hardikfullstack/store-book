package com.storebook.inventoryapp.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.storebook.inventoryapp.StoreBookApplication
import com.storebook.inventoryapp.utils.AnalyticsManager
import java.util.Date

/** Prefetches and shows App Open Ads. */
class AppOpenAdManager(
    private val myApplication: StoreBookApplication,
) : Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var loadTime: Long = 0

    private var currentActivity: Activity? = null

    init {
        myApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /** Request an ad. */
    fun fetchAd() {
        if (isAdAvailable() || isLoadingAd) {
            return
        }

        val appConfig = myApplication.appConfig
        val adUnitId = appConfig?.result?.app_open_1 ?: "ca-app-pub-3940256099942544/9257395921"
        val isOn = appConfig?.result?.app_open_1_on_off == "on"
        val isGoogleAdsOn = appConfig?.result?.google_ads_on_off == "on"

        if (adUnitId.isEmpty() || !isOn || !isGoogleAdsOn) {
            return
        }

        isLoadingAd = true
        AnalyticsManager.logAdEvent("app_open", "app_start", "request")
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            myApplication,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    AnalyticsManager.logAdEvent("app_open", "app_start", "loaded")
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time

                    // If we just loaded the first ad, try to show it immediately
                    // (mostly for the splash screen experience)
                    showAdIfAvailable()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    AnalyticsManager.logAdEvent("app_open", "app_start", "failed_to_load")
                    appOpenAd = null
                    isLoadingAd = false
                }
            },
        )
    }

    /** Utility method that checks if ad exists and can be shown. */
    private fun isAdAvailable(): Boolean = appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Shows the ad if one isn't already showing. */
    fun showAdIfAvailable() {
        if (!isShowingAd && isAdAvailable()) {
            val prefs = myApplication.getSharedPreferences("pdf_prefs", Context.MODE_PRIVATE)
            val isFirstLaunch = prefs.getBoolean("is_first_launch_aoa", true)

            if (isFirstLaunch) {
                prefs.edit().putBoolean("is_first_launch_aoa", false).apply()
                return
            }

            appOpenAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        appOpenAd = null
                        isShowingAd = false
                        fetchAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        appOpenAd = null
                        isShowingAd = false
                        fetchAd()
                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }

                    override fun onAdImpression() {
                        AnalyticsManager.logAdEvent("app_open", "app_start", "impression")
                    }

                    override fun onAdClicked() {
                        AnalyticsManager.logAdEvent("app_open", "app_start", "clicked")
                    }
                }
            currentActivity?.let {
                appOpenAd?.show(it)
            }
        } else {
            fetchAd()
        }
    }

    /** DefaultLifecycleObserver methods */
    override fun onStart(owner: LifecycleOwner) {
        showAdIfAvailable()
    }

    /** ActivityLifecycleCallback methods */
    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?,
    ) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle,
    ) {}

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }
}
