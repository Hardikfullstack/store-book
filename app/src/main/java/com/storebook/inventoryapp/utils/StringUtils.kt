package com.storebook.inventoryapp.utils

object StringUtils {
    /**
     * Sanitizes user input to prevent XSS and basic injection attacks
     * when syncing with the backend or rendering in WebView.
     */
    fun sanitize(input: String?): String {
        if (input == null) return ""
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
    }
}
