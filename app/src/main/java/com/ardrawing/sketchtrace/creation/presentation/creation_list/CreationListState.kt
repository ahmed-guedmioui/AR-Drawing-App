package com.ardrawing.sketchtrace.creation.presentation.creation_list

import com.ardrawing.sketchtrace.creation.domian.model.Creation


/**
 * @author Ahmed Guedmioui
 */
data class CreationListState(
    val creationList: List<Creation> = emptyList()
)