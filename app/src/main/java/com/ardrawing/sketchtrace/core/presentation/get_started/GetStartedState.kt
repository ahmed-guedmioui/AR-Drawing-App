package com.ardrawing.sketchtrace.core.presentation.get_started

import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData


/**
 * @author Ahmed Guedmioui
 */
data class GetStartedState(
    val showPrivacyDialog: Boolean = false,
    val appData: AppData? = null
)