package com.ardrawing.sketchtrace.sketch.presentation


/**
 * @author Ahmed Guedmioui
 */
data class SketchState(
    val timerTime: String = "05:00",

    val isImageLocked: Boolean = false,
    val imageTransparency: Float = 50f
)