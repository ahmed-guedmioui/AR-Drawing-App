package com.ardrawing.sketchtrace.sketch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.creation.domian.repository.CreationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _shouldStartCountdownState = MutableStateFlow(false)
    val shouldStartCountdownState = _shouldStartCountdownState.asStateFlow()

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
                _shouldStartCountdownState.update { event.shouldStart }
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
}



























