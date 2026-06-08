package com.pdfscanner.editorapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.pdfscanner.editorapp.ui.navigation.AppNavigation
import com.pdfscanner.editorapp.ui.theme.ImageToPDFPDFReaderTheme
import com.pdfscanner.editorapp.ui.viewmodels.AppConfigViewModel
import com.pdfscanner.editorapp.utils.AnalyticsManager
import com.pdfscanner.editorapp.utils.LanguageManager
import com.pdfscanner.editorapp.utils.LocaleHelper
import com.pdfscanner.editorapp.utils.LocalAppConfig
import com.pdfscanner.editorapp.utils.LocalDynamicAppName
import com.pdfscanner.editorapp.ui.theme.ManualThemeManager
import com.pdfscanner.editorapp.ui.theme.LocalAppTheme
import com.pdfscanner.editorapp.utils.LocalDynamicAppBrand
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: android.content.Context) {
        val languageManager = LanguageManager(newBase)
        val lang = try {
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
            val savedLang = try {
                languageManager.appLanguage.first()
            } catch (e: Exception) {
                "en"
            }
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            if (currentLocales.isEmpty || currentLocales.get(0)?.language != savedLang) {
                android.util.Log.d("MainActivity", "App locales disagree with saved config ($savedLang). Setting locales...")
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(savedLang)
                )
            }
        }
        AnalyticsManager.init()
        AnalyticsManager.logEventWithAction("app_open", "app", "launched")
        PDFBoxResourceLoader.init(applicationContext)

        handleIntent(intent)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val themeManager = ManualThemeManager.getInstance(this)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.WHITE,
                android.graphics.Color.BLACK
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.WHITE,
                android.graphics.Color.WHITE
            )
        )
        val appConfigViewModel: AppConfigViewModel by viewModels()

        lifecycleScope.launch {
            appConfigViewModel.appResponse.collect { response ->
                (application as? ImageToPdfApplication)?.appConfig = response
            }
        }

        setContent {
            val isDarkMode by themeManager.isDarkMode
            
            ImageToPDFPDFReaderTheme(darkTheme = isDarkMode) {
                val appConfigState by appConfigViewModel.appResponse.collectAsState()
                val dynamicAppName by appConfigViewModel.dynamicAppName.collectAsState()
                val dynamicAppBrand by appConfigViewModel.dynamicAppBrand.collectAsState()

                CompositionLocalProvider(
                    LocalAppConfig provides appConfigState,
                    LocalDynamicAppName provides dynamicAppName,
                    LocalDynamicAppBrand provides dynamicAppBrand,
                    LocalAppTheme provides themeManager
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
