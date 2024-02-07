package com.ardrawing.sketchtrace.paywall.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.image_list.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.splash.data.DataManager
import com.ardrawing.sketchtrace.splash.domain.repository.AppDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository,
    private val imageCategoriesRepository: ImageCategoriesRepository
) : ViewModel() {

    private val _paywallState = MutableStateFlow(PaywallState())
    val paywallState = _paywallState.asStateFlow()


    fun onEvent(paywallUiEvent: PaywallUiEvent) {
        when (paywallUiEvent) {

            is PaywallUiEvent.Subscribe -> {

                if (paywallUiEvent.isSubscribed) {

                    paywallUiEvent.date?.let {
                        if (it.after(Date())) {
                            DataManager.appData.isSubscribed = true

                            viewModelScope.launch {
                                appDataRepository.setAdsVisibilityForUser()
                                imageCategoriesRepository.setUnlockedImages(it)
                                imageCategoriesRepository.setNativeItems(it)
                            }
                        }
                    }

                } else {
                    viewModelScope.launch {
                        appDataRepository.setAdsVisibilityForUser()
                    }
                }
            }

            is PaywallUiEvent.ShowHideFaq -> {
                when (paywallUiEvent.faqNumber) {
                    1 -> {
                        _paywallState.update {
                            it.copy(faq1Visibility = !it.faq1Visibility)
                        }
                    }

                    2 -> {
                        _paywallState.update {
                            it.copy(faq2Visibility = !it.faq2Visibility)
                        }
                    }

                    3 -> {
                        _paywallState.update {
                            it.copy(faq3Visibility = !it.faq3Visibility)
                        }
                    }

                    4 -> {
                        _paywallState.update {
                            it.copy(faq4Visibility = !it.faq4Visibility)
                        }
                    }
                }
            }
        }
    }

}

























