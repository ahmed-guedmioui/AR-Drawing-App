package com.ardrawing.sketchtrace.creation.domian.repository

import android.graphics.Bitmap
import com.ardrawing.sketchtrace.creation.domian.model.Creation
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * @author (Ahmed Guedmioui)
 */
interface CreationRepository {

    suspend fun insertPhotoCreation(bitmap: Bitmap): Boolean

    suspend fun insertVideoCreation(
        file: File, isFast: Boolean
    ): Boolean

    suspend fun deleteCreation(uri: String): Boolean

    suspend fun deleteTempCreation(uri: String): Boolean

    suspend fun getCreationList(): Flow<List<Creation>>

}