package com.ardrawing.sketchtrace.onboarding.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed interface OnboardingUiEvent {
    object NextTip : OnboardingUiEvent
    object Back : OnboardingUiEvent
    object Navigate : OnboardingUiEvent
}