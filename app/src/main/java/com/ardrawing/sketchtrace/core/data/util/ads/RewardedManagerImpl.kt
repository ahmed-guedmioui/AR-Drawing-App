package com.ardrawing.sketchtrace.core.data.util.ads

import android.app.Activity
import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.data.repository.AppDataInstance
import com.ardrawing.sketchtrace.core.domain.repository.ads.RewardedManger
import com.ardrawing.sketchtrace.util.AdsConstants
import com.ardrawing.sketchtrace.util.PrefsConstants
import com.facebook.ads.Ad
import com.facebook.ads.RewardedVideoAd
import com.facebook.ads.RewardedVideoAdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import javax.inject.Inject

class RewardedManagerImpl @Inject constructor(
    private val prefs: SharedPreferences
) : RewardedManger {

    private lateinit var onAdClosedListener: RewardedManger.OnAdClosedListener

    private var isFacebookRewardedLoaded = false
    private var isAdmobRewardedLoaded = false

    private lateinit var admobRewardedAd: com.google.android.gms.ads.rewarded.RewardedAd
    private lateinit var facebookRewardedAd: RewardedVideoAd


    override fun loadRewarded(activity: Activity) {

        if (AppDataInstance.appData?.showAdsForThisUser == false) {
            return
        }

        when (AppDataInstance.appData?.rewarded) {
            AdsConstants.ADMOB -> loadAdmobRewarded(activity)
            AdsConstants.FACEBOOK -> loadFacebookRewarded(activity)
        }
    }

    override fun showRewarded(
        activity: Activity,
        adClosedListener: RewardedManger.OnAdClosedListener,
        isUnlockImages: Boolean,
        onOpenPaywall: () -> Unit,
    ) {
        onAdClosedListener = adClosedListener

        if (AppDataInstance.appData?.showAdsForThisUser == false) {
            onAdClosedListener.onRewClosed()
            onAdClosedListener.onRewComplete()
            return
        }

        dialog(activity, isUnlockImages) {
            onOpenPaywall()
        }
    }

    private fun dialog(
        activity: Activity,
        isUnlockImages: Boolean = true,
        onOpenPaywall: () -> Unit,
    ) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(
            if (isUnlockImages) R.layout.dialog_rewarded_images else R.layout.dialog_rewarded
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
            when (AppDataInstance.appData?.rewarded) {
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

        if (!prefs.getBoolean(PrefsConstants.CAN_SHOW_ADMOB_ADS, true)) {
            return
        }

        isAdmobRewardedLoaded = false

        val adRequest = AdRequest.Builder().build()

        com.google.android.gms.ads.rewarded.RewardedAd.load(
            activity,
            AppDataInstance.appData?.admobRewarded ?: "",
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

        if (!prefs.getBoolean(PrefsConstants.CAN_SHOW_ADMOB_ADS, true)) {
            return
        }

        if (isAdmobRewardedLoaded) {
            admobRewardedAd.show(activity) {
//                isAdmobRewardedLoaded = false
//                loadRewarded(activity)
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
            AppDataInstance.appData?.facebookRewarded ?: ""
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
//                    isFacebookRewardedLoaded = false
//                    loadRewarded(activity)
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
}
