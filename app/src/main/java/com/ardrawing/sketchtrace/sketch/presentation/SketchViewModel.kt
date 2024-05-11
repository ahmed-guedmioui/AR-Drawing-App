package com.ardrawing.sketchtrace.sketch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.sketch.domain.repository.SketchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class SketchViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository,
    private val sketchRepository: SketchRepository
) : ViewModel() {


    private val _sketchState = MutableStateFlow(SketchState())
    val sketchState = _sketchState.asStateFlow()

    private val _appDataState = MutableStateFlow<AppData?>(null)
    val appData = _appDataState.asStateFlow()

    private val _countdownTimeState = MutableStateFlow("05:00")
    val countdownTimeState = _countdownTimeState.asStateFlow()

    private val _isActivityInitializedState = MutableStateFlow(false)
    val isActivityInitializedState = _isActivityInitializedState.asStateFlow()

    private val _imageBorderState = MutableStateFlow(false)
    val imageBorderState = _imageBorderState.asStateFlow()

    private val _flashState = MutableStateFlow(false)
    val flashState = _flashState.asStateFlow()

    private val _isTimeFinishedDialogShowingState = MutableStateFlow(false)
    val isTimeFinishedDialogShowingState = _isTimeFinishedDialogShowingState.asStateFlow()

    private val _isTakePhotoDialogShowingState = MutableStateFlow(false)
    val isTakePhotoDialogShowingState = _isTakePhotoDialogShowingState.asStateFlow()


    private val _isSavePhotoDialogShowingState = MutableStateFlow(false)
    val isSavePhotoDialogShowingState = _isSavePhotoDialogShowingState.asStateFlow()

    private val _savePhotoProgressVisibility = Channel<Boolean>()
    val savePhotoProgressVisibility = _savePhotoProgressVisibility.receiveAsFlow()

    private val _isPhotoSavedChannel = Channel<Boolean>()
    val isPhotoSavedChannel = _isPhotoSavedChannel.receiveAsFlow()


    private val _isRecordingVideoState = MutableStateFlow(false)
    val isRecordingVideoState = _isRecordingVideoState.asStateFlow()

    private val _videoElapsedTimeState = MutableStateFlow("00:00")
    val videoElapsedTimeState = _videoElapsedTimeState.asStateFlow()

    private val _startTakingVideoChannel = Channel<File>()
    val startTakingVideoChannel = _startTakingVideoChannel.receiveAsFlow()

    private val _stopTakingVideoChannel = Channel<Boolean>()
    val stopTakingVideoChannel = _stopTakingVideoChannel.receiveAsFlow()

    private val _saveVideoProgressVisibility = Channel<Boolean>()
    val saveVideoProgressVisibility = _saveVideoProgressVisibility.receiveAsFlow()

    private val _isVideoSavedChannel = Channel<Boolean>()
    val isVideoSavedChannel = _isVideoSavedChannel.receiveAsFlow()


    init {
        viewModelScope.launch {
            viewModelScope.launch {
                _appDataState.update { appDataRepository.getAppData() }
            }
        }
    }

    fun onEvent(event: SketchUiEvent) {
        when (event) {

            is SketchUiEvent.InitializeActivity -> {
                _isActivityInitializedState.update { event.shouldInit }
            }

            is SketchUiEvent.UpdateCountdownTime -> {
                _sketchState.update { it.copy(countdownTime = event.time) }
            }

            is SketchUiEvent.UpdateImageTransparency -> {
                _sketchState.update { it.copy(imageTransparency = event.transparency) }
            }

            is SketchUiEvent.ShowAndHideSavePhotoDialog -> {
                _isSavePhotoDialogShowingState.update { event.shouldShow }
            }

            is SketchUiEvent.ShowAndHideTakePhotoDialog -> {
                _isTakePhotoDialogShowingState.update { event.shouldShow }
            }

            is SketchUiEvent.ShowAndHideTimeFinishedDialog -> {
                _isTimeFinishedDialogShowingState.update { event.shouldShow }
            }

            is SketchUiEvent.StartAndStopCountdownTimer -> {
                if (event.shouldStart) {
                    startCountdown()
                } else {
                    countdownJob?.cancel()
                }
            }


            SketchUiEvent.UpdateIsImageLocked -> {
                _sketchState.update { it.copy(isImageLocked = !it.isImageLocked) }
            }

            SketchUiEvent.UpdateIsFlashEnabled -> {
                _flashState.update { !it }
            }

            SketchUiEvent.UpdateIsImageBordered -> {
                _imageBorderState.update { !it }
            }

            SketchUiEvent.SaveTakenPhoto -> {
                viewModelScope.launch {
                    _savePhotoProgressVisibility.send(true)

                    val isSaved = sketchRepository.savePhoto()
                    _isPhotoSavedChannel.send(isSaved)

                    _savePhotoProgressVisibility.send(false)
                }
            }

            SketchUiEvent.StartVideo -> {
                viewModelScope.launch {
                    val tempFile = sketchRepository.createTempVideo()
                    _startTakingVideoChannel.send(tempFile)
                    _isRecordingVideoState.update { true }
                    startVideoElapsedTime()
                }
            }

            SketchUiEvent.StopVideo -> {
                viewModelScope.launch {
                    _stopTakingVideoChannel.send(true)
                    _isRecordingVideoState.update { false }
                }
            }

            is SketchUiEvent.SaveVideo -> {
                viewModelScope.launch {
                    _saveVideoProgressVisibility.send(true)
                    val isSaved = sketchRepository.saveVideo(
                        event.videoFile, event.isFast
                    )
                    _isVideoSavedChannel.send(isSaved)
                    _saveVideoProgressVisibility.send(false)
                }
            }
        }
    }


    val showTheDrawingIsReadyBtnTime = "04:55"
    private var countdownJob: Job? = null
    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {

            var countdownElapsedTimeMillis = 5 * 60 * 1000

            while (countdownElapsedTimeMillis > 0) {
                countdownElapsedTimeMillis -= 1000

                _countdownTimeState.update {
                    getFormattedTime(countdownElapsedTimeMillis.toLong())
                }

                delay(1000)
            }
        }
    }

    private var videoElapsedTimeJob: Job? = null
    private fun startVideoElapsedTime() {

        videoElapsedTimeJob?.cancel()
        videoElapsedTimeJob = viewModelScope.launch {

            var videoElapsedTimeMillis = 0L

            while (isRecordingVideoState.value) {
                videoElapsedTimeMillis += 1000

                _videoElapsedTimeState.update {
                    getFormattedTime(videoElapsedTimeMillis)
                }

                delay(1000)
            }
        }
    }

    private fun getFormattedTime(time: Long): String {
        val seconds = time / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(
            null, "%02d:%02d", minutes, remainingSeconds
        )
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        videoElapsedTimeJob?.cancel()
    }
}



























