package com.ardrawing.sketchtrace

import android.app.Application
import android.content.Context
import com.ardrawing.sketchtrace.language.data.util.LanguageChanger
import com.facebook.ads.AudienceNetworkAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.onesignal.OneSignal
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * @author Ahmed Guedmioui
 */
@HiltAndroidApp
class App : Application() {

    companion object {

        const val DEVELOPER_NAME = "AR Draw Studio"

        const val tiktok = "ardrawstudio.sketchtrace"
        const val facebook = "ardrawstudio.sketchtrace"
        const val instagram = "ardrawstudio.sketchtrace"
        const val x = "ArDrawStudio"
    }

    override fun onCreate() {
        super.onCreate()

        trimCache()
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        AudienceNetworkAds.initialize(this)

        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(
                this, BuildConfig.REVENUECUT_KEY
            ).build()
        )

        OneSignal.Debug.logLevel = com.onesignal.debug.LogLevel.VERBOSE
        OneSignal.initWithContext(
            this.baseContext, "016473e9-27ee-4bc9-8789-d20057f3dea5"
        )
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(
            LanguageChanger.changeAppLanguage(base)
        )
    }

    private fun trimCache() {
        try {
            val dir = cacheDir
            if (dir != null && dir.isDirectory) {
                deleteDir(dir)
            }
        } catch (_: Exception) {
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
            }
        }
        return dir!!.delete()
    }
}