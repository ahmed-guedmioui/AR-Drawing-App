package com.ardrawing.sketchtrace.core.domain.repository.ads

import android.app.Activity
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData

/**
 * @author Ahmed Guedmioui
 */
interface AppOpenManager {

    fun setAppDataRepository(
        appData: AppData
    )

    fun showSplashAd(
        activity: Activity,
        onAdClosed: () -> Unit
    )
}