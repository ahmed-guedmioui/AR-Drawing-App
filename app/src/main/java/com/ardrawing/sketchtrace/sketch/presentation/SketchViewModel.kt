package com.ardrawing.sketchtrace.sketch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    init {
        viewModelScope.launch {
            _sketchState.update {
                it.copy(
                    appData = appDataRepository.getAppData(),
                )
            }
        }
    }

    fun onEvent(event: SketchUiEvent) {
        when (event) {
            is SketchUiEvent.UpdateImageTransparency -> {
                _sketchState.update {
                    it.copy(
                        imageTransparency = event.transparency
                    )
                }
            }

            is SketchUiEvent.UpdateTimerTime -> {
                _sketchState.update {
                    it.copy(
                        timerTime = event.timerTime
                    )
                }
            }

            SketchUiEvent.UpdateIsImageEnabled -> {
                _sketchState.update {
                    it.copy(
                        isImageEnabled = !it.isImageEnabled
                    )
                }
            }

            SketchUiEvent.UpdateIsImageFlipped -> {
                _sketchState.update {
                    it.copy(
                        isImageFlipped = !it.isImageFlipped
                    )
                }
            }

            SketchUiEvent.UpdateIsImageBordered -> {
                _sketchState.update {
                    it.copy(
                        isImageBordered = !it.isImageBordered
                    )
                }
            }

            SketchUiEvent.ShowStartAnimation -> {
                _sketchState.update {
                    it.copy(
                        isStartAnimationShown = true
                    )
                }
            }

            SketchUiEvent.UpdateIsFlashEnabled -> {
                _sketchState.update {
                    it.copy(
                        isFlashEnabled = !it.isFlashEnabled
                    )
                }
            }

        }
    }
}



























