package com.kasif.arrowbuddies.feature.splash

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState(isLoading = false, initialLevelId = "1"))
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    fun onEvent(event: SplashUiEvent) {
        // Event handling
    }
}

