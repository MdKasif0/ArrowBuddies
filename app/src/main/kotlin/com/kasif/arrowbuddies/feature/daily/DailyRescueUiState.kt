package com.kasif.arrowbuddies.feature.daily

import androidx.compose.runtime.Immutable
import com.kasif.arrowbuddies.core.economy.EconomyConfig

@Immutable
data class StreakDayReward(
    val dayNumber: Int,
    val berries: Int,
    val hasDecorGift: Boolean = false,
    val isClaimed: Boolean = false,
    val isCurrent: Boolean = false
)

@Immutable
data class DailyRescueUiState(
    val todayDateFormatted: String = "",
    val dateRaw: String = "",
    val streakDays: Int = 0,
    val isCompletedToday: Boolean = false,
    val todayStars: Int = 0,
    val todayMistakes: Int = 0,
    val streakRewards: List<StreakDayReward> = emptyList(),
    val berries: Int = 100,
    val worldThemeIndex: Int = 1,
    val buddyCount: Int = 8,
    val dailyLevelId: String = "daily"
)

sealed interface DailyRescueUiEvent {
    data object PlayDailyRescue : DailyRescueUiEvent
    data object ShareResult : DailyRescueUiEvent
}
