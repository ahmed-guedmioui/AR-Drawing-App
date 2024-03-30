package com.ardrawing.sketchtrace.image_list.data.repository

import android.app.Application
import android.content.SharedPreferences
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.core.domain.usecase.UpdateSubscriptionInfo
import com.ardrawing.sketchtrace.image_list.data.mapper.toImageCategoryList
import com.ardrawing.sketchtrace.image_list.data.remote.ImageCategoryApi
import com.ardrawing.sketchtrace.image_list.domain.model.images.Image
import com.ardrawing.sketchtrace.image_list.domain.model.images.ImageCategory
import com.ardrawing.sketchtrace.image_list.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    override suspend fun loadImageCategoryList(): Flow<Resource<Unit>> {
        return flow {

            emit(Resource.Loading(true))

            val categoryListDto = try {
                imageCategoryApi.getImageCategoryList()
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

            categoryListDto?.let {
                ImageCategoriesInstance.imageCategoryList = it.toImageCategoryList().toMutableList()
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

    override fun getImageCategoryList(): MutableList<ImageCategory> {
        return ImageCategoriesInstance.imageCategoryList ?: mutableListOf()
    }

    override suspend fun unlockImageItem(imageItem: Image) {
        prefs.edit().putBoolean(imageItem.prefsId, false).apply()
    }

    override suspend fun setUnlockedImages(date: Date?) {
        date?.let {
            UpdateSubscriptionInfo(it, appDataRepository.getAppData()).invoke()
        }

        // When user is subscribed all images will be unlocked
        if (appDataRepository.getAppData()?.isSubscribed == true) {
            getImageCategoryList().forEach { categoryItem ->
                categoryItem.imageList.forEach { image ->
                    image.locked = false
                }
            }

            return
        }

        // When user is not subscribed unlock only the image the user manually unlocked by watching an ad
        getImageCategoryList().forEach { categoryItem ->
            categoryItem.imageList.forEach { image ->
                if (image.locked) {
                    prefs.getBoolean(image.prefsId, true).let { locked ->
                        image.locked = locked
                    }
                }
            }
        }
    }

    override suspend fun setNativeItems(date: Date?) {
        date?.let {
            UpdateSubscriptionInfo(it, appDataRepository.getAppData()).invoke()
        }

        if (appDataRepository.getAppData()?.isSubscribed == true) {

            val iterator: MutableIterator<ImageCategory> = getImageCategoryList().iterator()
            while (iterator.hasNext()) {
                val categoryItem: ImageCategory = iterator.next()
                if (categoryItem.imageCategoryName == "native") {
                    iterator.remove() // Safely remove the element using the iterator
                }
            }
            return
        }

        val nativeItem = ImageCategory(
            imageCategoryName = "native",
            categoryId = -1,
            imageList = emptyList()
        )

        appDataRepository.getAppData()?.nativeRate?.let { nativeRate ->
            var index = nativeRate
            while (index < getImageCategoryList().size) {
                getImageCategoryList().add(index, nativeItem)
                index += nativeRate + 1
            }
        }

    }

    override suspend fun setGalleryAndCameraItems() {
        getImageCategoryList().add(
            0,
            ImageCategory(
                imageCategoryName = "gallery and camera",
                categoryId = -1,
                imageList = emptyList(),
            )
        )

        getImageCategoryList().add(
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
    var imageCategoryList: MutableList<ImageCategory>? = null
}


















