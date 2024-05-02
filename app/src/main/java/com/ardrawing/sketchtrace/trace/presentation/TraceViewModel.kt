package com.ardrawing.sketchtrace.trace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
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
class TraceViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    private val _traceState = MutableStateFlow(TraceState())
    val traceState = _traceState.asStateFlow()

    init {
        viewModelScope.launch {
            _traceState.update {
                it.copy(
                    appData = appDataRepository.getAppData(),
                )
            }
        }
    }

    fun onEvent(event: TraceUiEvent) {
        when (event) {
            TraceUiEvent.ShowHideColorDialog -> {
                _traceState.update {
                    it.copy(
                        isColorDialogShown = !it.isColorDialogShown
                    )
                }
            }

            is TraceUiEvent.UpdateScreenBackgroundColor -> {
                _traceState.update {
                    it.copy(
                        screenBackgroundColor = event.backgroundColor
                    )
                }
            }

            is TraceUiEvent.UpdateScreenBrightness -> {
                _traceState.update {
                    it.copy(
                        screenBrightness = event.brightness
                    )
                }
            }

            TraceUiEvent.UpdateIsImageEnabled -> {
                _traceState.update {
                    it.copy(
                        isImageEnabled = !it.isImageEnabled
                    )
                }
            }

            is TraceUiEvent.UpdateImageTransparency -> {
                _traceState.update {
                    it.copy(
                        imageTransparency = event.transparency
                    )
                }
            }

            TraceUiEvent.UpdateIsImageFlipped -> {
                _traceState.update {
                    it.copy(
                        isImageFlipped = !it.isImageFlipped
                    )
                }
            }

            TraceUiEvent.ShowStartAnimation -> {
                _traceState.update {
                    it.copy(
                        isStartAnimationShown = true
                    )
                }
            }
        }
    }
}



























