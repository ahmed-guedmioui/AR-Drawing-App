package com.ardrawing.sketchtrace.core.presentation.onboarding

import androidx.lifecycle.ViewModel
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
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
class OnboardingViewModel @Inject constructor(
    private val coreRepository: CoreRepository,
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    private val _onboardingState = MutableStateFlow(OnboardingState())
    val tipsState = _onboardingState.asStateFlow()

    

    private val _appData = MutableStateFlow<AppData?>(null)
    val appData = _appData.asStateFlow()

    init {
        
        _appData.update {
            appDataRepository.getAppData()
        }
    }

    fun onEvent(onboardingUiEvent: OnboardingUiEvent) {
        when (onboardingUiEvent) {
            OnboardingUiEvent.NextTip -> {
                _onboardingState.update {
                    it.copy(tipNum = tipsState.value.tipNum + 1)
                }
            }

            OnboardingUiEvent.Back -> {
                _onboardingState.update {
                    it.copy(tipNum = tipsState.value.tipNum - 1)
                }
            }

            OnboardingUiEvent.Navigate -> {
                coreRepository.updateIsOnboardingShown()
            }
        }
    }
}


























