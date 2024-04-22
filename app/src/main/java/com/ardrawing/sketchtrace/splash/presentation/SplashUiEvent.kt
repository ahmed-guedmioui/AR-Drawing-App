package com.ardrawing.sketchtrace.splash.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed interface SplashUiEvent {
    object TryAgain : SplashUiEvent
    object ContinueApp : SplashUiEvent
    data class OnAdmobConsent(
        val canShowAdmobAds: Boolean
    ) : SplashUiEvent

    object AlreadySubscribed : SplashUiEvent
}