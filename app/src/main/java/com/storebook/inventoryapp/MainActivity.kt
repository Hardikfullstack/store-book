package com.storebook.inventoryapp

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.storebook.inventoryapp.ui.navigation.AppNavigation
import com.storebook.inventoryapp.ui.theme.LocalAppTheme
import com.storebook.inventoryapp.ui.theme.ManualThemeManager
import com.storebook.inventoryapp.ui.theme.StoreBookTheme
import com.storebook.inventoryapp.ui.theme.AppThemeMode
import com.storebook.inventoryapp.ui.viewmodels.AppConfigViewModel
import com.storebook.inventoryapp.utils.AnalyticsManager
import com.storebook.inventoryapp.utils.LanguageManager
import com.storebook.inventoryapp.utils.LocalAppConfig
import com.storebook.inventoryapp.utils.LocalAppBrand
import com.storebook.inventoryapp.utils.LocalAppName
import com.storebook.inventoryapp.utils.LocaleHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: android.content.Context) {
        val languageManager = LanguageManager(newBase)
        val lang =
            try {
                val l = runBlocking { languageManager.appLanguage.first() }
                if (com.storebook.inventoryapp.BuildConfig.DEBUG) android.util.Log.d("MainActivity", "attachBaseContext read lang: $l")
                l
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                if (com.storebook.inventoryapp.BuildConfig.DEBUG) android.util.Log.e("MainActivity", "attachBaseContext read error", e)
                "en"
            }
        val context = LocaleHelper.wrap(newBase, lang)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        var keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }
        super.onCreate(savedInstanceState)
        val languageManager = LanguageManager(this)
        lifecycleScope.launch {
            val savedLang =
                try {
                    languageManager.appLanguage.first()
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    "en"
                }
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            if (currentLocales.isEmpty || currentLocales.get(0)?.language != savedLang) {
                if (com.storebook.inventoryapp.BuildConfig.DEBUG) android.util.Log.d(
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

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val themeManager = ManualThemeManager.getInstance(this)
        enableEdgeToEdge()
        val appConfigViewModel: AppConfigViewModel by viewModels()

        lifecycleScope.launch {
            appConfigViewModel.appResponse.collect { response ->
                (application as? StoreBookApplication)?.appConfig = response
            }
        }

        setContent {
            val isDarkMode by themeManager.isDarkMode
            val themeMode by themeManager.themeMode

            // Reset theme to INK_BLUE if free user tries to use a premium theme
            val storebookPrefs = remember { com.storebook.inventoryapp.utils.SecurityUtils.getEncryptedPrefs(this) }
            val isPremium = remember { mutableStateOf(storebookPrefs.getBoolean("is_premium", false) || storebookPrefs.getString("user_role", "owner") == "staff") }

            DisposableEffect(Unit) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "is_premium" || key == "user_role") {
                        isPremium.value = storebookPrefs.getBoolean("is_premium", false) || storebookPrefs.getString("user_role", "owner") == "staff"
                    }
                }
                storebookPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    storebookPrefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            LaunchedEffect(isPremium.value, themeMode) {
                if (!isPremium.value && themeMode != AppThemeMode.INK_BLUE) {
                    themeManager.setThemeMode(AppThemeMode.INK_BLUE)
                }
            }

            StoreBookTheme(darkTheme = isDarkMode, themeMode = themeMode) {
                val appConfigState by appConfigViewModel.appResponse.collectAsState()
                val appName by appConfigViewModel.appName.collectAsState()
                val appBrand by appConfigViewModel.appBrand.collectAsState()

                CompositionLocalProvider(
                    LocalAppConfig provides appConfigState,
                    LocalAppName provides appName,
                    LocalAppBrand provides appBrand,
                    LocalAppTheme provides themeManager,
                ) {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        keepSplash = false
                    }
                    AppNavigation()
                }
            }
        }
    }
}
