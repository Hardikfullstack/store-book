package com.pdfscanner.editorapp.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import java.util.Locale

class LocaleHelper(base: Context) : ContextWrapper(base) {

    companion object {
        fun wrap(context: Context, language: String): ContextWrapper {
            var contextVar = context
            val locale = Locale(language)
            Locale.setDefault(locale)
            val resources = contextVar.resources
            val configuration = resources.configuration

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(locale)
                val localeList = android.os.LocaleList(locale)
                android.os.LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
                contextVar = contextVar.createConfigurationContext(configuration)
            } else {
                configuration.locale = locale
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }

            return LocaleHelper(contextVar)
        }
    }
}
