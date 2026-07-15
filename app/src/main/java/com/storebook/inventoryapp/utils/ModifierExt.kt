package com.storebook.inventoryapp.utils

import androidx.compose.foundation.basicMarquee
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Standardized marquee modifier for the app.
 * Applied to text elements that are forced to a single line but need to reveal their full content.
 * 
 * Note: Should not be used in PDF generation or contexts that do not support animations.
 */
fun Modifier.autoMarquee(): Modifier {
    return this.basicMarquee(
        iterations = Int.MAX_VALUE
    )
}
