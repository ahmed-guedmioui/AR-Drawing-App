package com.ardrawing.sketchtrace.core.data.util.ads

import android.app.Activity
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.data.repository.AppDataInstance
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.core.domain.repository.ads.NativeManager
import com.ardrawing.sketchtrace.util.AdsConstants
import com.ardrawing.sketchtrace.util.PrefsConstants
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdOptionsView
import com.facebook.ads.MediaView
import com.facebook.ads.NativeAdLayout
import com.facebook.ads.NativeAdListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController.VideoLifecycleCallbacks
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import javax.inject.Inject

class NativeManagerImpl @Inject constructor(
    private val prefs: SharedPreferences
) : NativeManager {

    private var admobNativeAd: NativeAd? = null
    private lateinit var activity: Activity

    override fun loadNative(
        nativeFrame: FrameLayout,
        nativeTemp: TextView,
        isButtonTop: Boolean,
        activity: Activity
    ) {

        this.activity = activity

        if (AppDataInstance.appData?.showAdsForThisUser == false) {
            nativeTemp.visibility = View.GONE
            return
        }

        when (AppDataInstance.appData?.native) {
            AdsConstants.ADMOB -> loadAdmobNative(
                nativeFrame, nativeTemp, isButtonTop
            )

            AdsConstants.FACEBOOK -> loadFacebookNative(
                nativeFrame, nativeTemp, isButtonTop
            )

            else -> {
                nativeFrame.visibility = View.GONE
                nativeTemp.visibility = View.GONE
            }
        }
    }


    // Admob ---------------------------------------------------------------------------------------------------------------------

    private fun loadAdmobNative(
        nativeFrame: FrameLayout,
        nativeTemp: TextView,
        isButtonTop: Boolean
    ) {

        if (!prefs.getBoolean(PrefsConstants.CAN_SHOW_ADMOB_ADS, true)) {
            return
        }

        val builder = AdLoader.Builder(
            activity,
            AppDataInstance.appData?.admobNative ?: ""
        )

        builder.forNativeAd { nativeAd: NativeAd ->
            val isDestroyed = activity.isDestroyed
            if (isDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                nativeAd.destroy()
                return@forNativeAd
            }
            if (admobNativeAd != null) {
                admobNativeAd?.destroy()
            }
            admobNativeAd = nativeAd
            val adView = if (!isButtonTop) {
                activity.layoutInflater.inflate(
                    R.layout.native_admob, null
                ) as NativeAdView
            } else {
                activity.layoutInflater.inflate(
                    R.layout.native_admob_button_top, null
                ) as NativeAdView
            }

            populateAdmobNative(nativeAd, adView)
            nativeFrame.removeAllViews()
            nativeFrame.addView(adView)

            nativeTemp.visibility = View.GONE
            nativeFrame.visibility = View.VISIBLE
        }
        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                nativeFrame.visibility = View.GONE
                nativeTemp.visibility = View.GONE
            }
        }).build()

        val adRequest = AdRequest.Builder().build()

        adLoader.loadAd(adRequest)
    }

    private fun populateAdmobNative(nativeAd: NativeAd, adView: NativeAdView) {
        adView.mediaView = adView.findViewById(R.id.ad_media)
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
        (adView.headlineView as TextView?)?.text = nativeAd.headline
        adView.mediaView?.mediaContent = nativeAd.mediaContent
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView?)?.text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button?)?.text = nativeAd.callToAction
        }
        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView?)?.setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView?)?.text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)
        val vc = nativeAd.mediaContent?.videoController
        if (vc!!.hasVideoContent()) {
            vc.videoLifecycleCallbacks = object : VideoLifecycleCallbacks() {}
        }
    }


    // Facebook ---------------------------------------------------------------------------------------------------------------------

    private fun loadFacebookNative(
        nativeFrame: FrameLayout,
        nativeTemp: TextView,
        isButtonTop: Boolean
    ) {
        val nativeAd = com.facebook.ads.NativeAd(
            activity,
            AppDataInstance.appData?.facebookNative ?: ""
        )
        val nativeAdListener: NativeAdListener = object : NativeAdListener {
            override fun onMediaDownloaded(ad: Ad) {}
            override fun onError(ad: Ad, adError: AdError) {
                nativeFrame.visibility = View.GONE
                nativeTemp.visibility = View.GONE
            }

            override fun onAdLoaded(ad: Ad) {
                if (nativeAd !== ad) {
                    return
                }
                nativeTemp.visibility = View.GONE
                nativeFrame.visibility = View.VISIBLE
                populateFacebookNative(
                    nativeFrame, nativeAd, isButtonTop
                )
            }

            override fun onAdClicked(ad: Ad) {}
            override fun onLoggingImpression(ad: Ad) {}
        }
        nativeAd.loadAd(nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build())
    }

    private fun populateFacebookNative(
        nativeFrame: FrameLayout,
        nativeAd: com.facebook.ads.NativeAd,
        isButtonTop: Boolean
    ) {


        nativeAd.unregisterView()
        val nativeAdLayout = NativeAdLayout(activity)

        // Add the Ad view into the ad container.
        val inflater = LayoutInflater.from(activity)
        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        val adView = if (!isButtonTop) {
            inflater.inflate(R.layout.native_meta, null) as LinearLayout
        } else {
            inflater.inflate(R.layout.native_meta_button_top, null) as LinearLayout
        }
        nativeFrame.addView(adView)

        try {

            // Add the AdOptionsView
            val adChoicesContainer = activity.findViewById<LinearLayout>(R.id.ad_choices_container)
            val adOptionsView = AdOptionsView(activity, nativeAd, nativeAdLayout)
            adChoicesContainer.removeAllViews()
            adChoicesContainer.addView(adOptionsView, 0)
        } catch (_: Exception) {
        }

        // Create native UI using the ad metadata.
        val nativeAdIcon = adView.findViewById<MediaView>(R.id.native_ad_icon)
        val nativeAdTitle = adView.findViewById<TextView>(R.id.native_ad_title)
        val nativeAdMedia = adView.findViewById<MediaView>(R.id.native_ad_media)
        val nativeAdSocialContext = adView.findViewById<TextView>(R.id.native_ad_social_context)
        val nativeAdBody = adView.findViewById<TextView>(R.id.native_ad_body)
        val sponsoredLabel = adView.findViewById<TextView>(R.id.native_ad_sponsored_label)
        val nativeAdCallToAction = adView.findViewById<Button>(R.id.native_ad_call_to_action)

        // Set the Text.
        nativeAdTitle.text = nativeAd.advertiserName
        nativeAdBody.text = nativeAd.adBodyText
        nativeAdSocialContext.text = nativeAd.adSocialContext
        nativeAdCallToAction.visibility =
            if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdCallToAction.text = nativeAd.adCallToAction
        sponsoredLabel.text = nativeAd.sponsoredTranslation

        // Create a list of clickable views
        val clickableViews: MutableList<View> = ArrayList()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
            adView, nativeAdMedia, nativeAdIcon, clickableViews
        )

    }
}
















