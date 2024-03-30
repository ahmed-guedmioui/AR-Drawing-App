package com.ardrawing.sketchtrace.core.domain.repository

import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * @author Ahmed Guedmioui
 */
interface AppDataRepository {
    suspend fun loadAppData(): Flow<Resource<Unit>>
    fun getAppData(): AppData?
    suspend fun setAdsVisibilityForUser()
}