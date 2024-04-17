package com.ardrawing.sketchtrace.my_creation.presentation.my_creation_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.my_creation.domian.repository.CreationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class MyCreationDetailsViewModel @Inject constructor(
    private val creationRepository: CreationRepository,
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    


    init {
        
    }

    fun onEvent(myCreationDetailsUiEvent: MyCreationDetailsUiEvent) {
        when (myCreationDetailsUiEvent) {
            is MyCreationDetailsUiEvent.DeleteCreation -> {
                viewModelScope.launch {
                    creationRepository.deleteCreation(
                        myCreationDetailsUiEvent.creationUri
                    )
                }
            }
        }
    }
}


























