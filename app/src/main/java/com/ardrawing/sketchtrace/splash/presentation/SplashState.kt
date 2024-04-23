package com.ardrawing.sketchtrace.splash.presentation

import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData

/**
 * @author Ahmed Guedmioui
 */
data class SplashState(
    val areImagesLoaded: Boolean = false,
    val isAppDataLoaded: Boolean = false,

    val isLanguageShown: Boolean = false,
    val isOnboardingShown: Boolean = false,
    val isGetStartedShown: Boolean = false,

    val appData: AppData? = null
)