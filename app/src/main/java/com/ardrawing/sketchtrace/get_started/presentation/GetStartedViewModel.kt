package com.ardrawing.sketchtrace.get_started.presentation

import androidx.lifecycle.ViewModel
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.core.domain.repository.CoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class GetStartedViewModel @Inject constructor(
    private val coreRepository: CoreRepository,
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    private val _getStartedState = MutableStateFlow(GetStartedState())
    val getsStartedState = _getStartedState.asStateFlow()

    init {
        _getStartedState.update {
            it.copy(
                appData = appDataRepository.getAppData()
            )
        }
    }

    fun onEvent(getStartedUiEvent: GetStartedUiEvent) {
        when (getStartedUiEvent) {
            GetStartedUiEvent.ShowHidePrivacyDialog -> {
                _getStartedState.update {
                    it.copy(showPrivacyDialog = !getsStartedState.value.showPrivacyDialog)
                }
            }

            GetStartedUiEvent.Navigate -> {
                coreRepository.setGetStartedShown()
            }
        }
    }
}


























