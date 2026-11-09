package com.kasif.arrowbuddies.feature.sanctuary

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SanctuaryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SanctuaryUiState())
    val uiState: StateFlow<SanctuaryUiState> = _uiState.asStateFlow()

    fun onEvent(event: SanctuaryUiEvent) {
        // Handled in Step 6
    }
}

