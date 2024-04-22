package com.ardrawing.sketchtrace.images.domain.repository

import com.ardrawing.sketchtrace.images.domain.model.images.Image
import com.ardrawing.sketchtrace.images.domain.model.images.ImageCategory
import com.ardrawing.sketchtrace.core.data.util.Resource
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