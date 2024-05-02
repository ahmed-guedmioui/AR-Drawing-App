package com.ardrawing.sketchtrace.trace.presentation

import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData


/**
 * @author Ahmed Guedmioui
 */
data class TraceState(
    val isStartAnimationShown: Boolean = false,
    val isImageFlipped: Boolean = false,
    val screenBrightness: Float = 0f,

    val isImageEnabled: Boolean = true,
    val imageTransparency: Float = 50f,

    val screenBackgroundColor: Int = 0,
    val isColorDialogShown: Boolean = false,

    val appData: AppData? = null
)