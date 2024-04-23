package com.ardrawing.sketchtrace.core.domain.repository

/**
 * @author Ahmed Guedmioui
 */
interface CoreRepository {

    fun initPurchases()
    fun updateCanShowAdmobAds(canShowAdmobAds: Boolean)
    fun setAppRated()
    fun isAppRated(): Boolean

    fun setLanguageShown()
    fun setOnboardingShown()
    fun setGetStartedShown()

    fun isLanguageShown(): Boolean
    fun isOnboardingShown(): Boolean
    fun isGetStartedShown(): Boolean


}