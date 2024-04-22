package com.ardrawing.sketchtrace.language.di

import com.ardrawing.sketchtrace.language.data.repository.LanguageRepositoryImpl
import com.ardrawing.sketchtrace.language.domain.repository.LanguageRepository
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
abstract class LanguageRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCoreRepository(
       languageRepositoryImpl: LanguageRepositoryImpl
    ): LanguageRepository


}












