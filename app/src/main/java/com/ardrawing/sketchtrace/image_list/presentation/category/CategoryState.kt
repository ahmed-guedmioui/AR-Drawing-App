package com.ardrawing.sketchtrace.image_list.presentation.category

import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.image_list.domain.model.images.Image
import com.ardrawing.sketchtrace.image_list.domain.model.images.ImageCategory
import com.ardrawing.sketchtrace.my_creation.domian.model.Creation


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