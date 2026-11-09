package com.kasif.arrowbuddies.feature.game

import androidx.compose.runtime.Immutable

@Immutable
data class GameUiState(
    val levelId: String = "1",
    val heartsRemaining: Int = 3,
    val rescuedCount: Int = 0,
    val totalBuddiesInLevel: Int = 4
)

sealed interface GameUiEvent {
    data object RestartLevel : GameUiEvent
    data object TapBuddy : GameUiEvent
}

