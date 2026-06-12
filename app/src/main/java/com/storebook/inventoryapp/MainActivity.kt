package com.storebook.inventoryapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.storebook.inventoryapp.ui.navigation.AppNavigation
import com.storebook.inventoryapp.ui.theme.LocalAppTheme
import com.storebook.inventoryapp.ui.theme.ManualThemeManager
import com.storebook.inventoryapp.ui.theme.StoreBookTheme
import com.storebook.inventoryapp.ui.viewmodels.AppConfigViewModel
import com.storebook.inventoryapp.utils.AnalyticsManager
import com.storebook.inventoryapp.utils.LanguageManager
import com.storebook.inventoryapp.utils.LocalAppConfig
import com.storebook.inventoryapp.utils.LocalDynamicAppBrand
import com.storebook.inventoryapp.utils.LocalDynamicAppName
import com.storebook.inventoryapp.utils.LocaleHelper
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: android.content.Context) {
        val languageManager = LanguageManager(newBase)
        val lang =
            try {
                val l = runBlocking { languageManager.appLanguage.first() }
                android.util.Log.d("MainActivity", "attachBaseContext read lang: $l")
                l
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "attachBaseContext read error", e)
                "en"
            }
        val context = LocaleHelper.wrap(newBase, lang)
        super.attachBaseContext(context)
    }

    val externalPdfUri = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val languageManager = LanguageManager(this)
        lifecycleScope.launch {
            val savedLang =
                try {
                    languageManager.appLanguage.first()
                } catch (e: Exception) {
                    "en"
                }
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            if (currentLocales.isEmpty || currentLocales.get(0)?.language != savedLang) {
                android.util.Log.d(
                    "MainActivity",
                    "App locales disagree with saved config ($savedLang). Setting locales...",
                )
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(savedLang),
                )
            }
        }
        AnalyticsManager.init(this)
        AnalyticsManager.logEventWithAction("app_open", "app", "launched")
        PDFBoxResourceLoader.init(applicationContext)

        handleIntent(intent)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val themeManager = ManualThemeManager.getInstance(this)
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.light(
                    android.graphics.Color.WHITE,
                    android.graphics.Color.BLACK,
                ),
            navigationBarStyle =
                SystemBarStyle.light(
                    android.graphics.Color.WHITE,
                    android.graphics.Color.WHITE,
                ),
        )
        val appConfigViewModel: AppConfigViewModel by viewModels()

        lifecycleScope.launch {
            appConfigViewModel.appResponse.collect { response ->
                (application as? StoreBookApplication)?.appConfig = response
            }
        }

        setContent {
            val isDarkMode by themeManager.isDarkMode

            StoreBookTheme(darkTheme = isDarkMode) {
                val appConfigState by appConfigViewModel.appResponse.collectAsState()
                val dynamicAppName by appConfigViewModel.dynamicAppName.collectAsState()
                val dynamicAppBrand by appConfigViewModel.dynamicAppBrand.collectAsState()

                CompositionLocalProvider(
                    LocalAppConfig provides appConfigState,
                    LocalDynamicAppName provides dynamicAppName,
                    LocalDynamicAppBrand provides dynamicAppBrand,
                    LocalAppTheme provides themeManager,
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW && intent.type == "application/pdf") {
            val uri: Uri? = intent.data
            uri?.let {
                externalPdfUri.value = it.toString()
            }
        }
    }
}
