package com.kasif.arrowbuddies.feature.game

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.kasif.arrowbuddies.core.ads.AdState
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.engine.model.GameState
import com.kasif.arrowbuddies.engine.model.GameStatus
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.LevelIdHelper
import com.kasif.arrowbuddies.engine.model.PeekingPath
import com.kasif.arrowbuddies.engine.model.Pos

enum class BoosterType {
    Hint,
    Undo,
    ExtraHeart
}

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
    val showPauseSheet: Boolean = false,
    val showLeaveLevelDialog: Boolean = false,
    val showBoosterShop: BoosterType? = null,
    val activeMechanicIntro: MechanicType? = null,
    val safeBuddyIds: Set<String> = emptySet(),
    val hintBuddyId: String? = null,
    @StringRes val tutorialSpeechBubble: Int? = null,
    val tutorialHandPos: Pos? = null,
    val hintsAvailable: Int = 3,
    val undosAvailable: Int = 3,
    val berries: Int = 100,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val newSpeciesUnlocked: BuddySpecies? = null,
    val goldenBerryBonus: Int = 0,
    val berriesEarned: Int = 0,
    val isTutorialLevel: Boolean = false,
    val showReminderPromptDialog: Boolean = false,
    val reduceMotion: Boolean = false,
    val adState: AdState = AdState.Unavailable,
    val isBerriesDoubled: Boolean = false,
    val remainingDailyAdHints: Int = 3,
    /** True while the level is being loaded in the background; the board isn't shown yet. */
    val isLoading: Boolean = false
) {
    val canShowAds: Boolean get() = (LevelIdHelper.parseFlatIndex(levelId) ?: 999) > 6
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
    data object RequestHint : GameUiEvent
    data object OpenPauseSheet : GameUiEvent
    data object DismissPauseSheet : GameUiEvent
    data object RequestLeaveLevel : GameUiEvent
    data object DismissLeaveLevel : GameUiEvent
    data object OpenBoosterShop : GameUiEvent
    data object DismissBoosterShop : GameUiEvent
    data class BuyBooster(val type: BoosterType) : GameUiEvent
    data object ReviveWithHeart : GameUiEvent
    data class ToggleSound(val enabled: Boolean) : GameUiEvent
    data class ToggleMusic(val enabled: Boolean) : GameUiEvent
    data class ToggleHaptics(val enabled: Boolean) : GameUiEvent
    data object AcceptDailyReminder : GameUiEvent
    data object DismissDailyReminder : GameUiEvent
    data class ReviveWithAd(val activity: Activity) : GameUiEvent
    data class DoubleBerriesWithAd(val activity: Activity) : GameUiEvent
    data class FreeHintWithAd(val activity: Activity) : GameUiEvent
}
