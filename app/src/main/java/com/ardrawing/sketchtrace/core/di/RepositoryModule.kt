package com.ardrawing.sketchtrace.core.di

import com.ardrawing.sketchtrace.core.data.repository.AppDataRepositoryImpl
import com.ardrawing.sketchtrace.core.data.repository.CoreRepositoryImpl
import com.ardrawing.sketchtrace.core.data.util.ads.AdmobAppOpenManagerImpl
import com.ardrawing.sketchtrace.core.data.util.ads.InterstitialMangerImpl
import com.ardrawing.sketchtrace.core.data.util.ads.NativeManagerImpl
import com.ardrawing.sketchtrace.core.data.util.ads.RewardedManagerImpl
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.core.domain.repository.CoreRepository
import com.ardrawing.sketchtrace.core.domain.repository.ads.AppOpenManager
import com.ardrawing.sketchtrace.core.domain.repository.ads.InterstitialManger
import com.ardrawing.sketchtrace.core.domain.repository.ads.NativeManager
import com.ardrawing.sketchtrace.core.domain.repository.ads.RewardedManger
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
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCoreRepository(
        coreRepositoryImpl: CoreRepositoryImpl
    ): CoreRepository

    @Binds
    @Singleton
    abstract fun bindAppDataRepository(
        appDataRepositoryImpl: AppDataRepositoryImpl
    ): AppDataRepository

    @Binds
    @Singleton
    abstract fun bindAdmobAppOpenRepository(
        admobAppOpenRepositoryImpl: AdmobAppOpenManagerImpl
    ): AppOpenManager

    @Binds
    @Singleton
    abstract fun bindInterRepository(
        interManagerImpl: InterstitialMangerImpl
    ): InterstitialManger

    @Binds
    @Singleton
    abstract fun bindRewardedRepository(
        rewardedManagerImpl: RewardedManagerImpl
    ): RewardedManger

    @Binds
    @Singleton
    abstract fun bindNativeRepository(
        nativeManagerImpl: NativeManagerImpl
    ): NativeManager

}












