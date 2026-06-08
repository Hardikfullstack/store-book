package com.pdfscanner.editorapp.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView

@Composable
fun SystemBarsConfig(isDarkMode: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity() as? ComponentActivity ?: return@SideEffect
            if (isDarkMode) {
                val darkColor = android.graphics.Color.parseColor("#212121")
                activity.enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(darkColor),
                    navigationBarStyle = SystemBarStyle.dark(darkColor)
                )
            } else {
                activity.enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.light(Color.WHITE, Color.BLACK),
                    navigationBarStyle = SystemBarStyle.light(Color.WHITE, Color.BLACK)
                )
            }
            
            // Hide Bottom Navigation Bar
            val window = activity.window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

// Helper function to safely extract Activity from Compose Context
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}