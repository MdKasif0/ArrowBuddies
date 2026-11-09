package com.kasif.arrowbuddies.feature.map

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class WorldMapViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(WorldMapUiState())
    val uiState: StateFlow<WorldMapUiState> = _uiState.asStateFlow()

    fun onEvent(event: WorldMapUiEvent) {
        when (event) {
            is WorldMapUiEvent.SelectWorld -> {
                _uiState.update { it.copy(selectedWorldId = event.worldId) }
            }
        }
    }
}

