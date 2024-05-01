package com.ardrawing.sketchtrace.paywall.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.paywall.domain.repository.PaywallRepository
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
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
@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val paywallRepository: PaywallRepository,
) : ViewModel() {

    private val _paywallState = MutableStateFlow(PaywallState())
    val paywallState = _paywallState.asStateFlow()

    private val _optionsState = MutableStateFlow<PaywallOptions?>(null)
    val optionsState = _optionsState.asStateFlow()

    private val _purchasesErrorChannel = Channel<Boolean>()
    val purchasesErrorChannel = _purchasesErrorChannel.receiveAsFlow()

    private val _dismissRequestChannel = Channel<Boolean>()
    val dismissRequestChannel = _dismissRequestChannel.receiveAsFlow()

    init {
        viewModelScope.launch {

            val options = paywallRepository.getPaywallOptions(
                dismissRequest = {
                    viewModelScope.launch {
                        _dismissRequestChannel.send(true)
                    }
                }
            )

            if (options != null) {
                _optionsState.update { options }
            } else {
                _purchasesErrorChannel.send(true)
            }
        }
    }

    fun onEvent(paywallUiEvent: PaywallUiEvent) {
        when (paywallUiEvent) {
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


























