package com.kasif.arrowbuddies.feature.daily

import androidx.compose.runtime.Immutable

@Immutable
data class DailyRescueUiState(
    val streakDays: Int = 0,
    val isCompletedToday: Boolean = false
)

sealed interface DailyRescueUiEvent {
    data object PlayDaily : DailyRescueUiEvent
}

