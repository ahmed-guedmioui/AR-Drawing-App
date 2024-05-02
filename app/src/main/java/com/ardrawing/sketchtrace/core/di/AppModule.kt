package com.ardrawing.sketchtrace.core.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.ardrawing.sketchtrace.core.data.remote.AppDataApi
import com.ardrawing.sketchtrace.core.data.repository.ads.AdmobAppOpenManagerImpl
import com.ardrawing.sketchtrace.core.data.repository.ads.InterstitialMangerImpl
import com.ardrawing.sketchtrace.core.data.repository.ads.NativeManagerImpl
import com.ardrawing.sketchtrace.core.data.repository.ads.RewardedManagerImpl
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * @author Ahmed Guedmioui
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    @Provides
    @Singleton
    fun providesAppDataApi(): AppDataApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(AppDataApi.ADS_BASE_URL)
            .client(client)
            .build()
            .create(AppDataApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSharedPref(app: Application): SharedPreferences {
        return app.getSharedPreferences(
            "ar_drawing_med_prefs_file", Context.MODE_PRIVATE
        )
    }

    @Provides
    @Singleton
    fun providesAdmobAppOpenManager(
        appDataRepository: AppDataRepository,
        prefs: SharedPreferences,
        app: Application
    ): AdmobAppOpenManagerImpl {
        return AdmobAppOpenManagerImpl(appDataRepository, prefs, app)
    }

    @Provides
    @Singleton
    fun providesRewardedManager(
        appDataRepository: AppDataRepository,
        prefs: SharedPreferences
    ): RewardedManagerImpl {
        return RewardedManagerImpl(appDataRepository, prefs)
    }

    @Provides
    @Singleton
    fun providesInterManager(
        appDataRepository: AppDataRepository,
        prefs: SharedPreferences
    ): InterstitialMangerImpl {
        return InterstitialMangerImpl(appDataRepository, prefs)
    }

    @Provides
    @Singleton
    fun providesNativeManager(
        appDataRepository: AppDataRepository,
        prefs: SharedPreferences
    ): NativeManagerImpl {
        return NativeManagerImpl(appDataRepository, prefs)
    }

}




















