package com.ardrawing.sketchtrace.image_list.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class CategoryViewModel @Inject constructor(
    private val imageCategoriesRepository: ImageCategoriesRepository,
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    private val _categoryState = MutableStateFlow(CategoryState())
    val categoryState = _categoryState.asStateFlow()

    private val _navigateToDrawingChannel = Channel<Boolean>()
    val navigateToDrawingChannel = _navigateToDrawingChannel.receiveAsFlow()

    private val _unlockImageChannel = Channel<Boolean>()
    val unlockImageChannel = _unlockImageChannel.receiveAsFlow()

    private val _appData = MutableStateFlow<AppData?>(null)
    val appData = _appData.asStateFlow()

    init {
        viewModelScope.launch {
            _categoryState.update {
                it.copy(
                    imageCategoryList = imageCategoriesRepository.getImageCategories(),
                    appData = appDataRepository.getAppData()
                )
            }
        }
        _appData.update {
            appDataRepository.getAppData()
        }
    }

    fun onEvent(event: CategoryUiEvents) {
        when (event) {

            is CategoryUiEvents.UpdateCategoryPositionAndIsTrace -> {
                val imageCategory =
                    categoryState.value.imageCategoryList[event.categoryPosition]

                _categoryState.update {
                    it.copy(
                        categoryPosition = event.categoryPosition,
                        isTrace = event.isTrace,
                        imageCategory = imageCategory
                    )
                }
            }

            is CategoryUiEvents.OnImageClick -> {

                viewModelScope.launch {
                    val categoryPosition = categoryState.value.categoryPosition
                    val imageCategory = categoryState.value.imageCategoryList[categoryPosition]
                    val clickedImageItem = imageCategory.imageList[event.imagePosition]

                    _categoryState.update {
                        it.copy(
                            imagePosition = event.imagePosition,
                            clickedImageItem = clickedImageItem,
                        )
                    }

                    if (clickedImageItem.locked) {
                        _unlockImageChannel.send(true)
                    } else {
                        _navigateToDrawingChannel.send(true)
                    }
                }
            }

            CategoryUiEvents.UnlockImage -> {

                viewModelScope.launch {
                    categoryState.value.clickedImageItem?.let {
                        imageCategoriesRepository.unlockImageItem(it)

                        categoryState.value.clickedImageItem?.locked = false

                        categoryState.value.imageCategory?.adapter?.notifyItemChanged(
                            categoryState.value.imagePosition
                        )
                    }

                }
            }

            CategoryUiEvents.UpdateAppData -> {
                _categoryState.update {
                    it.copy(
                        appData = appDataRepository.getAppData()
                    )
                }
            }
        }
    }
}



























