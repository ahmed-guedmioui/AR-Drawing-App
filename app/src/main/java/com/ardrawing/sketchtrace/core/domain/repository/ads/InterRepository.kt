package com.ardrawing.sketchtrace.core.domain.repository.ads

import android.app.Activity

/**
 * @author Ahmed Guedmioui
 */
interface InterRepository {

    fun loadInterstitial(activity: Activity)

    fun showInterstitial(
        activity: Activity,
        adClosedListener: OnAdClosedListener
    )

    interface OnAdClosedListener {
        fun onAdClosed()
    }
}