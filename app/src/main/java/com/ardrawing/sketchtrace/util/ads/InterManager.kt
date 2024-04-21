package com.ardrawing.sketchtrace.util.ads

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.util.AdsConstants
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterManager {

    var appData: AppData? = null

    private lateinit var admobInterstitialAd: InterstitialAd
    private lateinit var facebookInterstitialAd: com.facebook.ads.InterstitialAd

    private var isFacebookInterLoaded = false
    private var isAdmobInterLoaded = false

    private lateinit var onAdClosedListener: OnAdClosedListener
    private var counter = 1


    fun loadInterstitial(
        activity: Activity,
    ) {

        if (appData?.showAdsForThisUser == false ) {
            return
        }

        when (appData?.interstitial) {
            AdsConstants.ADMOB -> loadAdmobInter(activity)
            AdsConstants.FACEBOOK -> loadFacebookInter(activity)
        }
    }

    fun showInterstitial(
        activity: Activity,
        adClosedListener: OnAdClosedListener
    ) {
        onAdClosedListener = adClosedListener

        if (appData?.showAdsForThisUser == false) {
            onAdClosedListener.onAdClosed()
            return
        }

        if (appData?.clicksToShowInter == counter) {
            counter = 1

            val progressDialog = ProgressDialog(activity)
            progressDialog.setCancelable(false)
            progressDialog.setMessage(activity.getString(R.string.loading_ads))
            progressDialog.show()

            Handler(Looper.getMainLooper()).postDelayed({
                when (appData?.interstitial) {
                    AdsConstants.ADMOB -> showAdmobInter(activity)
                    AdsConstants.FACEBOOK -> showFacebookInter(activity)
                    else -> onAdClosedListener.onAdClosed()
                }
                try {
                    progressDialog.dismiss()
                } catch (_: Exception) {
                }

            }, 2000)

        } else {
            counter++
            onAdClosedListener.onAdClosed()
        }
    }


    // Facebook ---------------------------------------------------------------------------------------------------------------------

    private fun loadFacebookInter(activity: Activity) {
        isFacebookInterLoaded = false
        facebookInterstitialAd =
            com.facebook.ads.InterstitialAd(activity, appData?.facebookInterstitial)
        val interstitialAdListener: InterstitialAdListener = object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad) {}
            override fun onInterstitialDismissed(ad: Ad) {
                isFacebookInterLoaded = false
                loadInterstitial(activity)
                onAdClosedListener.onAdClosed()
            }

            override fun onError(ad: Ad, adError: AdError) {
                isFacebookInterLoaded = false
            }

            override fun onAdLoaded(ad: Ad) {
                isFacebookInterLoaded = true
            }

            override fun onAdClicked(ad: Ad) {}
            override fun onLoggingImpression(ad: Ad) {}
        }
        facebookInterstitialAd.loadAd(
            facebookInterstitialAd.buildLoadAdConfig()
                .withAdListener(interstitialAdListener).build()
        )
    }

    private fun showFacebookInter(activity: Activity) {
        if (isFacebookInterLoaded) {
            facebookInterstitialAd.show()
        } else {
            loadInterstitial(activity)
            onAdClosedListener.onAdClosed()
        }
    }

    // Admob ---------------------------------------------------------------------------------------------------------------------

    private fun loadAdmobInter(activity: Activity) {

        val prefs = activity.getSharedPreferences(
            "ar_drawing_med_prefs_file", Context.MODE_PRIVATE
        )

        if (!prefs.getBoolean(AdsConstants.CAN_SHOW_ADMOB_ADS, true)) {
            return
        }

        isAdmobInterLoaded = false

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity,
            appData?.admobInterstitial ?: "",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("interstitialAd_Ads_Tag", "interstitialAd onAdLoaded")
                    isAdmobInterLoaded = true
                    admobInterstitialAd = interstitialAd
                    interstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Log.d("interstitialAd_Ads_Tag", "interstitialAd onAdDismissedFullScreenContent")

                                isAdmobInterLoaded = false
                                loadInterstitial(activity)
                                onAdClosedListener.onAdClosed()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                                Log.d(
                                    "interstitialAd_Ads_Tag",
                                    "interstitialAd onAdFailedToShowFullScreenContent: $adError"
                                )

                                isAdmobInterLoaded = false
                                loadInterstitial(activity)
                                onAdClosedListener.onAdClosed()
                            }

                            override fun onAdShowedFullScreenContent() {}
                        }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d("interstitialAd_Ads_Tag", "interstitialAd onAdFailedToLoad: $loadAdError")

                    isAdmobInterLoaded = false
                }
            })
    }

    private fun showAdmobInter(activity: Activity) {

        val prefs = activity.getSharedPreferences(
            "ar_drawing_med_prefs_file", Context.MODE_PRIVATE
        )

        if (!prefs.getBoolean(AdsConstants.CAN_SHOW_ADMOB_ADS, true)) {
            return
        }

        if (isAdmobInterLoaded) {
            admobInterstitialAd.show(activity)
        } else {
            loadInterstitial(activity)
            onAdClosedListener.onAdClosed()
        }
    }


    interface OnAdClosedListener {
        fun onAdClosed()
    }
}



