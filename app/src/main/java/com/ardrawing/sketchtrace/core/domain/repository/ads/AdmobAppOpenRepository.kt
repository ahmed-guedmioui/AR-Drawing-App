package com.ardrawing.sketchtrace.core.domain.repository.ads

import android.app.Activity

/**
 * @author Ahmed Guedmioui
 */
interface AdmobAppOpenRepository {
    fun showSplashAd(
        activity: Activity,
        onAdClosed: () -> Unit
    )
}