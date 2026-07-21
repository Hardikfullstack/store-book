package com.storebook.inventoryapp.ui.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.storebook.inventoryapp.data.model.AppResponse
import com.storebook.inventoryapp.data.model.AppResult
import com.storebook.inventoryapp.data.network.ApiClient
import com.storebook.inventoryapp.utils.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class AppConfigViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("app_config", Context.MODE_PRIVATE)
    private val defaultAppName = "StoreBook"
    private val defaultAppBrand = " Kirana"

    private val apiPackageName = "StoreBook"
    private val _appResponse = MutableStateFlow<AppResponse?>(loadCachedResponse())
    val appResponse: StateFlow<AppResponse?> = _appResponse
    private val _appName =
        MutableStateFlow(
            _appResponse.value?.result?.app_name
                ?: prefs.getString("app_name", defaultAppName) ?: defaultAppName,
        )
    val appName: StateFlow<String> = _appName

    private val _appBrand =
        MutableStateFlow(
            _appResponse.value?.result?.extra_data_7_message
                ?: prefs.getString("app_brand", defaultAppBrand) ?: defaultAppBrand,
        )
    val appBrand: StateFlow<String> = _appBrand

    init {
        fetchAppData()
        observeNetwork()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            NetworkMonitor(getApplication()).isOnline.collectLatest { isOnline ->
                if (isOnline && (_appResponse.value == null || _appResponse.value?.title == "Loaded from cache")) {
                    fetchAppData()
                }
            }
        }
    }

    private fun loadCachedResponse(): AppResponse? {
        val cachedName = prefs.getString("app_name", null)
        val cachedMaintenance = prefs.getString("maintenance_on_off", null)
        val cachedVersion = prefs.getString("latest_version_message", null)
        val cachedBrand = prefs.getString("app_brand", null)
        val cachedOffline = prefs.getString("offline_on_off", null)

        return if (cachedName != null ||
            cachedMaintenance != null ||
            cachedVersion != null ||
            cachedBrand != null ||
            cachedOffline != null
        ) {
            // Reconstruct a partial AppResponse from the necessary cached fields
            AppResponse(
                status = 200,
                title = "Loaded from cache",
                result =
                    AppResult(
                        app_name = cachedName ?: defaultAppName,
                        extra_data_7_message = cachedBrand ?: defaultAppBrand,
                        extra_data_1_on_off = cachedMaintenance ?: "off",
                        extra_data_2_message = cachedVersion ?: "",
                        extra_data_6_on_off = cachedOffline ?: "off",
                    ),
            )
        } else {
            null
        }
    }

    private fun fetchAppData() {
        viewModelScope.launch {
            try {
                val mediaType = "text/plain".toMediaTypeOrNull()
                val packageNameBody = apiPackageName.toRequestBody(mediaType)

                val response = ApiClient.apiService.getAppData(packageNameBody)
                if (response.status == 200) {
                    _appResponse.value = response

                    // SAVE ONLY THE NECESSARY FIELDS
                    response.result?.let { result ->
                        val editor = prefs.edit()

                        // 1. App name
                        result.app_name?.let {
                            editor.putString("app_name", it)
                            _appName.value = it
                        }

                        // 2. App brand
                        result.extra_data_7_message?.let {
                            editor.putString("app_brand", it)
                            _appBrand.value = it
                        }

                        // 2. Maintenance ON/OFF
                        result.extra_data_1_on_off?.let {
                            editor.putString("maintenance_on_off", it)
                        }

                        // 3. Update version message
                        result.extra_data_2_message?.let {
                            editor.putString("latest_version_message", it)
                        }

                        // 4. Offline mode ON/OFF
                        result.extra_data_6_on_off?.let {
                            editor.putString("offline_on_off", it)
                        }

                        editor.apply()
                    }
                } else {
                    // Handle failure or non-200 code
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                if (com.storebook.inventoryapp.BuildConfig.DEBUG) {
                    Log
                        .e(AppConfigViewModel::class.java.simpleName, "Failed to fetch app data: ${e.message}", e)
                }
            }
        }
    }
}
