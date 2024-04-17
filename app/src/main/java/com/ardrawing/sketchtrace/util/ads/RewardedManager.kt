package com.ardrawing.sketchtrace.util.ads

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.facebook.ads.Ad
import com.facebook.ads.RewardedVideoAd
import com.facebook.ads.RewardedVideoAdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.util.AdsConstants

object RewardedManager {

    var appData: AppData? = null

    private lateinit var onAdClosedListener: OnAdClosedListener

    private var isFacebookRewardedLoaded = false
    private var isAdmobRewardedLoaded = false

    private lateinit var admobRewardedAd: com.google.android.gms.ads.rewarded.RewardedAd
    private lateinit var facebookRewardedAd: RewardedVideoAd


    fun loadRewarded(activity: Activity) {

        if (appData?.showAdsForThisUser == false) {
            return
        }

        when (appData?.rewarded) {
            AdsConstants.ADMOB -> loadAdmobRewarded(activity)
            AdsConstants.FACEBOOK -> loadFacebookRewarded(activity)
        }
    }

    fun showRewarded(
        activity: Activity,
        adClosedListener: OnAdClosedListener,
        isImages: Boolean = true,
        onOpenPaywall: () -> Unit,
    ) {
        onAdClosedListener = adClosedListener

        if (appData?.showAdsForThisUser == false) {
            onAdClosedListener.onRewClosed()
            onAdClosedListener.onRewComplete()
            return
        }

        dialog(activity, isImages) {
            onOpenPaywall()
        }
    }

    private fun dialog(
        activity: Activity,
        isImages: Boolean = true,
        onOpenPaywall: () -> Unit,
    ) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(
            if (isImages) R.layout.dialog_rewarded_images else R.layout.dialog_rewarded
        )
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.copyFrom(dialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes = layoutParams

        dialog.findViewById<ImageView>(R.id.close).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<CardView>(R.id.watch).setOnClickListener {
            when (appData?.rewarded) {
                AdsConstants.ADMOB -> showAdmobRewarded(activity)
                AdsConstants.FACEBOOK -> showFacebookRewarded(activity)
                else -> onAdClosedListener.onRewFailedToShow()
            }
            dialog.dismiss()
        }

        dialog.findViewById<CardView>(R.id.vip).setOnClickListener {
            onOpenPaywall()
            dialog.dismiss()
        }

        dialog.show()
    }

    // Admob ---------------------------------------------------------------------------------------------------------------------

    private fun loadAdmobRewarded(activity: Activity) {

        val prefs = activity.getSharedPreferences(
            "ar_drawing_med_prefs_file", Context.MODE_PRIVATE
        )

        if (!prefs.getBoolean(AdsConstants.CAN_SHOW_ADMOB_ADS, true)) {
            return
        }

        isAdmobRewardedLoaded = false

        val adRequest = AdRequest.Builder().build()

        com.google.android.gms.ads.rewarded.RewardedAd.load(
            activity,
            appData?.admobRewarded ?: "",
            adRequest,
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(rewardedAd: com.google.android.gms.ads.rewarded.RewardedAd) {
                    admobRewardedAd = rewardedAd
                    isAdmobRewardedLoaded = true

                    rewardedAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {

                            override fun onAdDismissedFullScreenContent() {
                                isAdmobRewardedLoaded = false
                                loadRewarded(activity)
                                onAdClosedListener.onRewClosed()
                            }

                            override fun onAdShowedFullScreenContent() {
                            }

                        }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isAdmobRewardedLoaded = false
                }
            })
    }

    private fun showAdmobRewarded(activity: Activity) {

        val prefs = activity.getSharedPreferences(
            "ar_drawing_med_prefs_file", Context.MODE_PRIVATE
        )

        if (!prefs.getBoolean(AdsConstants.CAN_SHOW_ADMOB_ADS, true)) {
            return
        }

        if (isAdmobRewardedLoaded) {
            admobRewardedAd.show(activity) {
                isAdmobRewardedLoaded = false
                loadRewarded(activity)
                onAdClosedListener.onRewComplete()
            }
        } else {
            loadRewarded(activity)
            onAdClosedListener.onRewFailedToShow()
        }
    }

    // Facebook ---------------------------------------------------------------------------------------------------------------------

    private fun loadFacebookRewarded(activity: Activity) {

        isFacebookRewardedLoaded = false
        facebookRewardedAd = RewardedVideoAd(
            activity,
            appData?.facebookRewarded
        )

        val rewardedVideoAdListener: RewardedVideoAdListener =
            object : RewardedVideoAdListener {
                override fun onError(ad: Ad?, adError: com.facebook.ads.AdError?) {
                    isFacebookRewardedLoaded = false
                }

                override fun onAdLoaded(ad: Ad) {
                    isFacebookRewardedLoaded = true
                }

                override fun onRewardedVideoCompleted() {
                    isFacebookRewardedLoaded = false
                    loadRewarded(activity)
                    onAdClosedListener.onRewComplete()
                }

                override fun onRewardedVideoClosed() {
                    isFacebookRewardedLoaded = false
                    loadRewarded(activity)
                    onAdClosedListener.onRewClosed()
                }

                override fun onAdClicked(ad: Ad) {}
                override fun onLoggingImpression(ad: Ad) {}

            }
        facebookRewardedAd.loadAd(
            facebookRewardedAd.buildLoadAdConfig()
                .withAdListener(rewardedVideoAdListener).build()
        )
    }

    private fun showFacebookRewarded(activity: Activity) {
        if (isFacebookRewardedLoaded) {
            facebookRewardedAd.show()
        } else {
            loadRewarded(activity)
            onAdClosedListener.onRewFailedToShow()
        }
    }

    interface OnAdClosedListener {
        fun onRewClosed()
        fun onRewFailedToShow()
        fun onRewComplete()
    }
}
