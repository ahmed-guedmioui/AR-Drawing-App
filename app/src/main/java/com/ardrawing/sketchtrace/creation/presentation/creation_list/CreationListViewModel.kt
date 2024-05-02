package com.ardrawing.sketchtrace.creation.presentation.creation_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.creation.domian.repository.CreationRepository
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
class CreationListViewModel @Inject constructor(
    private val creationRepository: CreationRepository,
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    private val _creationListState = MutableStateFlow(CreationListState())
    val myCreationState = _creationListState.asStateFlow()

    

    init {
        
        getCreationList()
    }

    private fun getCreationList() {
        viewModelScope.launch {
            creationRepository.getCreationList().collect { creationList ->
                _creationListState.update {
                    it.copy(creationList = creationList)
                }
            }
        }
    }
}


























