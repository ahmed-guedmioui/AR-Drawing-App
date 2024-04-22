package com.ardrawing.sketchtrace.home.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed interface HomeUiEvent {
    object BackPressed : HomeUiEvent
    object ShowHideHelperDialog : HomeUiEvent
}