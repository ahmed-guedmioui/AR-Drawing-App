package com.ardrawing.sketchtrace.image_list.presentation.category

import com.ardrawing.sketchtrace.image_list.presentation.categories.CategoriesUiEvents


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