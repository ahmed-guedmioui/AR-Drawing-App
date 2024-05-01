package com.ardrawing.sketchtrace.paywall.presentation

import android.graphics.drawable.Drawable
import java.util.Date

/**
 * @author Ahmed Guedmioui
 */
sealed interface PaywallUiEvent {
    data class ShowHideFaq(
        val faqNumber: Int
    ) : PaywallUiEvent
}