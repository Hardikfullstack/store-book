package com.storebook.inventoryapp

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.ads.MobileAds
import com.storebook.inventoryapp.data.model.AppResponse
import com.storebook.inventoryapp.utils.AppOpenAdManager
import com.storebook.inventoryapp.workers.ExpiryCheckWorker
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import java.util.concurrent.TimeUnit

class StoreBookApplication : Application() {
    private lateinit var appOpenAdManager: AppOpenAdManager
    var appConfig: AppResponse? = null
        set(value) {
            field = value
            if (value != null) {
                appOpenAdManager.fetchAd()
            }
        }

    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(applicationContext)
        MobileAds.initialize(this)
        appOpenAdManager = AppOpenAdManager(this)

        scheduleExpiryChecks()
    }

    private fun scheduleExpiryChecks() {
        val workRequest =
            PeriodicWorkRequestBuilder<ExpiryCheckWorker>(1, TimeUnit.DAYS)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ExpiryCheckWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )
    }
}
