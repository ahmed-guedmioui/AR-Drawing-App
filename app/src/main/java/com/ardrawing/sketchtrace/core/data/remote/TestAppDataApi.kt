package com.ardrawing.sketchtrace.core.data.remote

import com.ardrawing.sketchtrace.core.data.remote.respnod.app_data.AppDataDto

/**
 * @author Ahmed Guedmioui
 */
object TestAppDataApi {

    suspend fun getAppData(): AppDataDto? = AppDataDto(
        "admob",
        "admob",
        "admob",
        "admob",
        null,
        "ca-app-pub-3940256099942544/1033173712",
        "ca-app-pub-3940256099942544/2247696110",
        "ca-app-pub-3940256099942544/9257395921",
        "ca-app-pub-3940256099942544/5224354917",
        null,
        null,
        null,
        2,
        3,
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