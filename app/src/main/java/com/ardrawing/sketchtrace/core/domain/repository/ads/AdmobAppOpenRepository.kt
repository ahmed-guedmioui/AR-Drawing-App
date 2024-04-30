package com.ardrawing.sketchtrace.core.domain.repository.ads

import android.app.Activity
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository

/**
 * @author Ahmed Guedmioui
 */
interface AdmobAppOpenRepository {

    fun setAppDataRepository(
        appData: AppData
    )

    fun showSplashAd(
        activity: Activity,
        onAdClosed: () -> Unit
    )
}