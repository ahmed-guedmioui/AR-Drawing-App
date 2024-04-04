package com.ardrawing.sketchtrace.image_list.data.repository

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.core.domain.usecase.UpdateSubscriptionExpireDate
import com.ardrawing.sketchtrace.image_list.data.mapper.toImageCategoryList
import com.ardrawing.sketchtrace.image_list.data.remote.ImageCategoryApi
import com.ardrawing.sketchtrace.image_list.data.remote.respond.images.ImageCategoriesDto
import com.ardrawing.sketchtrace.image_list.domain.model.images.Image
import com.ardrawing.sketchtrace.image_list.domain.model.images.ImageCategory
import com.ardrawing.sketchtrace.image_list.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.util.Resource
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException
import java.util.Date
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
class ImageCategoriesRepositoryImpl @Inject constructor(
    private val application: Application,
    private val prefs: SharedPreferences,
    private val imageCategoryApi: ImageCategoryApi,
    private val appDataRepository: AppDataRepository
) : ImageCategoriesRepository {

    override suspend fun loadImageCategories(): Flow<Resource<Unit>> {
        return flow {

            emit(Resource.Loading(true))

            val remoteImageCategoriesDto = try {
                imageCategoryApi.getImageCategories()
            } catch (e: IOException) {
                e.printStackTrace()
                emit(
                    Resource.Error(application.getString(R.string.error_loading_images))
                )
                emit(Resource.Loading(false))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(
                    Resource.Error(application.getString(R.string.error_loading_images))
                )
                emit(Resource.Loading(false))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(
                    Resource.Error(application.getString(R.string.error_loading_images))
                )
                emit(Resource.Loading(false))
                return@flow
            }

            remoteImageCategoriesDto?.let { imageCategoriesDto ->

                ImageCategoriesInstance.imageCategories =
                    imageCategoriesDto.toImageCategoryList().toMutableList()

                updateImageCategoriesJsonString(imageCategoriesDto)

                emit(Resource.Success())
                emit(Resource.Loading(false))
                return@flow
            }

            emit(
                Resource.Error(application.getString(R.string.error_loading_images))
            )
            emit(Resource.Loading(false))
        }
    }

    override fun getImageCategories(): MutableList<ImageCategory> {

        ImageCategoriesInstance.imageCategories?.let { imageCategories ->
            return imageCategories
        }

        // When we get, it means we lost the list for some reason.
        // Now we reload it from cache
        convertJsonStringToImageCategories()?.let { imageCategoriesDto ->

            setUnlockedImages()
            setNativeItems()
            setGalleryAndCameraItems()

            return imageCategoriesDto.toImageCategoryList().toMutableList()
        }

        return mutableListOf()
    }

    private fun updateImageCategoriesJsonString(
        imageCategoriesDto: ImageCategoriesDto
    ) {

        val imageCategoriesJsonString =
            convertImageCategoriesToJsonString(imageCategoriesDto)

        prefs.edit()
            .putString("ImageCategoriesJson", imageCategoriesJsonString)
            .apply()
    }

    private fun convertImageCategoriesToJsonString(
        imageCategoriesDto: ImageCategoriesDto
    ): String {
        return Gson().toJson(imageCategoriesDto)
    }

    private fun convertJsonStringToImageCategories(): ImageCategoriesDto? {
        val imageCategoriesJsonString =
            prefs.getString("ImageCategoriesJson", null)

        return Gson().fromJson(
            imageCategoriesJsonString, ImageCategoriesDto::class.java
        )
    }

    override fun unlockImageItem(imageItem: Image) {
        prefs.edit().putBoolean(imageItem.prefsId, false).apply()
    }

    override fun setUnlockedImages(date: Date?) {
        date?.let {
            UpdateSubscriptionExpireDate(it, appDataRepository).invoke()
        }

        // When user is subscribed all images will be unlocked
        if (appDataRepository.getAppData().isSubscribed) {
            getImageCategories().forEach { categoryItem ->
                categoryItem.imageList.forEach { image ->
                    image.locked = false
                }
            }

            return
        }

        // When user is not subscribed unlock only the image the user manually unlocked by watching an ad
        getImageCategories().forEach { categoryItem ->
            categoryItem.imageList.forEach { image ->
                if (image.locked) {
                    prefs.getBoolean(image.prefsId, true).let { locked ->
                        image.locked = locked
                    }
                }
            }
        }
    }

    override fun setNativeItems(date: Date?) {
        date?.let {
            UpdateSubscriptionExpireDate(it, appDataRepository).invoke()
        }

        if (appDataRepository.getAppData().isSubscribed) {

            val iterator: MutableIterator<ImageCategory> = getImageCategories().iterator()
            while (iterator.hasNext()) {
                val categoryItem: ImageCategory = iterator.next()
                if (categoryItem.imageCategoryName == "native") {
                    iterator.remove()
                }
            }
            return
        }

        val nativeItem = ImageCategory(
            imageCategoryName = "native",
            categoryId = -1,
            imageList = emptyList()
        )

        appDataRepository.getAppData().nativeRate.let { nativeRate ->
            var index = nativeRate
            while (index < getImageCategories().size) {
                getImageCategories().add(index, nativeItem)
                index += nativeRate + 1
            }
        }

    }

    override fun setGalleryAndCameraItems() {
        getImageCategories().add(
            0,
            ImageCategory(
                imageCategoryName = "gallery and camera",
                categoryId = -1,
                imageList = emptyList(),
            )
        )

        getImageCategories().add(
            1,
            ImageCategory(
                imageCategoryName = "explore",
                categoryId = -1,
                imageList = emptyList(),
            )
        )
    }
}

object ImageCategoriesInstance {
    var imageCategories: MutableList<ImageCategory>? = null
}


















