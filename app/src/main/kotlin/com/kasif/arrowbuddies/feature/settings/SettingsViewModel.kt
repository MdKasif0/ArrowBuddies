package com.kasif.arrowbuddies.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: GameRepository,
    private val soundManager: SoundManager,
    private val hapticsManager: HapticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.soundEnabled.collect { enabled ->
                _uiState.update { it.copy(soundEnabled = enabled) }
                soundManager.setSoundEnabled(enabled)
            }
        }
        viewModelScope.launch {
            repository.musicEnabled.collect { enabled ->
                _uiState.update { it.copy(musicEnabled = enabled) }
                soundManager.setMusicEnabled(enabled)
            }
        }
        viewModelScope.launch {
            repository.hapticsEnabled.collect { enabled ->
                _uiState.update { it.copy(hapticsEnabled = enabled) }
                hapticsManager.setHapticsEnabled(enabled)
            }
        }
    }

    fun onEvent(event: SettingsUiEvent) {
        viewModelScope.launch {
            when (event) {
                is SettingsUiEvent.ToggleSound -> repository.setSoundEnabled(event.enabled)
                is SettingsUiEvent.ToggleMusic -> repository.setMusicEnabled(event.enabled)
                is SettingsUiEvent.ToggleHaptics -> repository.setHapticsEnabled(event.enabled)
            }
        }
    }
}

