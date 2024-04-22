package com.ardrawing.sketchtrace.settings.presentation

import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData


/**
 * @author Ahmed Guedmioui
 */
data class SettingsState(
    val showPrivacyDialog: Boolean = false,
    val appData: AppData? = null
)