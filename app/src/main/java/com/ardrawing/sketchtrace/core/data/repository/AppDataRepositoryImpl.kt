package com.ardrawing.sketchtrace.core.data.repository

import android.app.Application
import android.content.SharedPreferences
import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.data.mapper.toAppData
import com.ardrawing.sketchtrace.core.data.remote.AppDataApi
import com.ardrawing.sketchtrace.core.data.remote.respnod.app_data.AppDataDto
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.core.domain.usecase.ShouldShowAdsForUser
import com.ardrawing.sketchtrace.core.domain.usecase.UpdateSubscriptionInfo
import com.ardrawing.sketchtrace.util.Resource
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import kotlinx.coroutines.delay
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
    private val prefs: SharedPreferences
) : AppDataRepository {

    override suspend fun loadAppData(): Flow<Resource<Unit>> {
        return flow {

            emit(Resource.Loading(true))

            val appDataDto = try {
                appDataApi.getAppData()
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
                    .putString("admobOpenApp", getAppData()?.admobOpenApp)
                    .apply()

                subscription()

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

    override fun getAppData(): AppData? {
        return AppDataInstance.appData
    }

    override suspend fun setAdsVisibilityForUser() {
        ShouldShowAdsForUser(application, getAppData()).invoke()
    }

    private fun subscription() {
        Purchases.sharedInstance.getCustomerInfo(
            object : ReceiveCustomerInfoCallback {
                override fun onError(error: PurchasesError) {
                    UpdateSubscriptionInfo(null, getAppData()).invoke()
                }

                override fun onReceived(customerInfo: CustomerInfo) {
                    val date = customerInfo.getExpirationDateForEntitlement(BuildConfig.ENTITLEMENT)
                    date?.let {
                        if (it.after(Date())) {
                            getAppData()?.isSubscribed = true
                        }
                    }
                    UpdateSubscriptionInfo(date, getAppData()).invoke()
                    ShouldShowAdsForUser(application, getAppData()).invoke()
                }
            }
        )
    }


    // Only for testing
    suspend fun loadTestAppData(): Flow<Resource<Unit>> {
        return flow {

            emit(Resource.Loading(true))
            delay(3000)

            prefs.edit()
                .putString("admobOpenApp", getTestAppData().admobOpenApp)
                .apply()

            subscription()

            emit(Resource.Success())
            emit(Resource.Loading(false))
        }
    }

    private fun getTestAppData(): AppData = AppDataDto(
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
    ).toAppData()


}

object AppDataInstance {
    var appData: AppData? = null
}

















