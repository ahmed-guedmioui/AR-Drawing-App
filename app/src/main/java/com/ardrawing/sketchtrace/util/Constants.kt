package com.ardrawing.sketchtrace.util

import android.graphics.Bitmap

/**
 * @author Ahmed Guedmioui
 */
object Constants {
    var bitmap: Bitmap? = null
    var convertedBitmap: Bitmap? = null

    var languageChanged1 = false
    var languageChanged2 = false
}


// Do Not Edit These!
object PrefsConstants {
    const val ADMOB_OPEN_APP_AD_ID = "admobOpenApp"
    const val CAN_SHOW_ADMOB_ADS = "can_show_ads"
    const val IS_APP_RATED = "is_rated"
    const val LANGUAGE = "language"

    const val IS_LANGUAGE_CHOSEN = "language_chosen"
    const val IS_ONBOARDING_SHOWN = "tipsShown"

    const val IS_GET_STARTED_SHOWN = "getStartedShown"
}

object AdsConstants {
    const val FACEBOOK = "facebook"
    const val ADMOB = "admob"
}






