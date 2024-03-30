package com.ardrawing.sketchtrace.image_list.presentation.category


/**
 * @author Ahmed Guedmioui
 */
sealed class CategoryUiEvents {
    data class UpdateCategoryPositionAndIsTrace(
        val categoryPosition: Int,
        val isTrace: Boolean
    ) : CategoryUiEvents()

    data class OnImageClick(
        val imagePosition: Int
    ) : CategoryUiEvents()


    object UnlockImage : CategoryUiEvents()
}