package com.storebook.inventoryapp.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

class ManualThemeManager(
    context: Context,
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = mutableStateOf(prefs.getBoolean(KEY_IS_DARK_MODE, false))
    val isDarkMode: State<Boolean> = _isDarkMode

    fun toggleTheme() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, newValue).apply()
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, enabled).apply()
    }

    companion object {
        private const val KEY_IS_DARK_MODE = "is_dark_mode"

        @Volatile
        private var INSTANCE: ManualThemeManager? = null

        fun getInstance(context: Context): ManualThemeManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ManualThemeManager(context.applicationContext).also { INSTANCE = it }
            }
    }
}

val LocalAppTheme =
    compositionLocalOf<ManualThemeManager> {
        error("No ManualThemeManager provided")
    }

val isAppDarkMode: Boolean
    @Composable
    get() = LocalAppTheme.current.isDarkMode.value

val appThemeManager: ManualThemeManager
    @Composable
    get() = LocalAppTheme.current

private val LightColorScheme =
    lightColorScheme(
        primary = InkBlue500, // StoreBook Ink Blue
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = InkBlue50,
        onPrimaryContainer = InkBlue900,
        secondary = Emerald500, // Success green
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Emerald100,
        onSecondaryContainer = Color(0xFF064E3B),
        tertiary = Saffron500, // CTA accent
        onTertiary = Color(0xFFFFFFFF),
        background = SlateWhite,
        onBackground = SlateGray800,
        surface = Color(0xFFFFFFFF),
        onSurface = SlateGray800,
        surfaceVariant = SlateGray50,
        onSurfaceVariant = SlateGray600,
        error = Coral500,
        onError = Color.White,
        errorContainer = Coral100,
        onErrorContainer = Color(0xFF7F1D1D),
        outline = SlateGray100,
        outlineVariant = SlateGray100,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = InkBlue300,
        onPrimary = InkBlue900,
        primaryContainer = InkBlue700,
        onPrimaryContainer = InkBlue100,
        secondary = Emerald400,
        onSecondary = Color(0xFF064E3B),
        secondaryContainer = Color(0xFF065F46),
        onSecondaryContainer = Emerald100,
        tertiary = Saffron300,
        onTertiary = Color(0xFF3E2723),
        background = DarkBackground,
        onBackground = Color(0xFFF8FAFC),
        surface = DarkSurface,
        onSurface = Color(0xFFF8FAFC),
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = SlateGray400,
        error = Coral400,
        onError = Color(0xFF7F1D1D),
        errorContainer = Color(0xFF7F1D1D),
        onErrorContainer = Coral100,
        outline = SlateGray600,
        outlineVariant = SlateGray600,
    )

@Composable
fun StoreBookTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    SystemBarsConfig(isDarkMode = darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
