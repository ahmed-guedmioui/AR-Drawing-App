package com.ardrawing.sketchtrace.util.other_util

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView

/**
 * @author Ahmed Guedmioui
 */
class MultiTouchImageView(context: Context, attrs: AttributeSet) :
    AppCompatImageView(context, attrs) {

    private val multiTouch = MultiTouch(this)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return multiTouch.onTouch(this, event)
    }
}
