package com.ardrawing.sketchtrace.core.presentation.language

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
class LanguageViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    private val _languageState = MutableStateFlow(LanguageState())
    val languageState = _languageState.asStateFlow()

    private val _appData = MutableStateFlow<AppData?>(null)
    val appData = _appData.asStateFlow()

    init {
        _appData.update {
            appDataRepository.getAppData()
        }
    }

    fun onEvent(languageUiEvent: LanguageUiEvent) {
        when (languageUiEvent) {
            is LanguageUiEvent.ChangeLanguage -> {
                _languageState.update {
                    it.copy(language = languageUiEvent.language)
                }
            }
        }
    }
}


























