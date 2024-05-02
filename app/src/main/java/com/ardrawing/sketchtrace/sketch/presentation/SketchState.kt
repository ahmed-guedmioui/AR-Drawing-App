package com.ardrawing.sketchtrace.sketch.presentation

import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData


/**
 * @author Ahmed Guedmioui
 */
data class SketchState(
    val isStartAnimationShown: Boolean = false,
    val isFlashEnabled: Boolean = true,
    val timerTime: String = "05:00",

    val isImageEnabled: Boolean = true,
    val isImageFlipped: Boolean = false,
    val isImageBordered: Boolean = false,
    val imageTransparency: Float = 0f,

    val appData: AppData? = null
)