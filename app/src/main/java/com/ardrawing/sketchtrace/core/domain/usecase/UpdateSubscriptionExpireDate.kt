package com.ardrawing.sketchtrace.core.domain.usecase

import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @author Ahmed Guedmioui
 */

class UpdateSubscriptionExpireDate(
    private val date: Date?,
    private val appDataRepository: AppDataRepository
) {

    operator fun invoke() {
        if (date != null) {
            val dateFormat = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.ENGLISH
            )
            val formattedDate: String = dateFormat.format(date)

            if (date.after(Date())) {
                appDataRepository.updateSubscriptionExpireDate(formattedDate)
            }
        }

    }
}