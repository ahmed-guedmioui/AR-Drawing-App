package com.ardrawing.sketchtrace.language.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed interface LanguageUiEvent {
    data class ChangeLanguage(
        val language: String
    ) : LanguageUiEvent

    object Navigate : LanguageUiEvent
}