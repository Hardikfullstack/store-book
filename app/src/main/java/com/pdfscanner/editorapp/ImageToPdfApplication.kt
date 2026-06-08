package com.pdfscanner.editorapp

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.pdfscanner.editorapp.data.model.AppResponse
import com.pdfscanner.editorapp.utils.AppOpenAdManager
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class ImageToPdfApplication : Application() {
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
