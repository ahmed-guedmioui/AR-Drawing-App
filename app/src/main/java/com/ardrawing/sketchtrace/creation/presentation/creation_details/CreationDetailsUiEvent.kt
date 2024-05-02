package com.ardrawing.sketchtrace.creation.presentation.creation_details

/**
 * @author Ahmed Guedmioui
 */
sealed interface CreationDetailsUiEvent {
    data class DeleteCreation(
        val creationUri: String
    ) : CreationDetailsUiEvent
}