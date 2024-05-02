package com.ardrawing.sketchtrace.core.data.repository

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.data.mapper.toAppData
import com.ardrawing.sketchtrace.core.data.remote.AppDataApi
import com.ardrawing.sketchtrace.core.data.remote.TestAppDataApi
import com.ardrawing.sketchtrace.core.data.remote.respnod.app_data.AppDataDto
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.core.domain.usecase.UpdateSubscriptionExpireDate
import com.ardrawing.sketchtrace.core.data.util.CountryChecker
import com.ardrawing.sketchtrace.core.data.util.Resource
import com.ardrawing.sketchtrace.paywall.domain.repository.PaywallRepository
import com.ardrawing.sketchtrace.util.PrefsConstants
import com.google.gson.Gson
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.util.Date
import javax.inject.Inject


/**
 * @author Ahmed Guedmioui
 */
class AppDataRepositoryImpl @Inject constructor(
    private val application: Application,
    private val appDataApi: AppDataApi,
    private val prefs: SharedPreferences,
) : AppDataRepository {

    override suspend fun loadAppData(): Flow<Resource<Unit>> {
        return flow {

            emit(Resource.Loading(true))

            val appDataDto = try {
//               appDataApi.getAppData()
                TestAppDataApi.getAppData()
            } catch (e: IOException) {
                e.printStackTrace()
                emit(
                    Resource.Error(application.getString(R.string.error_loading_data))
                )
                emit(Resource.Loading(false))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(
                    Resource.Error(application.getString(R.string.error_loading_data))
                )
                emit(Resource.Loading(false))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(
                    Resource.Error(application.getString(R.string.error_loading_data))
                )
                emit(Resource.Loading(false))
                return@flow
            }

            appDataDto?.let {
                AppDataInstance.appData = it.toAppData()

                prefs.edit()
                    .putString(PrefsConstants.ADMOB_OPEN_APP_AD_ID, getAppData().admobOpenApp)
                    .apply()

                emit(Resource.Success())
                emit(Resource.Loading(false))
                return@flow
            }

            emit(
                Resource.Error(application.getString(R.string.error_loading_data))
            )
            emit(Resource.Loading(false))

        }
    }

    override fun getAppData(): AppData {
        return AppDataInstance.appData ?: getDefaultAppData().toAppData()
    }

    override fun updateIsSubscribed(isSubscribed: Boolean) {
        getAppData().isSubscribed = isSubscribed
    }

    override fun updateShowAdsForThisUser(showAdsForThisUser: Boolean) {
        getAppData().showAdsForThisUser = showAdsForThisUser
    }

    override fun updateSubscriptionExpireDate(subscriptionExpireDate: String) {
        getAppData().subscriptionExpireDate = subscriptionExpireDate
    }

//    private fun subscription() {
//        Purchases.sharedInstance.getCustomerInfo(
//            object : ReceiveCustomerInfoCallback {
//                override fun onError(error: PurchasesError) {
//                    updateIsSubscribed(false)
//                }
//
//                override fun onReceived(customerInfo: CustomerInfo) {
//                    val date = customerInfo.getExpirationDateForEntitlement(
//                        BuildConfig.ENTITLEMENT
//                    )
//                    date?.let {
//                        if (it.after(Date())) {
//                            updateIsSubscribed(true)
//                        }
//                    }
//                    UpdateSubscriptionExpireDate(
//                        date, this@AppDataRepositoryImpl
//                    ).invoke()
//                    updateShowAdsForThisUser()
//                }
//            }
//        )
//    }

    override fun updateShowAdsForThisUser() {
        if (getAppData().isSubscribed) {
            updateShowAdsForThisUser(false)
            Log.d("REVENUE_CUT", "ShouldShowAdsForUser: isSubscribed")
            return
        }

        if (!getAppData().areAdsForOnlyWhiteListCountries) {
            updateShowAdsForThisUser(true)

            Log.d("REVENUE_CUT", "ShouldShowAdsForUser: not AdsForOnlyWhiteListCountries")
            return
        }

        val countryChecker = CountryChecker(application, CountryChecker.CheckerType.SpeedServer)
        countryChecker.setOnCheckerListener(object : CountryChecker.OnCheckerListener {
            override fun onCheckerCountry(country: String?, userFromGG: Boolean) {
                getAppData().countriesWhiteList.forEach { countryInWhiteList ->
                    if (countryInWhiteList == country) {
                        Log.d("REVENUE_CUT", "ShouldShowAdsForUser: countryInWhiteList")
                        updateShowAdsForThisUser(true)

                    }
                }
            }

            override fun onCheckerError(error: String?) {
                if (!getAppData().areAdsForOnlyWhiteListCountries) {
                    Log.d("REVENUE_CUT", "ShouldShowAdsForUser: onChecker Country Error")
                    updateShowAdsForThisUser(true)

                }
            }
        })
    }

    private fun getDefaultAppData(): AppDataDto = AppDataDto(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    )
}

object AppDataInstance {
    var appData: AppData? = null
}
















