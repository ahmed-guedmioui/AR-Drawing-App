package com.ardrawing.sketchtrace.core.domain.usecase

import android.annotation.SuppressLint
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import java.text.SimpleDateFormat
import java.util.Date

/**
 * @author Ahmed Guedmioui
 */

class UpdateSubscriptionInfo(
    private val date: Date?,
    private val appData: AppData?
) {

    @SuppressLint("SimpleDateFormat")
    operator fun invoke() {
        if (date != null) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val formattedDate: String = dateFormat.format(date)

            if (date.after(Date())) {
                appData?.subscriptionExpireDate = formattedDate
            }
        }

    }
}