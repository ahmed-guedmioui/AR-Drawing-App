package com.ardrawing.sketchtrace.core.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.ardrawing.sketchtrace.core.data.remote.AppDataApi
import com.ardrawing.sketchtrace.core.data.util.ads.AdmobAppOpenManagerImpl
import com.ardrawing.sketchtrace.core.data.util.ads.InterstitialMangerImpl
import com.ardrawing.sketchtrace.core.data.util.ads.NativeManagerImpl
import com.ardrawing.sketchtrace.core.data.util.ads.RewardedManagerImpl
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.util.PrefsConstants
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
            PrefsConstants.PREFS_FILE_NAME, Context.MODE_PRIVATE
        )
    }

    @Provides
    @Singleton
    fun providesAdmobAppOpenManager(
        prefs: SharedPreferences,
        app: Application
    ): AdmobAppOpenManagerImpl {
        return AdmobAppOpenManagerImpl(prefs, app)
    }

    @Provides
    @Singleton
    fun providesRewardedManager(
        prefs: SharedPreferences
    ): RewardedManagerImpl {
        return RewardedManagerImpl(prefs)
    }

    @Provides
    @Singleton
    fun providesInterManager(
        prefs: SharedPreferences
    ): InterstitialMangerImpl {
        return InterstitialMangerImpl(prefs)
    }

    @Provides
    @Singleton
    fun providesNativeManager(
        prefs: SharedPreferences
    ): NativeManagerImpl {
        return NativeManagerImpl(prefs)
    }

}




















