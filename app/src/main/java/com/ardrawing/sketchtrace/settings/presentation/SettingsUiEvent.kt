package com.ardrawing.sketchtrace.settings.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed class SettingsUiEvent() {
    object ShowHidePrivacyDialog : SettingsUiEvent()
    object UpdateAppData: SettingsUiEvent()

}