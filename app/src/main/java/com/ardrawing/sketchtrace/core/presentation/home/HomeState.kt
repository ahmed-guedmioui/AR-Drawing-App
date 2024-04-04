package com.ardrawing.sketchtrace.core.presentation.home

import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData


/**
 * @author Ahmed Guedmioui
 */
data class HomeState(
     var doubleBackToExitPressedOnce: Boolean = false,
     val showHelperDialog: Boolean = false,
     val appData: AppData? = null
)