package com.ardrawing.sketchtrace.image_editor.presentation

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
class ImageEditorViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    private val _imageEditorState = MutableStateFlow(ImageEditorState())
    val advancedEditingState = _imageEditorState.asStateFlow()

    

    init {
        
        _imageEditorState.update {
            it.copy(
                appData = appDataRepository.getAppData()
            )
        }
    }

    fun onEvent(imageEditorUiEvent: ImageEditorUiEvent) {
        when (imageEditorUiEvent) {
            is ImageEditorUiEvent.Select -> {

                if (advancedEditingState.value.selected == imageEditorUiEvent.selected) {
                    _imageEditorState.update {
                        it.copy(selected = 0)
                    }
                } else {
                    _imageEditorState.update {
                        it.copy(
                            selected = imageEditorUiEvent.selected
                        )
                    }
                }
            }

            is ImageEditorUiEvent.SetContrast -> {
                _imageEditorState.update {
                    it.copy(
                        contrast = imageEditorUiEvent.contrast,
                        isContrast = true
                    )
                }
            }

            is ImageEditorUiEvent.SetEdge -> {
                _imageEditorState.update {
                    it.copy(
                        edge = imageEditorUiEvent.edge,
                        isEdged = true
                    )
                }
            }

            is ImageEditorUiEvent.SetNoise -> {
                _imageEditorState.update {
                    it.copy(
                        noise = imageEditorUiEvent.noise,
                        isNoise = true
                    )
                }
            }

            is ImageEditorUiEvent.SetSharpness -> {
                _imageEditorState.update {
                    it.copy(
                        sharpness = imageEditorUiEvent.sharpness,
                        isSharpened = true
                    )
                }
            }

            ImageEditorUiEvent.UpdateAppData -> {
                _imageEditorState.update {
                    it.copy(
                        appData = appDataRepository.getAppData()
                    )
                }
            }
        }
    }
}


























