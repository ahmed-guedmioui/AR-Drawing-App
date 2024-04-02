package com.ardrawing.sketchtrace.core.presentation.splash.util

import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData

/**
 * @author Ahmed Guedmioui
 */
class ShouldShowUpdateDialog(
    private val appData: AppData?
) {

    operator fun invoke(): Int {

        if (appData?.isAppSuspended == true) {
            return 2
        }

        if (BuildConfig.VERSION_CODE < (appData?.appLatestVersion ?: 0)) {
            return 1
        }

        return 0
    }
}