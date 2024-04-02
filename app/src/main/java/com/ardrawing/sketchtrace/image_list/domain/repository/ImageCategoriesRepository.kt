package com.ardrawing.sketchtrace.image_list.domain.repository

import com.ardrawing.sketchtrace.image_list.domain.model.images.Image
import com.ardrawing.sketchtrace.image_list.domain.model.images.ImageCategory
import com.ardrawing.sketchtrace.util.Resource
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * @author Ahmed Guedmioui
 */
interface ImageCategoriesRepository {
    suspend fun loadImageCategories(): Flow<Resource<Unit>>
    fun getImageCategories(): MutableList<ImageCategory>
    fun unlockImageItem(imageItem: Image)
    fun setGalleryAndCameraItems()
    fun setNativeItems(date: Date? = null)
    fun setUnlockedImages(date: Date? = null)

}