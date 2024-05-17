package com.ardrawing.sketchtrace.core.domain.model.app_data

import com.ardrawing.sketchtrace.settings.domain.model.RecommendedApp

data class AppData(
    val interstitial: String,
    val native: String,
    val openAppAd: String,
    val rewarded: String,

    val admobPublisherId: String,
    val admobInterstitial: String,
    val admobNative: String,
    val admobOpenApp: String,
    val admobRewarded: String,

    var facebookInterstitial: String,
    var facebookNative: String,
    val facebookRewarded: String,

    val clicksToShowInter: Int,
    val nativeRate: Int,

    val countriesWhiteList: List<String>,

    val onesignalId: String,

    val appLatestVersion: Int,

    val areAdsForOnlyWhiteListCountries: Boolean,
    val recommendedApps: List<RecommendedApp>,

    val showRecommendedApps: Boolean,

    val isAppSuspended: Boolean,
    val suspendedURL: String,
    val suspendedMessage: String,
    val suspendedTitle: String,

    val privacyLink: String,


    // these ones are not gotten from json configuration,
    // we assign a value to it based on if we can show ads for this user
    var isSubscribed: Boolean = false,
    var showAdsForThisUser: Boolean = false,
    var subscriptionExpireDate: String = ""
)