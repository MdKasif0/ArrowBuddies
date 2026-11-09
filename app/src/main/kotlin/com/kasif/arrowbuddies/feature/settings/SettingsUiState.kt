package com.kasif.arrowbuddies.feature.settings

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsUiState(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reduceMotion: Boolean = false,
    val dailyReminderEnabled: Boolean = false,
    val showPrivacyPolicyDialog: Boolean = false,
    val showCreditsDialog: Boolean = false
)

sealed interface SettingsUiEvent {
    data class ToggleSound(val enabled: Boolean) : SettingsUiEvent
    data class ToggleMusic(val enabled: Boolean) : SettingsUiEvent
    data class ToggleHaptics(val enabled: Boolean) : SettingsUiEvent
    data class ToggleReduceMotion(val enabled: Boolean) : SettingsUiEvent
    data class ToggleDailyReminder(val enabled: Boolean) : SettingsUiEvent
    data object OpenPrivacyPolicy : SettingsUiEvent
    data object DismissPrivacyPolicy : SettingsUiEvent
    data object OpenCredits : SettingsUiEvent
    data object DismissCredits : SettingsUiEvent
    data object ReplayTutorial : SettingsUiEvent
}
