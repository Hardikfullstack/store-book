package com.pdfscanner.editorapp.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pdfscanner.editorapp.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creates a single instance of DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class LanguageItem(
    val id: String,
    val name: String,
    val nativeName: String,
    val iconResId: Int
)

fun getSupportedLanguages(): List<LanguageItem> = listOf(
    LanguageItem("en", "English", "English", R.drawable.language_ic_english),
    LanguageItem("it", "Italian", "Italiano", R.drawable.language_ic_italian),
    LanguageItem("hi", "Hindi", "हिन्दी", R.drawable.language_ic_hindi),
    LanguageItem("gu", "Gujarati", "ગુજરાતી", R.drawable.language_ic_hindi),
    LanguageItem("es", "Spanish", "Español", R.drawable.language_ic_spanish),
    LanguageItem("fr", "French", "Français", R.drawable.language_ic_french),
    LanguageItem("de", "German", "Deutsch", R.drawable.language_ic_german),
    LanguageItem("pt", "Portuguese", "Português", R.drawable.language_ic_portuguese),
    LanguageItem("ru", "Russian", "Русский", R.drawable.language_ic_russian)
)

class LanguageManager(private val context: Context) {

    companion object {
        val LANGUAGE_KEY = stringPreferencesKey("app_language")
        val IS_LANGUAGE_SET_KEY = booleanPreferencesKey("is_language_set")
    }

    // Default language is 'en' (English)
    val appLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "en"
    }

    val isLanguageSet: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LANGUAGE_SET_KEY] ?: false
    }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
            preferences[IS_LANGUAGE_SET_KEY] = true
        }
    }
}
