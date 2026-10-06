package com.storebook.inventoryapp.utils

import android.os.Build
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.storebook.inventoryapp.BuildConfig
import com.storebook.inventoryapp.dataconnect.StorebookConnectorConnector
import com.storebook.inventoryapp.dataconnect.instance

object EmulatorConfig {
    private const val TAG = "EmulatorConfig"

    /**
     * Set to true to connect Android app to local Firebase Emulators.
     * Set to false to connect to production Firebase cloud.
     */
    const val USE_EMULATOR = true

    const val AUTH_PORT = 9099
    const val STORAGE_PORT = 9199
    const val DATA_CONNECT_PORT = 9399

    /**
     * Android Emulator (AVD) accesses host PC via 10.0.2.2.
     * Physical device over USB accesses host PC via 127.0.0.1 after running adb reverse.
     */
    val EMULATOR_HOST: String = if (isEmulator()) "10.0.2.2" else "127.0.0.1"

    fun setupIfEnabled() {
        if (!BuildConfig.DEBUG || !USE_EMULATOR) return

        try {
            Log.i(TAG, "Configuring Firebase Emulators on host: $EMULATOR_HOST")
            FirebaseAuth.getInstance().useEmulator(EMULATOR_HOST, AUTH_PORT)
            FirebaseStorage.getInstance().useEmulator(EMULATOR_HOST, STORAGE_PORT)
            StorebookConnectorConnector.instance.dataConnect.useEmulator(EMULATOR_HOST, DATA_CONNECT_PORT)
            Log.i(
                TAG,
                "Firebase Emulators connected successfully (Auth: $AUTH_PORT, Storage: $STORAGE_PORT, DataConnect: $DATA_CONNECT_PORT)",
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to Firebase Emulators", e)
        }
    }

    private fun isEmulator(): Boolean =
        Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.startsWith("unknown") ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for x86") ||
            Build.MANUFACTURER.contains("Genymotion") ||
            (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
            Build.PRODUCT == "google_sdk"
}
