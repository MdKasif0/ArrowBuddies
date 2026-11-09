package com.kasif.arrowbuddies.feature.daily

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DailyRescueViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DailyRescueUiState())
    val uiState: StateFlow<DailyRescueUiState> = _uiState.asStateFlow()

    fun onEvent(event: DailyRescueUiEvent) {
        // Handled in Step 9
    }
}

