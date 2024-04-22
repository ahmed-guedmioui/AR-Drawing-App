package com.ardrawing.sketchtrace.images.presentation.category


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
    object UpdateAppData: CategoryUiEvents()

}