package com.ardrawing.sketchtrace.core.domain.repository.ads

import android.app.Activity

/**
 * @author Ahmed Guedmioui
 */
interface RewardedManger {

    fun loadRewarded(activity: Activity)

    fun showRewarded(
        activity: Activity,
        adClosedListener: OnAdClosedListener,
        isUnlockImages: Boolean = true,
        onOpenPaywall: () -> Unit,
    )

    interface OnAdClosedListener {
        fun onRewClosed()
        fun onRewFailedToShow()
        fun onRewComplete()
    }
}