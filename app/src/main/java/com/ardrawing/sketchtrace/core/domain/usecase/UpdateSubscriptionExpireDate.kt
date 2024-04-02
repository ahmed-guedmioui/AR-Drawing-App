package com.ardrawing.sketchtrace.core.domain.usecase

import android.annotation.SuppressLint
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import java.text.SimpleDateFormat
import java.util.Date

/**
 * @author Ahmed Guedmioui
 */

class UpdateSubscriptionExpireDate(
    private val date: Date?,
    private val appDataRepository: AppDataRepository
) {

    @SuppressLint("SimpleDateFormat")
    operator fun invoke() {
        if (date != null) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val formattedDate: String = dateFormat.format(date)

            if (date.after(Date())) {
                appDataRepository.updateSubscriptionExpireDate(formattedDate)
            }
        }

    }
}