package com.ardrawing.sketchtrace.core.data.util.ads

import android.app.Activity
import android.app.ProgressDialog
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.data.repository.AppDataInstance
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.core.domain.repository.ads.InterstitialManger
import com.ardrawing.sketchtrace.util.AdsConstants
import com.ardrawing.sketchtrace.util.PrefsConstants
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import javax.inject.Inject

class InterstitialMangerImpl @Inject constructor(
    private val prefs: SharedPreferences
) : InterstitialManger {

    private lateinit var admobInterstitialAd: InterstitialAd
    private lateinit var facebookInterstitialAd: com.facebook.ads.InterstitialAd

    private var isFacebookInterLoaded = false
    private var isAdmobInterLoaded = false

    private lateinit var onAdClosedListener: InterstitialManger.OnAdClosedListener
    private var counter = 1

    override fun loadInterstitial(activity: Activity) {

        if (AppDataInstance.appData?.showAdsForThisUser == false) {
            return
        }

        when (AppDataInstance.appData?.interstitial) {
            AdsConstants.ADMOB -> loadAdmobInter(activity)
            AdsConstants.FACEBOOK -> loadFacebookInter(activity)
        }
    }

    override fun showInterstitial(
        activity: Activity,
        adClosedListener: InterstitialManger.OnAdClosedListener
    ) {

        onAdClosedListener = adClosedListener

        if (AppDataInstance.appData?.showAdsForThisUser == false) {
            onAdClosedListener.onAdClosed()
            return
        }

        if (AppDataInstance.appData?.clicksToShowInter == counter) {
            counter = 1

            val progressDialog = ProgressDialog(activity)
            progressDialog.setCancelable(false)
            progressDialog.setMessage(activity.getString(R.string.loading_ads))
            progressDialog.show()

            Handler(Looper.getMainLooper()).postDelayed({
                when (AppDataInstance.appData?.interstitial) {
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
            com.facebook.ads.InterstitialAd(
                activity,
                AppDataInstance.appData?.facebookInterstitial ?: ""
            )
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

        if (!prefs.getBoolean(PrefsConstants.CAN_SHOW_ADMOB_ADS, true)) {
            return
        }

        isAdmobInterLoaded = false

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity,
            AppDataInstance.appData?.admobInterstitial ?: "",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("interstitialAd_Ads_Tag", "interstitialAd onAdLoaded")
                    isAdmobInterLoaded = true
                    admobInterstitialAd = interstitialAd
                    interstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Log.d(
                                    "interstitialAd_Ads_Tag",
                                    "interstitialAd onAdDismissedFullScreenContent"
                                )

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

        if (!prefs.getBoolean(PrefsConstants.CAN_SHOW_ADMOB_ADS, true)) {
            onAdClosedListener.onAdClosed()
            return
        }

        if (isAdmobInterLoaded) {
            admobInterstitialAd.show(activity)
        } else {
            loadInterstitial(activity)
            onAdClosedListener.onAdClosed()
        }
    }
}



