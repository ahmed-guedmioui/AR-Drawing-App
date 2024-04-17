package com.ardrawing.sketchtrace.core.presentation.splash

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