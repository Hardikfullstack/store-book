package com.storebook.inventoryapp.utils

import androidx.compose.runtime.staticCompositionLocalOf
import com.storebook.inventoryapp.data.model.AppResponse

/**
 * A CompositionLocal to provide global access to the [AppResponse] data
 * fetched from the API across all screens.
 */
val LocalAppConfig = staticCompositionLocalOf<AppResponse?> { null }
val LocalAppName = staticCompositionLocalOf<String> { "StoreBook" }
val LocalAppBrand = staticCompositionLocalOf<String> { " Kirana" }
