package com.ardrawing.sketchtrace.sketch.di

import com.ardrawing.sketchtrace.creation.data.repository.CreationRepositoryImpl
import com.ardrawing.sketchtrace.sketch.data.repository.SketchRepositoryImpl
import com.ardrawing.sketchtrace.sketch.domain.repository.SketchRepository
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
abstract class SketchRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSketchRepository(
        sketchRepositoryImpl: SketchRepositoryImpl
    ): SketchRepository

}












