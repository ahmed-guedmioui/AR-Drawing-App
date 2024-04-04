package com.ardrawing.sketchtrace.core.presentation.settings

import com.ardrawing.sketchtrace.image_list.presentation.category.CategoryUiEvents

/**
 * @author Ahmed Guedmioui
 */
sealed class SettingsUiEvent() {
    object ShowHidePrivacyDialog : SettingsUiEvent()
    object UpdateAppData: SettingsUiEvent()

}