package com.ardrawing.sketchtrace.core.data.util.ads_original

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.ardrawing.sketchtrace.core.data.repository.AppDataInstance
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.util.PrefsConstants
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import java.util.Date

class AdmobAppOpenAdManager(
    private val application: Application
) : LifecycleObserver, ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private var loadTime: Long = 0
    private var currentActivity: Activity? = null
    private lateinit var loadCallback: AppOpenAdLoadCallback

    /**
     * Utility method to check if ad was loaded more than n hours ago.
     */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    private val isAdAvailable: Boolean
        /**
         * Utility method that checks if ad exists and can be shown.
         */
        get() = appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)

    /**
     * Request an ad
     */
    fun fetchAd(
        activity: Activity? = null,
        onAdClosed: () -> Unit
    ) {
        // Have unused ad, no need to fetch another.
        if (isAdAvailable) {
            return
        }

        val prefs = application.getSharedPreferences(
            PrefsConstants.PREFS_FILE_NAME, Context.MODE_PRIVATE
        )

        val id = prefs.getString(PrefsConstants.ADMOB_OPEN_APP_AD_ID, "") ?: ""
        Log.d(LOG_TAG, "id = $id")
        loadCallback = object : AppOpenAdLoadCallback() {
            /**
             * Called when an app open ad has loaded.
             *
             * @param ad the loaded app open ad.
             */
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                loadTime = Date().time
                Log.d(LOG_TAG, "onAdLoaded")
                Log.d(LOG_TAG, "\n\n")

                if (isSplash) {
                    showAdIfAvailable(activity) {
                        onAdClosed()
                    }
                }
            }

            /**
             * Called when an app open ad has failed to load.
             *
             * @param loadAdError the error.
             */
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d(LOG_TAG, "onAdFailedToLoad $loadAdError")
                if (isSplash) {
                    onAdClosed()
                }
            }
        }


        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            application, id, adRequest, loadCallback
        )
    }

    /**
     * Constructor
     */
    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * Shows the ad if one isn't already showing.
     */
    private fun showAdIfAvailable(
        activity: Activity? = null,
        onAdClosed: () -> Unit
    ) {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable) {
            Log.d(LOG_TAG, "Will show ad.")
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        // Set the reference to null so isAdAvailable() returns false.
                        appOpenAd = null
                        isShowingAd = false
                        if (isSplash) {
                            onAdClosed()
                        }
                        fetchAd {}
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        if (isSplash) {
                            onAdClosed()
                        }
                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }
                }
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            if (currentActivity != null) {
                appOpenAd?.show(currentActivity!!)

            } else if (activity != null && isSplash) {
                appOpenAd?.show(activity)

            } else {
                Log.d(LOG_TAG, "currentActivity = null")
                if (isSplash) {
                    onAdClosed()
                }

            }
        } else {
            Log.d(LOG_TAG, "Can not show ad.")
            if (isSplash) {
                onAdClosed()
            }
            fetchAd {}
        }
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }

    /**
     * LifecycleObserver methods
     */

    fun showSplashAd(
        activity: Activity,
        onAdClosed: () -> Unit
    ) {

        val prefs = application.getSharedPreferences(
            PrefsConstants.PREFS_FILE_NAME, Context.MODE_PRIVATE
        )

        if (
            AppDataInstance.appData?.showAdsForThisUser == false ||
            !prefs.getBoolean(PrefsConstants.CAN_SHOW_ADMOB_ADS, true)
        ) {
            onAdClosed()
            return
        }

        if (isSplash) {
            Log.d(LOG_TAG, "showSplashAd: fetchAd")
            fetchAd(activity) {
                onAdClosed()
                isSplash = false
            }
        }
    }

    companion object {
        private const val LOG_TAG = "AppOpenManager"
        private var isShowingAd = false
        private var isSplash = true
    }
}