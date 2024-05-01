package com.ardrawing.sketchtrace.paywall.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.images.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository,
    private val imageCategoriesRepository: ImageCategoriesRepository
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
        try {
            // TODO: Create a repository for this and and abstract it
            Purchases.sharedInstance.getOfferingsWith(
                onError = {
                    viewModelScope.launch {
                        _purchasesErrorChannel.send(true)
                    }
                },
                onSuccess = { offerings ->
                    offerings.current?.let { currentOffering ->
                        initOptions(currentOffering) {
                            viewModelScope.launch {
                                _dismissRequestChannel.send(true)
                            }
                        }
                    }
                },
            )
        } catch (e: Exception) {
            viewModelScope.launch {
                _purchasesErrorChannel.send(true)
            }
        }
    }

    fun onEvent(paywallUiEvent: PaywallUiEvent) {
        when (paywallUiEvent) {
//            is PaywallUiEvent.Subscribe -> {
//                if (paywallUiEvent.isSubscribed) {
//
//                    paywallUiEvent.date?.let {
//                        if (it.after(Date())) {
//                            appDataRepository.updateIsSubscribed(true)
//
//                            viewModelScope.launch {
//                                appDataRepository.updateShowAdsForThisUser()
//                                imageCategoriesRepository.setUnlockedImages(it)
//                                imageCategoriesRepository.setNativeItems(it)
//                            }
//                        }
//                    }
//
//                } else {
//                    viewModelScope.launch {
//                        appDataRepository.updateShowAdsForThisUser()
//                    }
//                }
//            }

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

    private fun initOptions(
        offering: Offering,
        dismissRequest: () -> Unit
    ) {
        _optionsState.update {
            PaywallOptions
                .Builder(dismissRequest)
                .setOffering(offering)
                .setListener(
                    object : PaywallListener {
                        override fun onPurchaseCompleted(
                            customerInfo: CustomerInfo, storeTransaction: StoreTransaction
                        ) {
                            val date = customerInfo.getExpirationDateForEntitlement(
                                BuildConfig.ENTITLEMENT
                            )

                            subscribe(isSubscribed = true, date = date)
                            Log.d("REVENUE_CUT", "onPurchaseCompleted")
                        }

                        override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                            val date = customerInfo.getExpirationDateForEntitlement(
                                BuildConfig.ENTITLEMENT
                            )

                            subscribe(isSubscribed = true, date = date)
                            Log.d("REVENUE_CUT", "onRestoreCompleted")
                        }

                        override fun onPurchaseCancelled() {
                            Log.d("REVENUE_CUT", "onPurchaseCancelled")
                            subscribe(isSubscribed = false)
                        }

                        override fun onPurchaseError(error: PurchasesError) {
                            Log.d("REVENUE_CUT", "onPurchaseError")
                            subscribe(isSubscribed = false)
                        }

                        override fun onRestoreError(error: PurchasesError) {
                            Log.d("REVENUE_CUT", "onRestoreError")
                            subscribe(isSubscribed = false)
                        }
                    }
                ).build()
        }
    }

    private fun subscribe(
        isSubscribed: Boolean,
        date: Date? = null
    ) {
        if (isSubscribed) {
            date?.let {
                if (it.after(Date())) {
                    appDataRepository.updateIsSubscribed(true)

                    viewModelScope.launch {
                        appDataRepository.updateShowAdsForThisUser()
                        imageCategoriesRepository.setUnlockedImages(it)
                        imageCategoriesRepository.setNativeItems(it)
                    }
                }
            }

        } else {
            viewModelScope.launch {
                appDataRepository.updateShowAdsForThisUser()
            }
        }
    }

}


























