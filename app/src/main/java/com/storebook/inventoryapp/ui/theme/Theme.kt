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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonElevation
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
enum class AppThemeMode { INK_BLUE, SUNSET_ORANGE, FOREST_GREEN, AMETHYST_PURPLE, CRIMSON_RUBY, CHARCOAL_OBSIDIAN }

class ManualThemeManager(
    context: Context,
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = mutableStateOf(prefs.getBoolean(KEY_IS_DARK_MODE, false))
    val isDarkMode: State<Boolean> = _isDarkMode

    private val _themeMode = mutableStateOf(
        try {
            AppThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, AppThemeMode.INK_BLUE.name) ?: AppThemeMode.INK_BLUE.name)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            AppThemeMode.INK_BLUE
        }
    )
    val themeMode: State<AppThemeMode> = _themeMode

    fun toggleTheme() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, newValue).apply()
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, enabled).apply()
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    companion object {
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
        private const val KEY_THEME_MODE = "theme_mode"

        @Suppress("ktlint:standard:property-naming")
        @Volatile
        private var _instance: ManualThemeManager? = null

        fun getInstance(context: Context): ManualThemeManager =
            _instance ?: synchronized(this) {
                _instance ?: ManualThemeManager(context.applicationContext).also { _instance = it }
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
        primary = InkBlue700, // StoreBook Ink Blue
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

// Sunset Orange Mode
private val SunsetOrangeLightColorScheme = lightColorScheme(
    primary = SunsetOrange700,
    onPrimary = Color.White,
    primaryContainer = SunsetOrange50,
    onPrimaryContainer = SunsetOrange900,
    secondary = Emerald500,
    onSecondary = Color.White,
    secondaryContainer = Emerald100,
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = SunsetOrange300,
    onTertiary = Color.White,
    background = SlateWhite,
    onBackground = SlateGray800,
    surface = Color.White,
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

private val SunsetOrangeDarkColorScheme = darkColorScheme(
    primary = SunsetOrange300,
    onPrimary = SunsetOrange900,
    primaryContainer = SunsetOrange700,
    onPrimaryContainer = SunsetOrange100,
    secondary = Emerald400,
    onSecondary = Color(0xFF064E3B),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Emerald100,
    tertiary = SunsetOrange300,
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

// Forest Green Mode
private val ForestGreenLightColorScheme = lightColorScheme(
    primary = ForestGreen700,
    onPrimary = Color.White,
    primaryContainer = ForestGreen50,
    onPrimaryContainer = ForestGreen900,
    secondary = Emerald500,
    onSecondary = Color.White,
    secondaryContainer = Emerald100,
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = ForestGreen300,
    onTertiary = Color.White,
    background = SlateWhite,
    onBackground = SlateGray800,
    surface = Color.White,
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

private val ForestGreenDarkColorScheme = darkColorScheme(
    primary = ForestGreen300,
    onPrimary = ForestGreen900,
    primaryContainer = ForestGreen700,
    onPrimaryContainer = ForestGreen100,
    secondary = Saffron500,
    onSecondary = Color(0xFF78350F),
    secondaryContainer = Color(0xFF92400E),
    onSecondaryContainer = Saffron300,
    tertiary = ForestGreen300,
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

// Amethyst Purple Mode
private val AmethystPurpleLightColorScheme = lightColorScheme(
    primary = AmethystPurple700,
    onPrimary = Color.White,
    primaryContainer = AmethystPurple50,
    onPrimaryContainer = AmethystPurple900,
    secondary = Emerald500,
    onSecondary = Color.White,
    secondaryContainer = Emerald100,
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = AmethystPurple300,
    onTertiary = Color.White,
    background = SlateWhite,
    onBackground = SlateGray800,
    surface = Color.White,
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

private val AmethystPurpleDarkColorScheme = darkColorScheme(
    primary = AmethystPurple300,
    onPrimary = AmethystPurple900,
    primaryContainer = AmethystPurple700,
    onPrimaryContainer = AmethystPurple100,
    secondary = Emerald400,
    onSecondary = Color(0xFF064E3B),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Emerald100,
    tertiary = AmethystPurple300,
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

// Crimson Ruby Mode
private val CrimsonRubyLightColorScheme = lightColorScheme(
    primary = CrimsonRuby700,
    onPrimary = Color.White,
    primaryContainer = CrimsonRuby50,
    onPrimaryContainer = CrimsonRuby900,
    secondary = Emerald500,
    onSecondary = Color.White,
    secondaryContainer = Emerald100,
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = CrimsonRuby300,
    onTertiary = Color.White,
    background = SlateWhite,
    onBackground = SlateGray800,
    surface = Color.White,
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

private val CrimsonRubyDarkColorScheme = darkColorScheme(
    primary = CrimsonRuby300,
    onPrimary = CrimsonRuby900,
    primaryContainer = CrimsonRuby700,
    onPrimaryContainer = CrimsonRuby100,
    secondary = Emerald400,
    onSecondary = Color(0xFF064E3B),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Emerald100,
    tertiary = CrimsonRuby300,
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

// Charcoal Obsidian Mode
private val CharcoalObsidianLightColorScheme = lightColorScheme(
    primary = CharcoalObsidian700,
    onPrimary = Color.White,
    primaryContainer = CharcoalObsidian50,
    onPrimaryContainer = CharcoalObsidian900,
    secondary = Emerald500,
    onSecondary = Color.White,
    secondaryContainer = Emerald100,
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = CharcoalObsidian300,
    onTertiary = Color.White,
    background = SlateWhite,
    onBackground = SlateGray800,
    surface = Color.White,
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

private val CharcoalObsidianDarkColorScheme = darkColorScheme(
    primary = CharcoalObsidian300,
    onPrimary = CharcoalObsidian900,
    primaryContainer = CharcoalObsidian700,
    onPrimaryContainer = CharcoalObsidian100,
    secondary = Emerald400,
    onSecondary = Color(0xFF064E3B),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Emerald100,
    tertiary = CharcoalObsidian300,
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
    themeMode: AppThemeMode = AppThemeMode.INK_BLUE,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.INK_BLUE -> if (darkTheme) DarkColorScheme else LightColorScheme
        AppThemeMode.SUNSET_ORANGE -> if (darkTheme) SunsetOrangeDarkColorScheme else SunsetOrangeLightColorScheme
        AppThemeMode.FOREST_GREEN -> if (darkTheme) ForestGreenDarkColorScheme else ForestGreenLightColorScheme
        AppThemeMode.AMETHYST_PURPLE -> if (darkTheme) AmethystPurpleDarkColorScheme else AmethystPurpleLightColorScheme
        AppThemeMode.CRIMSON_RUBY -> if (darkTheme) CrimsonRubyDarkColorScheme else CrimsonRubyLightColorScheme
        AppThemeMode.CHARCOAL_OBSIDIAN -> if (darkTheme) CharcoalObsidianDarkColorScheme else CharcoalObsidianLightColorScheme
    }

    SystemBarsConfig(isDarkMode = darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

val MaterialTheme.primaryGradient: Brush
    @Composable
    get() = if (isAppDarkMode) {
        SolidColor(colorScheme.primary)
    } else {
        Brush.linearGradient(listOf(colorScheme.primary, colorScheme.tertiary))
    }

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        contentColor = Color.White,
        disabledContainerColor = Color.Transparent
    ),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val gradient = MaterialTheme.primaryGradient
    val backgroundModifier = if (enabled) {
        Modifier.background(brush = gradient, shape = shape)
    } else {
        Modifier.background(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), shape = shape)
    }

    Button(
        onClick = onClick,
        modifier = modifier.then(backgroundModifier),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

