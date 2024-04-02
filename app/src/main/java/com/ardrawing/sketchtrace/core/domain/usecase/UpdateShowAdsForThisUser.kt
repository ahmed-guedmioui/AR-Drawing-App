package com.ardrawing.sketchtrace.core.domain.usecase

import android.app.Application
import android.util.Log
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.util.CountryChecker

/**
 * @author Ahmed Guedmioui
 */
class UpdateShowAdsForThisUser(
    private val application: Application,
    private val appDataRepository: AppDataRepository
) {

    operator fun invoke() {

        if (appDataRepository.getAppData().isSubscribed) {
            appDataRepository.updateShowAdsForThisUser(false)
            Log.d("REVENUE_CUT", "ShouldShowAdsForUser: isSubscribed")
            return
        }

        if (!appDataRepository.getAppData().areAdsForOnlyWhiteListCountries) {
            appDataRepository.updateShowAdsForThisUser(true)

            Log.d("REVENUE_CUT", "ShouldShowAdsForUser: not AdsForOnlyWhiteListCountries")
            return
        }

        val countryChecker = CountryChecker(application, CountryChecker.CheckerType.SpeedServer)
        countryChecker.setOnCheckerListener(object : CountryChecker.OnCheckerListener {
            override fun onCheckerCountry(country: String?, userFromGG: Boolean) {
                appDataRepository.getAppData().countriesWhiteList.forEach { countryInWhiteList ->
                    if (countryInWhiteList == country) {
                        Log.d("REVENUE_CUT", "ShouldShowAdsForUser: countryInWhiteList")
                        appDataRepository.updateShowAdsForThisUser(true)

                    }
                }
            }

            override fun onCheckerError(error: String?) {
                if (!appDataRepository.getAppData().areAdsForOnlyWhiteListCountries) {
                    Log.d("REVENUE_CUT", "ShouldShowAdsForUser: onChecker Country Error")
                    appDataRepository.updateShowAdsForThisUser(true)

                }
            }
        })
    }
}





