package com.storebook.inventoryapp.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun HideSystemBarsInBottomSheet() {
    val view = LocalView.current
    DisposableEffect(view) {
        val dialogWindow = (view.parent as? DialogWindowProvider)?.window

        if (dialogWindow != null) {
            // Allows the bottom sheet to draw under system windows if needed
            WindowCompat.setDecorFitsSystemWindows(dialogWindow, false)

            // Hide only the navigation bars for this specific window, keeping status bar visible
            val controller = WindowCompat.getInsetsController(dialogWindow, view)
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose { }
    }
}
