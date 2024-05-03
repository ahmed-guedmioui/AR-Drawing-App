package com.ardrawing.sketchtrace.sketch.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed interface SketchUiEvent {
    data class UpdateImageTransparency(
        val transparency: Float
    ) : SketchUiEvent
    data class UpdateTimerTime(
        val timerTime: String
    ) : SketchUiEvent

    data object UpdateIsFlashEnabled : SketchUiEvent
    data object UpdateIsImageLocked : SketchUiEvent
    data object UpdateIsImageBordered : SketchUiEvent

}