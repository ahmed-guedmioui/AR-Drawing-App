package com.ardrawing.sketchtrace.images.data.mapper

import com.ardrawing.sketchtrace.images.data.remote.respond.images.ImageCategoriesDto
import com.ardrawing.sketchtrace.images.data.remote.respond.images.ImageDto
import com.ardrawing.sketchtrace.images.domain.model.images.Image
import com.ardrawing.sketchtrace.images.domain.model.images.ImageCategory

/**
 * @author Ahmed Guedmioui
 */

fun ImageCategoriesDto.toImageCategoryList(): List<ImageCategory> {
    var currentCategoryId = 1 // Start category ID from 1

    return category_list?.map { categoryDto ->
        ImageCategory(
            categoryId = currentCategoryId,
            imageCategoryName = categoryDto.category_name.orEmpty(),
            imageList = categoryDto.images?.map {
                it.toImage(currentCategoryId)
            } ?: emptyList()
        ).also { currentCategoryId++ }
    } ?: emptyList()
}

fun ImageDto.toImage(currentCategoryId: Int): Image {
    return Image(
        prefsId = "${currentCategoryId}_${id}",
        id = id ?: 0,
        image = image.orEmpty(),
        locked = locked ?: false
    )
}










