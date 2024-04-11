package com.ardrawing.sketchtrace.trace.presentation

import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData


/**
 * @author Ahmed Guedmioui
 */
data class TraceState(
    val isActivityInitialized: Boolean = false,

    val isSubscribed: Boolean = false,
    val isEnabled: Boolean = true,
    val isFlipped: Boolean = false,
    val isGallery: Boolean = false,
    val brightness: Float = 0f,
    val transparency: Float = 0f,
    val backgroundColor: Int = 0,
    val isColorDialogShown: Boolean = false,

    val appData: AppData? = null
)