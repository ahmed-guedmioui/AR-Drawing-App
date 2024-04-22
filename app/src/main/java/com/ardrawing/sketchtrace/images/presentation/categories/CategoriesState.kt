package com.ardrawing.sketchtrace.images.presentation.categories

import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.images.domain.model.images.Image
import com.ardrawing.sketchtrace.images.domain.model.images.ImageCategory


/**
 * @author Ahmed Guedmioui
 */
data class CategoriesState(
    val isTrace: Boolean = false,
    val isGallery: Boolean = false,

    val imageCategoryList: List<ImageCategory> = emptyList(),

    val imagePosition: Int = 0,
    val clickedImageItem: Image? = null,
    val imageCategory: ImageCategory? = null,

    val appData: AppData? = null
)