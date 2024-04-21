package com.ardrawing.sketchtrace.core.di

import com.ardrawing.sketchtrace.core.data.repository.AppDataRepositoryImpl
import com.ardrawing.sketchtrace.core.data.repository.CoreRepositoryImpl
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.core.domain.repository.CoreRepository
import com.ardrawing.sketchtrace.util.ads.RewardedManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.scopes.ActivityScoped
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

}












