package com.ardrawing.sketchtrace.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.images.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.core.domain.repository.CoreRepository
import com.ardrawing.sketchtrace.splash.presentation.util.ShouldShowUpdateDialog
import com.ardrawing.sketchtrace.core.data.util.Resource
import com.ardrawing.sketchtrace.paywall.domain.repository.PaywallRepository
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
class SplashViewModel @Inject constructor(
    private val coreRepository: CoreRepository,
    private val appDataRepository: AppDataRepository,
    private val imageCategoriesRepository: ImageCategoriesRepository,
    private val paywallRepository: PaywallRepository
) : ViewModel() {

    private val _splashState = MutableStateFlow(SplashState())
    val splashState = _splashState.asStateFlow()


    private val _updateDialogState = MutableStateFlow(-1)
    val updateDialogState = _updateDialogState.asStateFlow()

    private val _continueAppChannel = Channel<Boolean>()
    val continueAppChannel = _continueAppChannel.receiveAsFlow()

    private val _showErrorToastChannel = Channel<Boolean>()
    val showErrorToastChannel = _showErrorToastChannel.receiveAsFlow()

    init {
        navigationValidator()
        getData()
        getImages()
    }

    fun onEvent(splashUiEvent: SplashUiEvent) {
        when (splashUiEvent) {
            SplashUiEvent.TryAgain -> {
                coreRepository.initPurchases()
                navigationValidator()
                getData()
                getImages()
            }

            is SplashUiEvent.ContinueApp -> {
                viewModelScope.launch {
                    _continueAppChannel.send(true)
                }
            }

            SplashUiEvent.AlreadySubscribed -> {
                viewModelScope.launch {
                    appDataRepository.updateShowAdsForThisUser()
                    imageCategoriesRepository.setUnlockedImages()
                    imageCategoriesRepository.setNativeItems()
                }
            }

            is SplashUiEvent.OnAdmobConsent -> {
                coreRepository.updateCanShowAdmobAds(splashUiEvent.canShowAdmobAds)
            }
        }
    }

    private fun navigationValidator() {
        _splashState.update {
            it.copy(
                isLanguageShown = coreRepository.isLanguageShown(),
                isOnboardingShown = coreRepository.isOnboardingShown(),
                isGetStartedShown = coreRepository.isGetStartedShown(),
            )
        }
    }

    private fun getData() {
        viewModelScope.launch {

            appDataRepository.loadAppData().collect { appDataResult ->
                when (appDataResult) {
                    is Resource.Error -> {
                        _showErrorToastChannel.send(true)
                    }

                    is Resource.Loading -> Unit

                    is Resource.Success -> {

                        paywallRepository.subscription()

                        _splashState.update {
                            it.copy(
                                appData = appDataRepository.getAppData()
                            )
                        }

                        when (ShouldShowUpdateDialog(splashState.value.appData).invoke()) {
                            1 -> {
                                _updateDialogState.update { 1 }
                            }

                            2 -> {
                                _updateDialogState.update { 2 }
                            }

                            0 -> {
                                _updateDialogState.update { 0 }

                                if (splashState.value.areImagesLoaded) {

                                    appDataRepository.updateShowAdsForThisUser()
                                    imageCategoriesRepository.setUnlockedImages()
                                    imageCategoriesRepository.setNativeItems()
                                    imageCategoriesRepository.setGalleryAndCameraItems()

                                    _continueAppChannel.send(true)
                                }
                            }
                        }

                        _splashState.update {
                            it.copy(isAppDataLoaded = true)
                        }
                    }
                }
            }
        }
    }

    private fun getImages() {
        viewModelScope.launch {
            imageCategoriesRepository.loadImageCategories().collect { imagesResult ->
                when (imagesResult) {
                    is Resource.Error -> {
                        _showErrorToastChannel.send(true)
                    }

                    is Resource.Loading -> Unit

                    is Resource.Success -> {
                        _splashState.update {
                            it.copy(areImagesLoaded = true)
                        }

                        if (splashState.value.isAppDataLoaded && updateDialogState.value == 0) {

                            appDataRepository.updateShowAdsForThisUser()
                            imageCategoriesRepository.setUnlockedImages()
                            imageCategoriesRepository.setNativeItems()
                            imageCategoriesRepository.setGalleryAndCameraItems()

                            _continueAppChannel.send(true)
                        }

                    }
                }
            }
        }
    }
}


























