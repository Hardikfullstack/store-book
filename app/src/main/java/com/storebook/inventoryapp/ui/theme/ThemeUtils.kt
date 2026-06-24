package com.storebook.inventoryapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A custom modifier that provides a premium "Neon Glow" in Dark Mode
 * and a soft structural drop shadow in Light Mode.
 */
@Composable
fun Modifier.premiumShadow(
    elevation: Dp = 8.dp,
    shape: androidx.compose.ui.graphics.Shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    useNeonGlow: Boolean = true
): Modifier {
    val isDark = isAppDarkMode
    
    return if (isDark && useNeonGlow) {
        // Neon Glow in Dark Mode
        val neonColor = MaterialTheme.colorScheme.primary
        this.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = neonColor.copy(alpha = 0.5f),
            spotColor = neonColor,
        )
    } else {
        // Soft spread in Light Mode
        this.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        )
    }
}
