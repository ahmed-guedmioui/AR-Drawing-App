package com.ardrawing.sketchtrace.creation.domian.repository

import com.ardrawing.sketchtrace.creation.domian.model.Creation
import kotlinx.coroutines.flow.Flow

/**
 * @author (Ahmed Guedmioui)
 */
interface CreationRepository {

    suspend fun deleteCreation(uri: String): Boolean

    suspend fun getCreationList(): Flow<List<Creation>>

}