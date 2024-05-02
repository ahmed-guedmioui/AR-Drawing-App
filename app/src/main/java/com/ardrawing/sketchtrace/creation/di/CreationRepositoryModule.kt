package com.ardrawing.sketchtrace.creation.di

import com.ardrawing.sketchtrace.creation.data.repository.CreationRepositoryImpl
import com.ardrawing.sketchtrace.creation.domian.repository.CreationRepository
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
abstract class CreationRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindACreationRepository(
        creationRepositoryImpl: CreationRepositoryImpl
    ): CreationRepository

}












