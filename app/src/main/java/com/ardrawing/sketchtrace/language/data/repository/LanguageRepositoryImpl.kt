package com.ardrawing.sketchtrace.language.data.repository

import android.content.SharedPreferences
import com.ardrawing.sketchtrace.language.domain.repository.LanguageRepository
import com.ardrawing.sketchtrace.util.PrefsConstants
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
class LanguageRepositoryImpl  @Inject constructor(
    private val prefs: SharedPreferences,
) : LanguageRepository {

    override fun getLanguageCode(): String {
        return prefs.getString(PrefsConstants.LANGUAGE, "en") ?: "en"
    }

    override fun updateLanguageCode(code: String) {
        prefs.edit().putString(PrefsConstants.LANGUAGE, code).apply()
    }
}