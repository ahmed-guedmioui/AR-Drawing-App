package com.ardrawing.sketchtrace.trace.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed interface TraceUiEvent {
    data class UpdateImageTransparency(
        val transparency: Float
    ) : TraceUiEvent

    data class UpdateScreenBrightness(
        val brightness: Float
    ) : TraceUiEvent

    data class UpdateScreenBackgroundColor(
        val backgroundColor: Int
    ) : TraceUiEvent


    data object ShowStartAnimation : TraceUiEvent
    data object ShowHideColorDialog : TraceUiEvent
    data object UpdateIsImageLocked : TraceUiEvent
    data object UpdateIsImageFlipped : TraceUiEvent

}