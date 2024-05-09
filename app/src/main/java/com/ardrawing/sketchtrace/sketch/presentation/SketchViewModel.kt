package com.ardrawing.sketchtrace.sketch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.creation.domian.repository.CreationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class SketchViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository,
    private val creationRepository: CreationRepository
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

//    private val _shouldStartCountdownState = MutableStateFlow(false)
//    val shouldStartCountdownState = _shouldStartCountdownState.asStateFlow()

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
        }
    }


    private var countdownJob: Job? = null
    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            val totalMinutes = 5 * 60 * 1000
            val totalSeconds = totalMinutes / 1000

            val startTime = System.currentTimeMillis()
            var remainingSeconds = totalSeconds.toLong()

            while (remainingSeconds > 0) {
                val elapsedTime = System.currentTimeMillis() - startTime
                val remaining = totalSeconds - elapsedTime / 1000
                remainingSeconds = remaining

                val formattedRemainingTime = String.format(
                    locale = null,
                    format = "%02d:%02d",
                    remainingSeconds / 60, remainingSeconds % 60
                )
                _countdownTimeState.update { formattedRemainingTime }

                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}



























