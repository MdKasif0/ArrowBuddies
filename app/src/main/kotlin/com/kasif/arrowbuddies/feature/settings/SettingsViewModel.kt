package com.kasif.arrowbuddies.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.core.notification.DailyReminderScheduler
import com.kasif.arrowbuddies.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val hapticsManager: HapticsManager,
    @ApplicationContext private val context: Context? = null
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
        viewModelScope.launch {
            repository.reduceMotion.collect { enabled ->
                _uiState.update { it.copy(reduceMotion = enabled) }
            }
        }
        viewModelScope.launch {
            repository.dailyReminderEnabled.collect { enabled ->
                _uiState.update { it.copy(dailyReminderEnabled = enabled) }
            }
        }
    }

    fun onEvent(event: SettingsUiEvent) {
        viewModelScope.launch {
            when (event) {
                is SettingsUiEvent.ToggleSound -> repository.setSoundEnabled(event.enabled)
                is SettingsUiEvent.ToggleMusic -> repository.setMusicEnabled(event.enabled)
                is SettingsUiEvent.ToggleHaptics -> repository.setHapticsEnabled(event.enabled)
                is SettingsUiEvent.ToggleReduceMotion -> repository.setReduceMotion(event.enabled)
                is SettingsUiEvent.ToggleDailyReminder -> {
                    repository.setDailyReminderEnabled(event.enabled)
                    context?.let { ctx ->
                        if (event.enabled) {
                            DailyReminderScheduler.scheduleDailyReminder(ctx)
                        } else {
                            DailyReminderScheduler.cancelDailyReminder(ctx)
                        }
                    }
                }
                is SettingsUiEvent.OpenPrivacyPolicy -> _uiState.update { it.copy(showPrivacyPolicyDialog = true) }
                is SettingsUiEvent.DismissPrivacyPolicy -> _uiState.update { it.copy(showPrivacyPolicyDialog = false) }
                is SettingsUiEvent.OpenCredits -> _uiState.update { it.copy(showCreditsDialog = true) }
                is SettingsUiEvent.DismissCredits -> _uiState.update { it.copy(showCreditsDialog = false) }
                is SettingsUiEvent.ReplayTutorial -> {
                    repository.resetTutorial()
                }
            }
        }
    }
}
