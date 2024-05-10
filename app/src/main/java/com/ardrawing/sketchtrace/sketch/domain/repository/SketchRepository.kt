package com.ardrawing.sketchtrace.sketch.domain.repository

import java.io.File

/**
 * @author (Ahmed Guedmioui)
 */
interface SketchRepository {

    suspend fun savePhoto(): Boolean

    suspend fun saveVideo(
        file: File, isFast: Boolean
    ): Boolean


    suspend fun deleteTempVideo(uri: String): Boolean


}