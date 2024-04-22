package com.ardrawing.sketchtrace.onboarding.presentation

import androidx.lifecycle.ViewModel
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
    private val coreRepository: CoreRepository
) : ViewModel() {

    private val _onboardingState = MutableStateFlow(OnboardingState())
    val tipsState = _onboardingState.asStateFlow()

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


























