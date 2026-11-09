package com.kasif.arrowbuddies.feature.game

import androidx.compose.runtime.Immutable
import com.kasif.arrowbuddies.engine.model.GameState
import com.kasif.arrowbuddies.engine.model.GameStatus
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.PeekingPath

@Immutable
data class GameUiState(
    val levelId: String = "1",
    val level: LevelDef,
    val gameState: GameState,
    val isInputLocked: Boolean = false,
    val pressedBuddyId: String? = null,
    val activePeekingPath: PeekingPath? = null,
    val showLevelCompleteDialog: Boolean = false,
    val showLevelFailedDialog: Boolean = false,
    val activeMechanicIntro: MechanicType? = null,
    val safeBuddyIds: Set<String> = emptySet()
) {
    val heartsRemaining: Int get() = gameState.hearts
    val rescuedCount: Int get() = gameState.rescuedIds.size
    val totalBuddiesInLevel: Int get() = level.buddies.size
    val canUndo: Boolean get() = gameState.history.isNotEmpty() && !isInputLocked && gameState.status == GameStatus.PLAYING
    val isWon: Boolean get() = gameState.status == GameStatus.WON
    val isLost: Boolean get() = gameState.status == GameStatus.LOST
    val starsEarned: Int get() = gameState.stars
}

sealed interface GameUiEvent {
    data class TapBuddy(val buddyId: String) : GameUiEvent
    data class PressBuddy(val buddyId: String) : GameUiEvent
    data object ReleaseBuddy : GameUiEvent
    data class StartPathPeek(val buddyId: String) : GameUiEvent
    data object EndPathPeek : GameUiEvent
    data object Undo : GameUiEvent
    data object RestartLevel : GameUiEvent
    data class LoadLevel(val levelId: String) : GameUiEvent
    data object DismissDialog : GameUiEvent
    data object DismissMechanicIntro : GameUiEvent
    data object NextLevel : GameUiEvent
}
