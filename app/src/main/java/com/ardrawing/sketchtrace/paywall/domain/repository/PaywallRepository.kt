package com.ardrawing.sketchtrace.paywall.domain.repository

import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions

/**
 * @author Ahmed Guedmioui
 */
interface PaywallRepository {
    fun subscription()

    @OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
    suspend fun getPaywallOptions(
        dismissRequest: () -> Unit
    ): PaywallOptions?
}