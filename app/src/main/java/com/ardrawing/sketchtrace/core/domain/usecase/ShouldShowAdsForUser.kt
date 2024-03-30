package com.ardrawing.sketchtrace.core.domain.usecase

import android.app.Application
import android.util.Log
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.util.CountryChecker

/**
 * @author Ahmed Guedmioui
 */
class ShouldShowAdsForUser(
    private val application: Application,
    private val appData: AppData?
) {

    operator fun invoke() {

        if (appData?.isSubscribed == true) {
            appData.showAdsForThisUser = false
            Log.d("REVENUE_CUT", "ShouldShowAdsForUser: isSubscribed")
            return
        }

        if (appData?.areAdsForOnlyWhiteListCountries == false) {
           appData.showAdsForThisUser = true
            Log.d("REVENUE_CUT", "ShouldShowAdsForUser: not AdsForOnlyWhiteListCountries")
            return
        }

        val countryChecker = CountryChecker(application, CountryChecker.CheckerType.SpeedServer)
        countryChecker.setOnCheckerListener(object : CountryChecker.OnCheckerListener {
            override fun onCheckerCountry(country: String?, userFromGG: Boolean) {
                appData?.countriesWhiteList?.forEach { countryInWhiteList ->
                    if (countryInWhiteList == country) {
                        Log.d("REVENUE_CUT", "ShouldShowAdsForUser: countryInWhiteList")
                        appData.showAdsForThisUser = true
                    }
                }
            }

            override fun onCheckerError(error: String?) {
                if (appData?.areAdsForOnlyWhiteListCountries == false) {
                    Log.d("REVENUE_CUT", "ShouldShowAdsForUser: onChecker Country Error")
                    appData.showAdsForThisUser = true
                }
            }
        })
    }
}





