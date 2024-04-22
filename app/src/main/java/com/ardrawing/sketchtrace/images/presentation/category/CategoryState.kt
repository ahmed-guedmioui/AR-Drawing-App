package com.ardrawing.sketchtrace.images.presentation.category

import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.images.domain.model.images.Image
import com.ardrawing.sketchtrace.images.domain.model.images.ImageCategory


/**
 * @author Ahmed Guedmioui
 */
data class CategoryState(
    val isTrace: Boolean = false,

    val imageCategoryList: List<ImageCategory> = emptyList(),

    val imagePosition: Int = 0,
    val clickedImageItem: Image? = null,

    val categoryPosition: Int = 0,
    val imageCategory: ImageCategory? = null,

    val appData: AppData? = null
)