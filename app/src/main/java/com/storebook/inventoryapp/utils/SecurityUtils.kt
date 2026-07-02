package com.storebook.inventoryapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.util.Log

object SecurityUtils {
    fun getEncryptedPrefs(context: Context): SharedPreferences {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "storebook_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            // Data Migration: Move plaintext data to encrypted storage and wipe original
            val oldPrefs = context.getSharedPreferences("storebook_prefs", Context.MODE_PRIVATE)
            val oldKeys = oldPrefs.all
            if (oldKeys.isNotEmpty()) {
                val editor = encryptedPrefs.edit()
                for ((key, value) in oldKeys) {
                    when (value) {
                        is String -> editor.putString(key, value)
                        is Int -> editor.putInt(key, value)
                        is Long -> editor.putLong(key, value)
                        is Boolean -> editor.putBoolean(key, value)
                        is Float -> editor.putFloat(key, value)
                    }
                }
                editor.apply()
                // Wipe the unencrypted file
                oldPrefs.edit().clear().apply()
            }

            return encryptedPrefs
        } catch (e: Exception) {
            Log.e("SecurityUtils", "Failed to initialize EncryptedSharedPreferences", e)
            // Fallback to in-memory or throw, but here we can return a standard one if it fails (not recommended for strict security, but prevents crash loops on older buggy OEMs)
            return context.getSharedPreferences("storebook_fallback_prefs", Context.MODE_PRIVATE)
        }
    }
}
