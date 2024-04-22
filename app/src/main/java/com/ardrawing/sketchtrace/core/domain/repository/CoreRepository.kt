package com.ardrawing.sketchtrace.core.domain.repository

/**
 * @author Ahmed Guedmioui
 */
interface CoreRepository {

    fun updateCanShowAdmobAds(canShowAdmobAds: Boolean)

    fun updateIsLanguageChosen()
    fun updateIsOnboardingShown()
    fun updateIsGetStartedShown()

    fun isLanguageChosen(): Boolean
    fun isOnboardingShown(): Boolean
    fun isGetStartedShown(): Boolean


    fun initPurchases()
}