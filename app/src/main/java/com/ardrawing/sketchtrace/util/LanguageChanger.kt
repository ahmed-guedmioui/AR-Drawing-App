package com.ardrawing.sketchtrace.util

import android.content.Context
import java.util.Locale

/**
 * @author Ahmed Guedmioui
 */
object LanguageChanger {
    fun changeAppLanguage(context: Context): Context {
        val prefs = context.getSharedPreferences(
            "ar_drawing_med_prefs_file", Context.MODE_PRIVATE
        )
        val languageCode = prefs.getString(PrefsConstants.LANGUAGE, "en") ?: "en"

        val config = context.resources.configuration
        val systemLocal = config.getLocales().get(0)

        if (systemLocal.language != languageCode) {
            println("languageCode: $languageCode")
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }

        return context.createConfigurationContext(config)
    }
}