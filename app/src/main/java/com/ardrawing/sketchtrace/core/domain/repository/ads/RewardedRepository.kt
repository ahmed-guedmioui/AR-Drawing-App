package com.ardrawing.sketchtrace.core.domain.repository.ads

import android.app.Activity

/**
 * @author Ahmed Guedmioui
 */
interface RewardedRepository {

    fun loadRewarded(activity: Activity)

    fun showRewarded(
        activity: Activity,
        adClosedListener: OnAdClosedListener,
        isImages: Boolean = true,
        onOpenPaywall: () -> Unit,
    )

    interface OnAdClosedListener {
        fun onRewClosed()
        fun onRewFailedToShow()
        fun onRewComplete()
    }
}