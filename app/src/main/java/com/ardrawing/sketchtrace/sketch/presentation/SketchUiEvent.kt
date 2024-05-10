package com.ardrawing.sketchtrace.sketch.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed interface SketchUiEvent {

    // undone yet (UpdateTimerTime)
    data class UpdateCountdownTime(val time: String) : SketchUiEvent

    data class InitializeActivity(
        val shouldInit: Boolean = false
    ) : SketchUiEvent

    data class UpdateImageTransparency(val transparency: Float) : SketchUiEvent
    data class ShowAndHideTakePhotoDialog(val shouldShow: Boolean) : SketchUiEvent
    data class ShowAndHideSavePhotoDialog(val shouldShow: Boolean) : SketchUiEvent
    data class ShowAndHideTimeFinishedDialog(val shouldShow: Boolean) : SketchUiEvent
    data class StartAndStopCountdownTimer(val shouldStart: Boolean) : SketchUiEvent


    data object UpdateIsFlashEnabled : SketchUiEvent
    data object UpdateIsImageLocked : SketchUiEvent
    data object UpdateIsImageBordered : SketchUiEvent

    data object SaveTakenPhoto : SketchUiEvent

}














