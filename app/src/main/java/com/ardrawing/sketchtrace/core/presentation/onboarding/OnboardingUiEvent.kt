package com.ardrawing.sketchtrace.core.presentation.onboarding

/**
 * @author Ahmed Guedmioui
 */
sealed interface OnboardingUiEvent {
    object NextTip : OnboardingUiEvent
    object Back : OnboardingUiEvent
    object Navigate : OnboardingUiEvent
}