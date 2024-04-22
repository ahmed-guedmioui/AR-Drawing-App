package com.ardrawing.sketchtrace.get_started.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed interface GetStartedUiEvent {
    object ShowHidePrivacyDialog : GetStartedUiEvent
    object Navigate : GetStartedUiEvent
}