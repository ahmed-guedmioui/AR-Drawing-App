package com.ardrawing.sketchtrace.splash.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed interface SplashUiEvent {
    data object TryAgain : SplashUiEvent
    data object ContinueApp : SplashUiEvent
    data object AlreadySubscribed : SplashUiEvent

    data class OnAdmobConsent(
        val canShowAdmobAds: Boolean
    ) : SplashUiEvent
}