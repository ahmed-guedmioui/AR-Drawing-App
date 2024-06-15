package com.ardrawing.sketchtrace.core.domain.repository.ads

import android.app.Activity
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData

/**
 * @author Ahmed Guedmioui
 */
interface AppOpenManager {

    fun showSplashAd(
        activity: Activity,
        onAdClosed: () -> Unit
    )
}