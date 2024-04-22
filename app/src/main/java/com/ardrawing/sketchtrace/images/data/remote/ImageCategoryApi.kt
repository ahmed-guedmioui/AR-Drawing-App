package com.ardrawing.sketchtrace.images.data.remote

import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.images.data.remote.respond.images.ImageCategoriesDto
import retrofit2.http.GET

/**
 * @author Ahmed Guedmioui
 */
interface ImageCategoryApi {


    @GET(IMAGES_PATH)
    suspend fun getImageCategories(): ImageCategoriesDto?

    companion object {
        const val IMAGES_BASE_URL = BuildConfig.IMAGES_BASE_URL
        const val IMAGES_PATH = BuildConfig.IMAGES_PATH
    }

}