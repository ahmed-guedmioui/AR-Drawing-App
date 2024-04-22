package com.ardrawing.sketchtrace.core.data.repository

import android.app.Application
import android.content.SharedPreferences
import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.core.domain.repository.CoreRepository
import com.ardrawing.sketchtrace.util.AdsConstants
import com.ardrawing.sketchtrace.util.PrefsConstants
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import javax.inject.Inject


/**
 * @author Ahmed Guedmioui
 */
class CoreRepositoryImpl @Inject constructor(
    private val application: Application,
    private val prefs: SharedPreferences,
) : CoreRepository {


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

    override fun initPurchases() {
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(
                application.applicationContext, BuildConfig.REVENUECUT_KEY
            ).build()
        )
    }


}

















