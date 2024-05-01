package com.ardrawing.sketchtrace.paywall.di

import com.ardrawing.sketchtrace.paywall.data.repository.PaywallRepositoryImpl
import com.ardrawing.sketchtrace.paywall.domain.repository.PaywallRepository
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Ahmed Guedmioui
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PaywallRepositoryModule {

    @OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
    @Binds
    @Singleton
    abstract fun bindPaywallRepository(
        paywallRepositoryImpl: PaywallRepositoryImpl
    ): PaywallRepository

}












