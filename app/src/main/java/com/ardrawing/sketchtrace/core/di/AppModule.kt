package com.ardrawing.sketchtrace.core.di

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.ardrawing.sketchtrace.core.data.remote.AppDataApi
import com.ardrawing.sketchtrace.util.ads.AdmobAppOpenManager
import com.ardrawing.sketchtrace.util.ads.InterManager
import com.ardrawing.sketchtrace.util.ads.NativeManager
import com.ardrawing.sketchtrace.util.ads.RewardedManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
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
        app: Application
    ): AdmobAppOpenManager {
        return AdmobAppOpenManager(app)
    }

    @Provides
    @Singleton
    fun providesRewardedManager(): RewardedManager {
        return RewardedManager()
    }

    @Provides
    @Singleton
    fun providesInterManager(): InterManager {
        return InterManager()
    }

    @Provides
    @Singleton
    fun providesNativeManager(): NativeManager {
        return NativeManager()
    }

}




















