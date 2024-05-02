package com.ardrawing.sketchtrace.creation.presentation.creation_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.creation.domian.repository.CreationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class MyCreationDetailsViewModel @Inject constructor(
    private val creationRepository: CreationRepository
) : ViewModel() {

    fun onEvent(creationDetailsUiEvent: CreationDetailsUiEvent) {
        when (creationDetailsUiEvent) {
            is CreationDetailsUiEvent.DeleteCreation -> {
                viewModelScope.launch {
                    creationRepository.deleteCreation(
                        creationDetailsUiEvent.creationUri
                    )
                }
            }
        }
    }
}


























