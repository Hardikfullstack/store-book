package com.storebook.inventoryapp

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.storebook.inventoryapp.data.model.AppResponse
import com.storebook.inventoryapp.utils.AppOpenAdManager
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

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
    }
}
