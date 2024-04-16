package com.ardrawing.sketchtrace.core.presentation.settings

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
class SettingsViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState = _settingsState.asStateFlow()

    private val _languageCode = MutableStateFlow("en")
    val languageCode = _languageCode.asStateFlow()

    private val _appData = MutableStateFlow<AppData?>(null)
    val appData = _appData.asStateFlow()

    init {
        _languageCode.update {
            appDataRepository.getLanguageCode()
        }
        _settingsState.update {
            it.copy(
                appData = appDataRepository.getAppData()
            )
        }
        _appData.update {
            appDataRepository.getAppData()
        }
    }

    fun onEvent(settingsUiEvent: SettingsUiEvent) {
        when (settingsUiEvent) {
            SettingsUiEvent.ShowHidePrivacyDialog -> {
                _settingsState.update {
                    it.copy(showPrivacyDialog = !settingsState.value.showPrivacyDialog)
                }
            }

            SettingsUiEvent.UpdateAppData -> {
                _settingsState.update {
                    it.copy(
                        appData = appDataRepository.getAppData()
                    )
                }
            }
        }
    }

}


























