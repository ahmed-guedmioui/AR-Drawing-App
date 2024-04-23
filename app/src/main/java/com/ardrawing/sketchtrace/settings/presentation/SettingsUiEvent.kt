package com.ardrawing.sketchtrace.settings.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed class SettingsUiEvent() {
    data object ShowHidePrivacyDialog : SettingsUiEvent()
    data object UpdateAppData : SettingsUiEvent()
    data class OnAdmobConsent(
        val canShowAds: Boolean
    ) : SettingsUiEvent()

}