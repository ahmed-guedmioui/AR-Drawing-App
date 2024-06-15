package com.ardrawing.sketchtrace.core.domain.repository.ads

import android.app.Activity
import android.widget.FrameLayout
import android.widget.TextView

/**
 * @author Ahmed Guedmioui
 */
interface NativeManager {

    fun loadNative(
        nativeFrame: FrameLayout,
        nativeTemp: TextView,
        isButtonTop: Boolean = false,
        activity: Activity
    )

}