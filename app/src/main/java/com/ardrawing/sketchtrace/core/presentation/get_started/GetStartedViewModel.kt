package com.ardrawing.sketchtrace.core.presentation.get_started

import androidx.lifecycle.ViewModel
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
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
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    private val _getStartedState = MutableStateFlow(GetStartedState())
    val getsStartedState = _getStartedState.asStateFlow()

    private val _languageCode = MutableStateFlow("en")
    val languageCode = _languageCode.asStateFlow()

    private val _appData = MutableStateFlow<AppData?>(null)
    val appData = _appData.asStateFlow()

    init {
        _languageCode.update {
            appDataRepository.getLanguageCode()
        }
        _getStartedState.update {
            it.copy(
                appData = appDataRepository.getAppData()
            )
        }

        _appData.update {
            appDataRepository.getAppData()
        }
    }

    fun onEvent(getStartedUiEvent: GetStartedUiEvent) {
        when (getStartedUiEvent) {
            GetStartedUiEvent.ShowHidePrivacyDialog -> {
                _getStartedState.update {
                    it.copy(showPrivacyDialog = !getsStartedState.value.showPrivacyDialog)
                }
            }
        }
    }
}


























