package com.ardrawing.sketchtrace.util.other_util;

import android.app.Activity;

import com.ardrawing.sketchtrace.R;


public class AppConstants {
    public static void overridePendingTransitionEnter(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_slide_from_right, R.anim.activity_slide_to_left);
    }
    public static void overridePendingTransitionExit(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_slide_from_left, R.anim.activity_slide_to_right);
    }
}
