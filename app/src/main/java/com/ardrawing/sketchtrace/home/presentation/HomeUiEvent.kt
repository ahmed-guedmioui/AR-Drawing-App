package com.ardrawing.sketchtrace.home.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed interface HomeUiEvent {
    data object OnAppRated : HomeUiEvent
    data object BackPressed : HomeUiEvent
    data object ShowHideHelperDialog : HomeUiEvent
}