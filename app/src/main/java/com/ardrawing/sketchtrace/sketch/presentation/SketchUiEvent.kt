package com.ardrawing.sketchtrace.sketch.presentation

import java.io.File

/**
 * @author Ahmed Guedmioui
 */
sealed interface SketchUiEvent {

    data class InitializeActivity(val shouldInit: Boolean = false) : SketchUiEvent

    data class StartAndStopCountdownTimer(val shouldStart: Boolean) : SketchUiEvent
    data class UpdateCountdownTime(val time: String) : SketchUiEvent

    data class ShowAndHideTakePhotoDialog(val shouldShow: Boolean) : SketchUiEvent
    data class ShowAndHideSavePhotoDialog(val shouldShow: Boolean) : SketchUiEvent
    data class ShowAndHideTimeFinishedDialog(val shouldShow: Boolean) : SketchUiEvent
    data class UpdateImageTransparency(val transparency: Float) : SketchUiEvent


    data object UpdateIsFlashEnabled : SketchUiEvent
    data object UpdateIsImageLocked : SketchUiEvent
    data object UpdateIsImageBordered : SketchUiEvent


    data object SaveTakenPhoto : SketchUiEvent

    data object StartVideo : SketchUiEvent
    data object StopVideo : SketchUiEvent
    data class SaveVideo(
        val videoFile: File, val isFast: Boolean
    ) : SketchUiEvent

}














