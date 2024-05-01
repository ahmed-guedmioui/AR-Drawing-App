package com.ardrawing.sketchtrace.paywall.data.repository

import android.util.Log
import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.core.domain.usecase.UpdateSubscriptionExpireDate
import com.ardrawing.sketchtrace.images.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.paywall.domain.repository.PaywallRepository
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@ExperimentalPreviewRevenueCatUIPurchasesAPI
class PaywallRepositoryImpl @Inject constructor(
    private val appDataRepository: AppDataRepository,
    private val imageCategoriesRepository: ImageCategoriesRepository
) : PaywallRepository {

    override fun subscription() {
        Purchases.sharedInstance.getCustomerInfo(
            object : ReceiveCustomerInfoCallback {
                override fun onError(error: PurchasesError) {
                    appDataRepository.updateIsSubscribed(false)
                }

                override fun onReceived(customerInfo: CustomerInfo) {
                    val date = customerInfo.getExpirationDateForEntitlement(
                        BuildConfig.ENTITLEMENT
                    )
                    date?.let {
                        if (it.after(Date())) {
                            appDataRepository.updateIsSubscribed(true)
                        }
                    }
                    UpdateSubscriptionExpireDate(
                        date, appDataRepository
                    ).invoke()
                    appDataRepository.updateShowAdsForThisUser()
                }
            }
        )
    }

    override suspend fun getPaywallOptions(
        dismissRequest: () -> Unit
    ): PaywallOptions? {

        val offering = getOffering()

        var options: PaywallOptions? = null
        val optionsJob = CoroutineScope(Dispatchers.IO).launch {
            options = PaywallOptions
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

        optionsJob.join()
        return options
    }

    private suspend fun getOffering(): Offering? {
        var offering: Offering? = null

        val offeringJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                Purchases.sharedInstance.getOfferingsWith(
                    onError = {
                        offering = null
                    },
                    onSuccess = { offerings ->
                        offerings.current?.let { currentOffering ->
                            offering = currentOffering
                        }
                    },
                )
            } catch (e: Exception) {
                offering = null
            }
        }

        offeringJob.join()
        return offering
    }

    private fun subscribe(
        isSubscribed: Boolean,
        date: Date? = null
    ) {
        if (isSubscribed) {
            date?.let {
                if (it.after(Date())) {
                    appDataRepository.updateIsSubscribed(true)
                    appDataRepository.updateShowAdsForThisUser()
                    imageCategoriesRepository.setUnlockedImages(it)
                    imageCategoriesRepository.setNativeItems(it)
                }
            }

        } else {
            appDataRepository.updateShowAdsForThisUser()
        }
    }
}












