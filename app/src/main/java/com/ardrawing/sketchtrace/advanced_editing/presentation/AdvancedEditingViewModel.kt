package com.ardrawing.sketchtrace.advanced_editing.presentation

import androidx.lifecycle.ViewModel
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class AdvancedEditingViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    private val _advancedEditingState = MutableStateFlow(AdvancedEditingState())
    val advancedEditingState = _advancedEditingState.asStateFlow()

    init {
        _advancedEditingState.update {
            it.copy(
                appData = appDataRepository.getAppData()
            )
        }
    }

    fun onEvent(advancedEditingUiEvent: AdvancedEditingUiEvent) {
        when (advancedEditingUiEvent) {
            is AdvancedEditingUiEvent.Select -> {

                if (advancedEditingState.value.selected == advancedEditingUiEvent.selected) {
                    _advancedEditingState.update {
                        it.copy(selected = 0)
                    }
                } else {
                    _advancedEditingState.update {
                        it.copy(
                            selected = advancedEditingUiEvent.selected
                        )
                    }
                }
            }

            is AdvancedEditingUiEvent.SetContrast -> {
                _advancedEditingState.update {
                    it.copy(
                        contrast = advancedEditingUiEvent.contrast,
                        isContrast = true
                    )
                }
            }

            is AdvancedEditingUiEvent.SetEdge -> {
                _advancedEditingState.update {
                    it.copy(
                        edge = advancedEditingUiEvent.edge,
                        isEdged = true
                    )
                }
            }

            is AdvancedEditingUiEvent.SetNoise -> {
                _advancedEditingState.update {
                    it.copy(
                        noise = advancedEditingUiEvent.noise,
                        isNoise = true
                    )
                }
            }

            is AdvancedEditingUiEvent.SetSharpness -> {
                _advancedEditingState.update {
                    it.copy(
                        sharpness = advancedEditingUiEvent.sharpness,
                        isSharpened = true
                    )
                }
            }

            AdvancedEditingUiEvent.UpdateAppData -> {
                _advancedEditingState.update {
                    it.copy(
                        appData = appDataRepository.getAppData()
                    )
                }
            }
        }
    }
}


























