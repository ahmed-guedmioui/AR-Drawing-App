package com.ardrawing.sketchtrace.core.di

import com.ardrawing.sketchtrace.core.data.repository.AppDataRepositoryImpl
import com.ardrawing.sketchtrace.core.data.repository.CoreRepositoryImpl
import com.ardrawing.sketchtrace.core.data.repository.ads.AdmobAppOpenRepositoryImpl
import com.ardrawing.sketchtrace.core.data.repository.ads.InterRepositoryImpl
import com.ardrawing.sketchtrace.core.data.repository.ads.NativeRepositoryImpl
import com.ardrawing.sketchtrace.core.data.repository.ads.RewardedManagerImpl
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.core.domain.repository.CoreRepository
import com.ardrawing.sketchtrace.core.domain.repository.ads.AdmobAppOpenRepository
import com.ardrawing.sketchtrace.core.domain.repository.ads.InterRepository
import com.ardrawing.sketchtrace.core.domain.repository.ads.NativeRepository
import com.ardrawing.sketchtrace.core.domain.repository.ads.RewardedRepository
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
        admobAppOpenRepositoryImpl: AdmobAppOpenRepositoryImpl
    ): AdmobAppOpenRepository

    @Binds
    @Singleton
    abstract fun bindInterRepository(
        interManagerImpl: InterRepositoryImpl
    ): InterRepository

    @Binds
    @Singleton
    abstract fun bindRewardedRepository(
        rewardedManagerImpl: RewardedManagerImpl
    ): RewardedRepository

    @Binds
    @Singleton
    abstract fun bindNativeRepository(
        nativeManagerImpl: NativeRepositoryImpl
    ): NativeRepository

}












