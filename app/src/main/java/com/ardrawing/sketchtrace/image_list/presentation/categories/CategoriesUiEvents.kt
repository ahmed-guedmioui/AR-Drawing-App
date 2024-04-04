package com.ardrawing.sketchtrace.image_list.presentation.categories

import com.ardrawing.sketchtrace.advanced_editing.presentation.AdvancedEditingUiEvent

/**
 * @author Ahmed Guedmioui
 */
sealed class CategoriesUiEvents {
    data class UpdateIsTrace(
        val isTrace: Boolean
    ) : CategoriesUiEvents()

    data class OnImageClick(
        val categoryPosition: Int,
        val imagePosition: Int
    ) : CategoriesUiEvents()

    data class UpdateIsGallery(
        val isGallery: Boolean
    ) : CategoriesUiEvents()

    object UnlockImage : CategoriesUiEvents()
    object UpdateAppData: CategoriesUiEvents()

}