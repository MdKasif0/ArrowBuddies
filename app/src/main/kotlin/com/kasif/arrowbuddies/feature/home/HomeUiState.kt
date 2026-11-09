package com.kasif.arrowbuddies.feature.home

import androidx.compose.runtime.Immutable
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies

@Immutable
data class HomeUiState(
    val totalRescued: Int = 0,
    val berries: Int = 100,
    val currentLevelIndex: Int = 1,
    val currentLevelId: String = "w1-l01",
    val nextSpecies: BuddySpecies = BuddySpecies.BUNNY,
    val isDailyDone: Boolean = false,
    val dailyCountdownFormatted: String = "12h 30m",
    val isRescueRunUnlocked: Boolean = false,
    val unlockedSpeciesCount: Int = 1,
    val sanctuaryBuddiesCount: Int = 0
)

sealed interface HomeUiEvent {
    data object Refresh : HomeUiEvent
}
