package com.ardrawing.sketchtrace.image_list.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.App
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.image_list.domain.repository.ImageCategoriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val imageCategoriesRepository: ImageCategoriesRepository,
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    private val _categoriesState = MutableStateFlow(CategoriesState())
    val categoriesState = _categoriesState.asStateFlow()

    private val _navigateToDrawingChannel = Channel<Boolean>()
    val navigateToDrawingChannel = _navigateToDrawingChannel.receiveAsFlow()

    private val _unlockImageChannel = Channel<Boolean>()
    val unlockImageChannel = _unlockImageChannel.receiveAsFlow()

    private val _appData = MutableStateFlow<AppData?>(null)
    val appData = _appData.asStateFlow()

    init {
        viewModelScope.launch {
            _categoriesState.update {
                it.copy(
                    imageCategoryList = imageCategoriesRepository.getImageCategoryList(),
                    appData = appDataRepository.getAppData()
                )
            }
        }

        _appData.update {
            appDataRepository.getAppData()
        }
    }

    fun onEvent(event: CategoriesUiEvents) {
        when (event) {
            is CategoriesUiEvents.UpdateIsTrace -> {
                _categoriesState.update {
                    it.copy(
                        isTrace = event.isTrace
                    )
                }
            }

            is CategoriesUiEvents.OnImageClick -> {

                viewModelScope.launch {
                    val imageCategory =
                        categoriesState.value.imageCategoryList[event.categoryPosition]
                    val clickedImageItem = imageCategory.imageList[event.imagePosition]

                    _categoriesState.update {
                        it.copy(
                            imagePosition = event.imagePosition,
                            clickedImageItem = clickedImageItem,
                            imageCategory = imageCategory
                        )
                    }

                    if (clickedImageItem.locked) {
                        _unlockImageChannel.send(true)
                    } else
                        _navigateToDrawingChannel.send(true)
                }
            }

            CategoriesUiEvents.UnlockImage -> {

                viewModelScope.launch {
                    categoriesState.value.clickedImageItem?.let {
                        imageCategoriesRepository.unlockImageItem(it)

                        categoriesState.value.clickedImageItem?.locked = false

                        categoriesState.value.imageCategory?.adapter?.notifyItemChanged(
                            categoriesState.value.imagePosition
                        )
                    }

                }
            }

            is CategoriesUiEvents.UpdateIsGallery -> {
                _categoriesState.update {
                    it.copy(
                        isGallery = event.isGallery
                    )
                }
            }

        }
    }
}



























