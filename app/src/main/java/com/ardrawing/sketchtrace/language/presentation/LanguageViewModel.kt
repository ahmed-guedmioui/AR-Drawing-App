package com.ardrawing.sketchtrace.language.presentation

import androidx.lifecycle.ViewModel
import com.ardrawing.sketchtrace.core.domain.repository.CoreRepository
import com.ardrawing.sketchtrace.language.domain.repository.LanguageRepository
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
    private val languageRepository: LanguageRepository,
    private val coreRepository: CoreRepository,
) : ViewModel() {

    private val _languageState = MutableStateFlow(LanguageState())
    val languageState = _languageState.asStateFlow()

    init {
        _languageState.update {
            it.copy(language = languageRepository.getLanguageCode())
        }
    }

    fun onEvent(languageUiEvent: LanguageUiEvent) {
        when (languageUiEvent) {
            is LanguageUiEvent.ChangeLanguage -> {
                _languageState.update {
                    it.copy(language = languageUiEvent.language)
                }
                languageRepository.updateLanguageCode(languageUiEvent.language)
            }

            LanguageUiEvent.Navigate -> {
                coreRepository.setLanguageShown()
            }
        }
    }
}


























