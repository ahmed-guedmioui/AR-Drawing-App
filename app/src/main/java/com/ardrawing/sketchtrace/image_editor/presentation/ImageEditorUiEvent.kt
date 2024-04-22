package com.ardrawing.sketchtrace.image_editor.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed class ImageEditorUiEvent() {
    data class Select(val selected: Int) : ImageEditorUiEvent()

    data class SetEdge(val edge: Int) : ImageEditorUiEvent()
    data class SetContrast(val contrast: Int) : ImageEditorUiEvent()
    data class SetNoise(val noise: Int) : ImageEditorUiEvent()
    data class SetSharpness(val sharpness: Int) : ImageEditorUiEvent()

    object UpdateAppData: ImageEditorUiEvent()
}