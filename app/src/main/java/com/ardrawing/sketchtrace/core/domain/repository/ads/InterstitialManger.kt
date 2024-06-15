package com.ardrawing.sketchtrace.core.domain.repository.ads

import android.app.Activity
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData

/**
 * @author Ahmed Guedmioui
 */
interface InterstitialManger {

    fun loadInterstitial(activity: Activity)

    fun showInterstitial(
        activity: Activity,
        adClosedListener: OnAdClosedListener
    )

    interface OnAdClosedListener {
        fun onAdClosed()
    }
}