package com.ardrawing.sketchtrace.core.data.repository

import android.content.SharedPreferences
import com.ardrawing.sketchtrace.core.domain.repository.CoreRepository
import com.ardrawing.sketchtrace.util.AdsConstants
import com.ardrawing.sketchtrace.util.PrefsConstants
import javax.inject.Inject


/**
 * @author Ahmed Guedmioui
 */
class CoreRepositoryImpl @Inject constructor(
    private val prefs: SharedPreferences
) : CoreRepository {

    override fun getLanguageCode(): String {
        return prefs.getString(PrefsConstants.LANGUAGE, "en") ?: "en"
    }

    override fun updateLanguageCode(code: String) {
        prefs.edit().putString(PrefsConstants.LANGUAGE, code).apply()
    }


    override fun updateCanShowAdmobAds(canShowAdmobAds: Boolean) {
        prefs.edit().putBoolean(
            AdsConstants.CAN_SHOW_ADMOB_ADS, canShowAdmobAds
        ).apply()
    }


    override fun updateIsLanguageChosen() {
        prefs.edit().putBoolean(PrefsConstants.IS_LANGUAGE_CHOSEN, true).apply()
    }

    override fun updateIsOnboardingShown() {
        prefs.edit().putBoolean(PrefsConstants.IS_ONBOARDING_SHOWN, true).apply()
    }

    override fun updateIsGetStartedShown() {
        prefs.edit().putBoolean(PrefsConstants.IS_GET_STARTED_SHOWN, true).apply()
    }

    override fun isLanguageChosen(): Boolean {
        return prefs.getBoolean(PrefsConstants.IS_LANGUAGE_CHOSEN, false)
    }


    override fun isOnboardingShown(): Boolean {
        return prefs.getBoolean(PrefsConstants.IS_ONBOARDING_SHOWN, false)
    }

    override fun isGetStartedShown(): Boolean {
        return prefs.getBoolean(PrefsConstants.IS_GET_STARTED_SHOWN, false)
    }


}

















