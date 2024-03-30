package com.ardrawing.sketchtrace.advanced_editing.presentation

import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData


/**
 * @author Ahmed Guedmioui
 */
data class AdvancedEditingState(
    val selected: Int = 0,

    val edge: Int = 0,
    val contrast: Int = 0,
    val noise: Int = 0,
    val sharpness: Int = 0,

    val isEdged: Boolean = false,
    val isContrast: Boolean = false,
    val isNoise: Boolean = false,
    val isSharpened: Boolean = false,

    val appData: AppData? = null
)