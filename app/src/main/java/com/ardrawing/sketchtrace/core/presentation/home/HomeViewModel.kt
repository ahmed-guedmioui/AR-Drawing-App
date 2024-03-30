package com.ardrawing.sketchtrace.core.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeState())
    val homeState = _homeState.asStateFlow()

    private val _appData = MutableStateFlow<AppData?>(null)
    val appData = _appData.asStateFlow()

    private val _doubleTapToastChannel = Channel<Boolean>()
    val doubleTapToastChannel = _doubleTapToastChannel.receiveAsFlow()

    private val _closeChannel = Channel<Boolean>()
    val closeChannel = _closeChannel.receiveAsFlow()

    init {
        _appData.update {
            appDataRepository.getAppData()
        }
    }

    fun onEvent(homeUiEvent: HomeUiEvent) {
        when (homeUiEvent) {
            HomeUiEvent.BackPressed -> {

                if (homeState.value.doubleBackToExitPressedOnce) {
                    viewModelScope.launch {
                        _closeChannel.send(true)
                    }

                } else {
                    viewModelScope.launch {
                        _doubleTapToastChannel.send(true)
                        _homeState.update {
                            it.copy(doubleBackToExitPressedOnce = true)
                        }
                        delay(2000)
                        _homeState.update {
                            it.copy(doubleBackToExitPressedOnce = false)
                        }
                    }
                }
            }

            HomeUiEvent.ShowHideHelperDialog -> {
                _homeState.update {
                    it.copy(showHelperDialog = !homeState.value.showHelperDialog)
                }
            }
        }
    }
}


























