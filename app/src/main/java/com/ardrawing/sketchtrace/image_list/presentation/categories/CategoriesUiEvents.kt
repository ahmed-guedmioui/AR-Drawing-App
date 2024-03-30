package com.ardrawing.sketchtrace.image_list.presentation.categories

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
}