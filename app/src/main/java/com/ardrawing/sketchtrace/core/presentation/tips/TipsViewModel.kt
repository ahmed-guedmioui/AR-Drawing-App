package com.ardrawing.sketchtrace.core.presentation.tips

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
class TipsViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    private val _tipsState = MutableStateFlow(TipsState())
    val tipsState = _tipsState.asStateFlow()

    private val _appData = MutableStateFlow<AppData?>(null)
    val appData = _appData.asStateFlow()

    init {
        _appData.update {
            appDataRepository.getAppData()
        }
    }

    fun onEvent(tipsUiEvent: TipsUiEvent) {
        when (tipsUiEvent) {
            TipsUiEvent.NextTip -> {
                _tipsState.update {
                    it.copy(tipNum = tipsState.value.tipNum + 1)
                }
            }

            TipsUiEvent.Back -> {
                _tipsState.update {
                    it.copy(tipNum = tipsState.value.tipNum - 1)
                }
            }
        }
    }
}


























