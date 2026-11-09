package com.kasif.arrowbuddies.feature.settings

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsUiState(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)

sealed interface SettingsUiEvent {
    data class ToggleSound(val enabled: Boolean) : SettingsUiEvent
    data class ToggleMusic(val enabled: Boolean) : SettingsUiEvent
    data class ToggleHaptics(val enabled: Boolean) : SettingsUiEvent
}

