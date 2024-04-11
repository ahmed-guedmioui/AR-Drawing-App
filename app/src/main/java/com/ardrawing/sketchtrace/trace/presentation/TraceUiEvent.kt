package com.ardrawing.sketchtrace.trace.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed interface TraceUiEvent {
    data class UpdateTransparency(
        val transparency: Float
    ) : TraceUiEvent

    data class UpdateBrightness(
        val brightness: Float
    ) : TraceUiEvent

    data class UpdateBackgroundColor(
        val backgroundColor: Int
    ) : TraceUiEvent

    object UpdateIsEnabled : TraceUiEvent
    object InitializedActivity : TraceUiEvent
    object UpdateIsFlipped : TraceUiEvent
    object ShowHideColorDialog : TraceUiEvent

}