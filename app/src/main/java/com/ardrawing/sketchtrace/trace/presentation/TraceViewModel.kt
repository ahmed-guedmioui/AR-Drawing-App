package com.ardrawing.sketchtrace.trace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _changeBrightnessChannel = Channel<Boolean>()
    val changeBrightnessChannel = _changeBrightnessChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            _traceState.update {
                it.copy(
                    appData = appDataRepository.getAppData(),
                    isSubscribed = appDataRepository.getAppData().isSubscribed
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

            is TraceUiEvent.UpdateBackgroundColor -> {
                _traceState.update {
                    it.copy(
                        backgroundColor = event.backgroundColor
                    )
                }
            }

            is TraceUiEvent.UpdateBrightness -> {
                _traceState.update {
                    it.copy(
                        brightness = event.brightness
                    )
                }
            }

            TraceUiEvent.UpdateIsEnabled -> {
                _traceState.update {
                    it.copy(
                        isEnabled = !it.isEnabled
                    )
                }
            }

            is TraceUiEvent.UpdateTransparency -> {
                _traceState.update {
                    it.copy(
                        transparency = event.transparency
                    )
                }
            }

            TraceUiEvent.UpdateIsFlipped -> {
                _traceState.update {
                    it.copy(
                        isFlipped = !it.isFlipped
                    )
                }
            }

            TraceUiEvent.InitializedActivity -> {
                _traceState.update {
                    it.copy(
                        isActivityInitialized = true
                    )
                }
            }
        }
    }
}



























