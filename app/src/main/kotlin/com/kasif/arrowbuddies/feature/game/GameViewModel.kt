package com.kasif.arrowbuddies.feature.game

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.ads.AdPlacement
import com.kasif.arrowbuddies.core.ads.AdState
import com.kasif.arrowbuddies.core.ads.AdsManager
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.core.notification.DailyReminderScheduler
import com.kasif.arrowbuddies.data.repository.GameRepository
import com.kasif.arrowbuddies.data.repository.LevelRepository
import com.kasif.arrowbuddies.engine.GameEngine
import com.kasif.arrowbuddies.engine.model.GameState
import com.kasif.arrowbuddies.engine.model.GameStatus
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.LevelIdHelper
import com.kasif.arrowbuddies.engine.model.MoveResult
import com.kasif.arrowbuddies.engine.runtime.DailyRescue
import com.kasif.arrowbuddies.engine.model.Pos
import com.kasif.arrowbuddies.engine.model.RefusalReason
import com.kasif.arrowbuddies.engine.model.Tile
import com.kasif.arrowbuddies.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.kasif.arrowbuddies.core.economy.EconomyConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val levelRepository: LevelRepository,
    private val gameRepository: GameRepository,
    private val adsManager: AdsManager,
    private val savedStateHandle: SavedStateHandle,
    private val hapticsManager: HapticsManager,
    private val soundManager: SoundManager,
    @ApplicationContext private val context: Context? = null
) : ViewModel() {

    private val json = Json {
        ignoreUnknownKeys = true
        allowStructuredMapKeys = true
    }

    private val route = runCatching { savedStateHandle.toRoute<Route.Game>() }.getOrNull()
    private val initialLevelId = route?.levelId
        ?: savedStateHandle.get<String>("levelId")
        ?: "1"

    /** Dispatcher for CPU-heavy solver work (hints). Overridable in tests. */
    internal var computeDispatcher: CoroutineDispatcher = Dispatchers.Default

    /** The in-flight move animation; cancelled when the board is reset so it can't overwrite it. */
    private var moveJob: Job? = null
    private var hintJob: Job? = null
    private var loadJob: Job? = null

    private val _effects = MutableSharedFlow<GameEffect>(extraBufferCapacity = 16)
    val effects: SharedFlow<GameEffect> = _effects.asSharedFlow()

    private val _uiState: MutableStateFlow<GameUiState>

    init {
        val restoredStateJson = savedStateHandle.get<String>(KEY_SAVED_GAME_STATE)
        val restoredState = restoredStateJson?.let {
            runCatching { json.decodeFromString(GameState.serializer(), it) }.getOrNull()
        }

        // A level restored after process death carries its own definition: show it right away.
        // Otherwise show a loading placeholder and load the level off the main thread.
        val restoredLevel = restoredState?.level
        val placeholder = restoredLevel ?: placeholderLevel(initialLevelId)
        _uiState = MutableStateFlow(
            GameUiState(
                levelId = placeholder.id,
                level = placeholder,
                gameState = restoredState ?: GameState(level = placeholder, buddies = placeholder.buddies),
                isLoading = restoredLevel == null
            )
        )
        if (restoredState != null) {
            showLevel(restoredState.level, restoredState)
        } else {
            loadJob = viewModelScope.launch {
                val levelDef = levelRepository.loadLevelInBackground(initialLevelId)
                showLevel(levelDef, GameState(level = levelDef, buddies = levelDef.buddies))
            }
        }

        // Observe repository state for inventory and preferences
        viewModelScope.launch {
            launch { gameRepository.hintsCount.collect { c -> _uiState.update { it.copy(hintsAvailable = c) } } }
            launch { gameRepository.undosCount.collect { c -> _uiState.update { it.copy(undosAvailable = c) } } }
            launch { gameRepository.berries.collect { b -> _uiState.update { it.copy(berries = b) } } }
            launch { gameRepository.soundEnabled.collect { s -> _uiState.update { it.copy(soundEnabled = s) } } }
            launch { gameRepository.musicEnabled.collect { m -> _uiState.update { it.copy(musicEnabled = m) } } }
            launch { gameRepository.hapticsEnabled.collect { h -> _uiState.update { it.copy(hapticsEnabled = h) } } }
            launch { gameRepository.reduceMotion.collect { r -> _uiState.update { it.copy(reduceMotion = r) } } }
            launch { adsManager.adState.collect { s -> _uiState.update { it.copy(adState = s) } } }
            launch { gameRepository.getRemainingDailyAdHints().collect { rem -> _uiState.update { it.copy(remainingDailyAdHints = rem) } } }
        }

    }

    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun onEvent(event: GameUiEvent) {
        when (event) {
            is GameUiEvent.TapBuddy -> handleBuddyTap(event.buddyId)
            is GameUiEvent.PressBuddy -> handleBuddyPressed(event.buddyId)
            is GameUiEvent.ReleaseBuddy -> handleBuddyReleased()
            is GameUiEvent.StartPathPeek -> handlePathPeekStart(event.buddyId)
            is GameUiEvent.EndPathPeek -> handlePathPeekEnd()
            is GameUiEvent.Undo -> handleUndo()
            is GameUiEvent.RestartLevel -> handleRestart()
            is GameUiEvent.LoadLevel -> handleLoadLevel(event.levelId)
            is GameUiEvent.DismissDialog -> handleDismissDialog()
            is GameUiEvent.DismissMechanicIntro -> handleDismissMechanicIntro()
            is GameUiEvent.NextLevel -> handleNextLevel()
            is GameUiEvent.RequestHint -> handleRequestHint()
            is GameUiEvent.OpenPauseSheet -> _uiState.update { it.copy(showPauseSheet = true) }
            is GameUiEvent.DismissPauseSheet -> _uiState.update { it.copy(showPauseSheet = false) }
            is GameUiEvent.RequestLeaveLevel -> _uiState.update { it.copy(showLeaveLevelDialog = true) }
            is GameUiEvent.DismissLeaveLevel -> _uiState.update { it.copy(showLeaveLevelDialog = false) }
            is GameUiEvent.OpenBoosterShop -> _uiState.update { it.copy(showBoosterShop = BoosterType.Hint) }
            is GameUiEvent.DismissBoosterShop -> _uiState.update { it.copy(showBoosterShop = null) }
            is GameUiEvent.BuyBooster -> handleBuyBooster(event.type)
            is GameUiEvent.ReviveWithHeart -> handleReviveWithHeart()
            is GameUiEvent.ToggleSound -> handleToggleSound(event.enabled)
            is GameUiEvent.ToggleMusic -> handleToggleMusic(event.enabled)
            is GameUiEvent.ToggleHaptics -> handleToggleHaptics(event.enabled)
            is GameUiEvent.AcceptDailyReminder -> {
                viewModelScope.launch {
                    gameRepository.setDailyReminderEnabled(true)
                    gameRepository.setReminderPromptShown(true)
                    context?.let { DailyReminderScheduler.scheduleDailyReminder(it) }
                    _uiState.update { it.copy(showReminderPromptDialog = false, showLevelCompleteDialog = true) }
                }
            }
            is GameUiEvent.DismissDailyReminder -> {
                viewModelScope.launch {
                    gameRepository.setReminderPromptShown(true)
                    _uiState.update { it.copy(showReminderPromptDialog = false, showLevelCompleteDialog = true) }
                }
            }
            is GameUiEvent.ReviveWithAd -> handleReviveWithAd(event.activity)
            is GameUiEvent.DoubleBerriesWithAd -> handleDoubleBerriesWithAd(event.activity)
            is GameUiEvent.FreeHintWithAd -> handleFreeHintWithAd(event.activity)
        }
    }

    private fun handleBuddyPressed(buddyId: String) {
        if (_uiState.value.isInputLocked || _uiState.value.gameState.status != GameStatus.PLAYING) return
        _uiState.update { it.copy(pressedBuddyId = buddyId) }
        viewModelScope.launch {
            _effects.emit(GameEffect.TriggerHaptic(HapticType.TICK))
        }
    }

    private fun handleBuddyReleased() {
        _uiState.update { it.copy(pressedBuddyId = null) }
    }

    private fun handleBuddyTap(buddyId: String) {
        val currentState = _uiState.value
        if (currentState.isLoading) return
        if (currentState.isInputLocked || currentState.gameState.status != GameStatus.PLAYING) return

        val buddy = currentState.gameState.findBuddy(buddyId) ?: return

        // 1. Immediate visual + haptic feedback (< 50ms)
        viewModelScope.launch {
            _effects.emit(GameEffect.TriggerHaptic(HapticType.TICK))
        }

        // 2. Delegate rule evaluation exclusively to pure GameEngine
        val outcome = GameEngine.tap(currentState.gameState, buddyId)

        when (val result = outcome.result) {
            is MoveResult.Escaped -> {
                val dashMs = (result.path.size * 55L).coerceAtMost(450L)
                val totalAnimMs = 70L + dashMs + 320L

                _uiState.update {
                    it.copy(
                        isInputLocked = true,
                        pressedBuddyId = null,
                        activePeekingPath = null,
                        tutorialSpeechBubble = null,
                        tutorialHandPos = null
                    )
                }

                moveJob = viewModelScope.launch {
                    _effects.emit(GameEffect.PlayEscape(buddyId, result.path, result.exitSide, buddy))
                    _effects.emit(GameEffect.PlaySound(SoundType.ESCAPE))

                    // Key Carrier Escaped -> Unlock matching cages
                    val carrierKeyColor = buddy.keyColor
                    if (carrierKeyColor != null) {
                        val matchingCages = currentState.gameState.buddies.filter { it.cageColor == carrierKeyColor }
                        if (matchingCages.isNotEmpty()) {
                            _effects.emit(
                                GameEffect.PlayKeyUnlock(
                                    keyColor = carrierKeyColor,
                                    fromPos = buddy.pos,
                                    targetPositions = matchingCages.map { it.pos }
                                )
                            )
                            _effects.emit(GameEffect.PlaySound(SoundType.KEY_UNLOCK))
                        }
                    }

                    // Sleepy buddies waking up if neighbor vacated
                    val newlyAwakened = outcome.newState.buddies.filter { newB ->
                        val oldB = currentState.gameState.findBuddy(newB.id)
                        oldB != null && oldB.sleepy && !newB.sleepy
                    }
                    for (awakened in newlyAwakened) {
                        _effects.emit(GameEffect.PlayWakeUp(awakened.id, awakened.pos))
                        _effects.emit(GameEffect.PlaySound(SoundType.WAKE_UP))
                    }

                    delay(totalAnimMs)
                    _effects.emit(GameEffect.TriggerHaptic(HapticType.CONFIRM))

                    persistGameState(outcome.newState)
                    val safeIds = computeSafeBuddyIds(outcome.newState)
                    _uiState.update {
                        it.copy(
                            gameState = outcome.newState,
                            isInputLocked = false,
                            safeBuddyIds = safeIds
                        )
                    }

                    if (outcome.newState.status == GameStatus.WON) {
                        onLevelWon(outcome.newState)
                    }
                }
            }

            is MoveResult.Bonked -> {
                val dashMs = (result.path.size * 45L).coerceAtMost(280L)

                // Tutorial protection: never lose hearts during tutorial
                val safeNewState = if (currentState.isTutorialLevel) {
                    outcome.newState.copy(hearts = 3, mistakes = 0, status = GameStatus.PLAYING)
                } else {
                    outcome.newState
                }

                val speechBubbleText = if (currentState.isTutorialLevel && (currentState.levelId == "w1-l02" || currentState.levelId == "2")) {
                    R.string.ui_tutorial_free_friend
                } else null

                val tutorialHandPos = if (currentState.isTutorialLevel && (currentState.levelId == "w1-l02" || currentState.levelId == "2")) {
                    Pos(2, 2) // Point to the unblocked buddy
                } else null

                _uiState.update { it.copy(isInputLocked = true, pressedBuddyId = null, activePeekingPath = null) }

                moveJob = viewModelScope.launch {
                    _effects.emit(GameEffect.PlayBonk(buddyId, result.path, result.blockerPos, result.blockerKind, buddy))
                    _effects.emit(GameEffect.PlaySound(SoundType.BONK))

                    delay(dashMs)
                    _effects.emit(GameEffect.ScreenShake(intensityDp = 4f, durationMs = 180L))
                    _effects.emit(GameEffect.TriggerHaptic(HapticType.REJECT))

                    delay(260L)
                    persistGameState(safeNewState)
                    val safeIds = computeSafeBuddyIds(safeNewState)
                    _uiState.update {
                        it.copy(
                            gameState = safeNewState,
                            isInputLocked = false,
                            safeBuddyIds = safeIds,
                            tutorialSpeechBubble = speechBubbleText,
                            tutorialHandPos = tutorialHandPos
                        )
                    }

                    if (safeNewState.status == GameStatus.LOST) {
                        _effects.emit(GameEffect.PlaySound(SoundType.LOSE))
                        delay(350L)
                        _uiState.update { it.copy(showLevelFailedDialog = true) }
                    }
                }
            }

            is MoveResult.Stopped -> {
                val dashMs = (result.path.size * 55L).coerceAtMost(350L)
                val targetPos = result.path.lastOrNull()?.pos ?: buddy.pos

                _uiState.update { it.copy(isInputLocked = true, pressedBuddyId = null, activePeekingPath = null) }

                moveJob = viewModelScope.launch {
                    _effects.emit(GameEffect.PlayMudStop(buddyId, result.path, targetPos, buddy))
                    _effects.emit(GameEffect.PlaySound(SoundType.SQUELCH))

                    val newlyAwakened = outcome.newState.buddies.filter { newB ->
                        val oldB = currentState.gameState.findBuddy(newB.id)
                        oldB != null && oldB.sleepy && !newB.sleepy
                    }
                    for (awakened in newlyAwakened) {
                        _effects.emit(GameEffect.PlayWakeUp(awakened.id, awakened.pos))
                        _effects.emit(GameEffect.PlaySound(SoundType.WAKE_UP))
                    }

                    delay(dashMs + 80L)
                    _effects.emit(GameEffect.TriggerHaptic(HapticType.TICK))

                    persistGameState(outcome.newState)
                    val safeIds = computeSafeBuddyIds(outcome.newState)
                    _uiState.update {
                        it.copy(
                            gameState = outcome.newState,
                            isInputLocked = false,
                            safeBuddyIds = safeIds
                        )
                    }
                }
            }

            is MoveResult.Refused -> {
                _uiState.update { it.copy(pressedBuddyId = null) }
                viewModelScope.launch {
                    _effects.emit(GameEffect.PlayRefusal(buddyId, result.reason, buddy.pos))
                    _effects.emit(GameEffect.TriggerHaptic(HapticType.TICK))
                    when (result.reason) {
                        RefusalReason.CAGED -> _effects.emit(GameEffect.PlaySound(SoundType.CAGE_LOCKED))
                        RefusalReason.SLEEPY -> _effects.emit(GameEffect.PlaySound(SoundType.SLEEPY_YAWN))
                    }
                }
            }

            is MoveResult.Ignored -> {
                _uiState.update { it.copy(pressedBuddyId = null) }
            }
        }
    }

    private fun onLevelWon(state: GameState) {
        viewModelScope.launch {
            _effects.emit(GameEffect.PlaySound(SoundType.WIN))

            val level = _uiState.value.level
            val hasGolden = level.buddies.any { it.isGolden }
            val levelSpecies = level.buddies.map { it.species }

            // Atomic database transaction via CompleteLevelUseCase in Room
            val completionResult = gameRepository.completeLevel(
                levelId = level.id,
                stars = state.stars,
                mistakes = state.mistakes,
                rescuedSpeciesList = levelSpecies,
                isGoldenBuddyRescued = hasGolden
            )

            // If daily rescue, also record completion & streak
            var dailyBonus = 0
            if (level.id.startsWith("daily")) {
                val dateStr = level.id.removePrefix("daily-").ifEmpty {
                    LocalDate.now(DailyRescue.TIME_ZONE).format(DateTimeFormatter.BASIC_ISO_DATE)
                }
                val firstDailyClear = gameRepository.getDailyRecord(dateStr).first()?.completed != true
                gameRepository.recordDailyCompletion(dateStr, state.stars, state.mistakes)
                if (firstDailyClear) dailyBonus = EconomyConfig.BERRIES_DAILY_RESCUE
            }

            if (_uiState.value.isTutorialLevel && (level.id == "w1-l03" || level.id == "3")) {
                gameRepository.setTutorialCompleted(true)
            }

            // Check if reminder opt-in should trigger (after level 5)
            val isLevel5 = level.id == "w1-l05" || level.id == "5" || level.index == 5
            val promptShown = gameRepository.reminderPromptShown.first()
            val shouldShowReminderPrompt = isLevel5 && !promptShown

            // Only celebrate a "new friend" on a tier-up or the first clear, not on every replay
            val newSpeciesEnum = completionResult.tierUpEvents.firstOrNull()?.species?.let { s ->
                runCatching { BuddySpecies.valueOf(s) }.getOrNull()
            } ?: levelSpecies.firstOrNull()?.takeIf { completionResult.isFirstClear }?.let {
                runCatching { BuddySpecies.valueOf(it) }.getOrNull()
            }

            delay(500L)
            _uiState.update {
                it.copy(
                    showLevelCompleteDialog = !shouldShowReminderPrompt,
                    showReminderPromptDialog = shouldShowReminderPrompt,
                    newSpeciesUnlocked = newSpeciesEnum,
                    goldenBerryBonus = if (hasGolden && completionResult.isFirstClear) EconomyConfig.BERRIES_GOLDEN_BUDDY_BONUS else 0,
                    berriesEarned = completionResult.berriesEarned + dailyBonus
                )
            }
        }
    }

    private fun handleRequestHint() {
        val state = _uiState.value
        if (state.isLoading) return
        if (state.isInputLocked || state.gameState.status != GameStatus.PLAYING) return
        if (hintJob?.isActive == true) return
        if (state.hintsAvailable <= 0) {
            _uiState.update { it.copy(showBoosterShop = BoosterType.Hint) }
            return
        }
        hintJob = viewModelScope.launch {
            // Solve off the main thread, and only spend a hint if one was actually found
            val hintBuddyId = withContext(computeDispatcher) { GameEngine.findHint(state.gameState) }
                ?: return@launch
            if (_uiState.value.gameState != state.gameState) return@launch // board changed meanwhile
            if (!gameRepository.useHint()) return@launch
            val buddy = state.gameState.findBuddy(hintBuddyId)
            _uiState.update {
                it.copy(
                    hintBuddyId = hintBuddyId,
                    tutorialHandPos = buddy?.pos
                )
            }
            _effects.emit(GameEffect.TriggerHaptic(HapticType.CONFIRM))
            delay(2000L)
            _uiState.update {
                it.copy(
                    hintBuddyId = null,
                    tutorialHandPos = null
                )
            }
        }
    }

    private fun handleUndo() {
        val currentState = _uiState.value
        if (currentState.isInputLocked || currentState.gameState.status != GameStatus.PLAYING) return
        // Nothing to undo: don't charge the player
        val restored = GameEngine.undo(currentState.gameState) ?: return

        if (currentState.undosAvailable <= 0) {
            _uiState.update { it.copy(showBoosterShop = BoosterType.Undo) }
            return
        }
        viewModelScope.launch {
            if (!gameRepository.useUndo()) return@launch
            if (_uiState.value.gameState != currentState.gameState) return@launch
            persistGameState(restored)
            val safeIds = computeSafeBuddyIds(restored)
            _uiState.update {
                it.copy(
                    gameState = restored,
                    safeBuddyIds = safeIds,
                    activePeekingPath = null,
                    pressedBuddyId = null
                )
            }
            _effects.emit(GameEffect.TriggerHaptic(HapticType.TICK))
        }
    }

    private fun handleBuyBooster(type: BoosterType) {
        viewModelScope.launch {
            when (type) {
                BoosterType.Hint -> gameRepository.buyHintWithBerries(EconomyConfig.COST_HINT)
                BoosterType.Undo -> gameRepository.buyUndoWithBerries(EconomyConfig.COST_UNDO)
                BoosterType.ExtraHeart -> {
                    // Hearts are already full: don't take berries for nothing
                    if (_uiState.value.gameState.hearts < 3 &&
                        gameRepository.spendBerries(EconomyConfig.COST_EXTRA_HEART)
                    ) {
                        _uiState.update {
                            it.copy(
                                gameState = it.gameState.copy(
                                    hearts = (it.gameState.hearts + 1).coerceAtMost(3),
                                    status = GameStatus.PLAYING
                                ),
                                showLevelFailedDialog = false
                            )
                        }
                    }
                }
            }
            _uiState.update { it.copy(showBoosterShop = null) }
        }
    }

    private fun handleReviveWithHeart() {
        viewModelScope.launch {
            if (gameRepository.spendBerries(EconomyConfig.COST_EXTRA_HEART)) {
                _uiState.update {
                    it.copy(
                        gameState = it.gameState.copy(
                            hearts = 1,
                            status = GameStatus.PLAYING
                        ),
                        showLevelFailedDialog = false
                    )
                }
                _effects.emit(GameEffect.TriggerHaptic(HapticType.CONFIRM))
            } else {
                _uiState.update { it.copy(showBoosterShop = BoosterType.ExtraHeart) }
            }
        }
    }

    private fun handleReviveWithAd(activity: Activity) {
        val state = _uiState.value
        if (!state.canShowAds || state.adState != AdState.Ready) return

        adsManager.showRewardedAd(
            activity = activity,
            placement = AdPlacement.FAIL_REVIVE,
            onUserEarnedReward = {
                _uiState.update {
                    it.copy(
                        gameState = it.gameState.copy(
                            hearts = 1,
                            status = GameStatus.PLAYING
                        ),
                        showLevelFailedDialog = false
                    )
                }
                viewModelScope.launch {
                    _effects.emit(GameEffect.TriggerHaptic(HapticType.CONFIRM))
                }
            },
            onAdFailedToShow = { error ->
                Log.w("GameViewModel", "Revive ad failed to show: $error")
            }
        )
    }

    private fun handleDoubleBerriesWithAd(activity: Activity) {
        val state = _uiState.value
        if (!state.canShowAds || state.adState != AdState.Ready || state.isBerriesDoubled) return

        adsManager.showRewardedAd(
            activity = activity,
            placement = AdPlacement.WIN_DOUBLE_BERRIES,
            onUserEarnedReward = {
                val currentEarned = _uiState.value.berriesEarned
                if (currentEarned > 0 && !_uiState.value.isBerriesDoubled) {
                    viewModelScope.launch {
                        gameRepository.addBerries(currentEarned)
                        _uiState.update {
                            it.copy(
                                isBerriesDoubled = true,
                                berriesEarned = currentEarned * 2
                            )
                        }
                        _effects.emit(GameEffect.TriggerHaptic(HapticType.CONFIRM))
                        _effects.emit(GameEffect.PlaySound(SoundType.WIN))
                    }
                }
            },
            onAdFailedToShow = { error ->
                Log.w("GameViewModel", "Double berries ad failed to show: $error")
            }
        )
    }

    private fun handleFreeHintWithAd(activity: Activity) {
        val state = _uiState.value
        if (!state.canShowAds || state.adState != AdState.Ready || state.remainingDailyAdHints <= 0) return

        adsManager.showRewardedAd(
            activity = activity,
            placement = AdPlacement.FREE_HINT,
            onUserEarnedReward = {
                viewModelScope.launch {
                    val claimed = gameRepository.claimFreeAdHint()
                    if (claimed) {
                        _uiState.update { it.copy(showBoosterShop = null) }
                        _effects.emit(GameEffect.TriggerHaptic(HapticType.CONFIRM))
                        handleRequestHint()
                    }
                }
            },
            onAdFailedToShow = { error ->
                Log.w("GameViewModel", "Free hint ad failed to show: $error")
            }
        )
    }

    private fun handleToggleSound(enabled: Boolean) {
        viewModelScope.launch { gameRepository.setSoundEnabled(enabled) }
    }

    private fun handleToggleMusic(enabled: Boolean) {
        viewModelScope.launch { gameRepository.setMusicEnabled(enabled) }
    }

    private fun handleToggleHaptics(enabled: Boolean) {
        viewModelScope.launch { gameRepository.setHapticsEnabled(enabled) }
    }

    private fun handlePathPeekStart(buddyId: String) {
        val currentState = _uiState.value
        if (currentState.isInputLocked || currentState.gameState.status != GameStatus.PLAYING) return

        val path = GameEngine.peekPath(currentState.gameState, buddyId) ?: return

        _uiState.update { it.copy(activePeekingPath = path) }
        viewModelScope.launch {
            _effects.emit(GameEffect.TriggerHaptic(HapticType.TICK))
        }
    }

    private fun handlePathPeekEnd() {
        _uiState.update { it.copy(activePeekingPath = null) }
    }

    private fun cancelPendingWork() {
        moveJob?.cancel()
        hintJob?.cancel()
        moveJob = null
        hintJob = null
    }

    private fun handleRestart() {
        if (_uiState.value.isLoading) return
        cancelPendingWork()
        val currentLevel = _uiState.value.level
        val initialState = GameState(level = currentLevel, buddies = currentLevel.buddies)
        persistGameState(initialState)
        val safeIds = computeSafeBuddyIds(initialState)

        _uiState.update {
            it.copy(
                gameState = initialState,
                isInputLocked = false,
                pressedBuddyId = null,
                activePeekingPath = null,
                showLevelCompleteDialog = false,
                showLevelFailedDialog = false,
                showPauseSheet = false,
                safeBuddyIds = safeIds,
                hintBuddyId = null
            )
        }
        viewModelScope.launch {
            _effects.emit(GameEffect.TriggerHaptic(HapticType.TICK))
        }
    }

    private fun handleNextLevel() {
        val currentId = _uiState.value.level.id
        val nextId = nextLevelIdFor(currentId)
        if (nextId == null) {
            viewModelScope.launch { _effects.emit(GameEffect.NavigateBack) }
            return
        }
        handleLoadLevel(nextId)
    }

    private fun nextLevelIdFor(currentId: String): String? {
        RESCUE_RUN_ID.matchEntire(currentId)?.let { match ->
            return "rescuerun-${match.groupValues[1].toInt() + 1}"
        }
        if (currentId.startsWith("daily")) return null
        if (LevelIdHelper.parseCampaignIndex(currentId) != null) {
            return LevelIdHelper.nextCampaignLevelId(currentId)
        }
        // Dev / custom levels: cycle through the dev pack (debug builds only)
        val available = levelRepository.getAvailableDevLevelIds()
        if (available.isEmpty()) return null
        val currentIndex = available.indexOf(currentId).coerceAtLeast(0)
        return available[(currentIndex + 1) % available.size]
    }

    private fun handleLoadLevel(levelId: String) {
        cancelPendingWork()
        loadJob?.cancel()
        savedStateHandle["levelId"] = levelId
        _uiState.update {
            it.copy(
                isLoading = true,
                isInputLocked = false,
                pressedBuddyId = null,
                activePeekingPath = null,
                showLevelCompleteDialog = false,
                showLevelFailedDialog = false,
                showPauseSheet = false,
                showLeaveLevelDialog = false,
                newSpeciesUnlocked = null,
                hintBuddyId = null
            )
        }
        loadJob = viewModelScope.launch {
            val nextLevel = levelRepository.loadLevelInBackground(levelId)
            val initialState = GameState(level = nextLevel, buddies = nextLevel.buddies)
            persistGameState(initialState)
            showLevel(nextLevel, initialState)
        }
    }

    /** Puts a loaded level on screen with [state] as the board, resetting per-level UI. */
    private fun showLevel(levelDef: LevelDef, state: GameState) {
        val safeIds = computeSafeBuddyIds(state)
        val isTutorial = isTutorialLevelId(levelDef.id)
        val isFirstTutorial = isTutorial && (levelDef.id == "w1-l01" || levelDef.id == "1")
        _uiState.update {
            it.copy(
                levelId = levelDef.id,
                level = levelDef,
                gameState = state,
                isLoading = false,
                isInputLocked = false,
                pressedBuddyId = null,
                activePeekingPath = null,
                safeBuddyIds = safeIds,
                isTutorialLevel = isTutorial,
                tutorialHandPos = if (isFirstTutorial) Pos(1, 1) else null,
                tutorialSpeechBubble = if (isFirstTutorial) R.string.ui_tutorial_tap_me else null,
                hintBuddyId = null,
                isBerriesDoubled = false
            )
        }
        checkMechanicIntro(levelDef)
    }

    /** Empty stand-in shown for the instant a level is loading. */
    private fun placeholderLevel(levelId: String): LevelDef {
        val flat = LevelIdHelper.parseCampaignIndex(levelId)
        return LevelDef(
            id = levelId,
            cols = 1,
            rows = 1,
            tiles = emptyMap(),
            buddies = emptyList(),
            world = flat?.let { (it - 1) / 30 + 1 } ?: 1,
            index = flat?.let { (it - 1) % 30 + 1 } ?: 1
        )
    }

    private fun handleDismissDialog() {
        _uiState.update {
            it.copy(
                showLevelCompleteDialog = false,
                showLevelFailedDialog = false,
                showPauseSheet = false,
                showLeaveLevelDialog = false,
                showBoosterShop = null
            )
        }
    }

    private fun handleDismissMechanicIntro() {
        val currentIntro = _uiState.value.activeMechanicIntro
        _uiState.update { it.copy(activeMechanicIntro = null) }
        if (currentIntro != null) {
            viewModelScope.launch {
                gameRepository.setMechanicSeen(currentIntro.id)
            }
        }
    }

    private fun checkMechanicIntro(level: LevelDef) {
        val candidate = when {
            level.tiles.values.any { it is Tile.Mud } -> MechanicType.MUD
            level.tiles.values.any { it is Tile.Turntable } -> MechanicType.TURNTABLE
            level.buddies.any { it.cageColor != null || it.keyColor != null } -> MechanicType.CAGE
            level.tiles.values.any { it is Tile.Portal } -> MechanicType.PORTAL
            level.buddies.any { it.sleepy } -> MechanicType.SLEEPY
            else -> null
        } ?: return

        viewModelScope.launch {
            val seen = runCatching { gameRepository.isMechanicSeen(candidate.id).first() }.getOrDefault(false)
            if (!seen) {
                _uiState.update { it.copy(activeMechanicIntro = candidate) }
            }
        }
    }

    private fun computeSafeBuddyIds(state: GameState): Set<String> {
        return GameEngine.safeMoves(state).map { it.id }.toSet()
    }

    private fun persistGameState(state: GameState) {
        runCatching {
            savedStateHandle[KEY_SAVED_GAME_STATE] = json.encodeToString(GameState.serializer(), state)
        }
    }

    private fun isTutorialLevelId(id: String): Boolean {
        return id in listOf("1", "w1-l01", "2", "w1-l02", "3", "w1-l03")
    }

    companion object {
        const val KEY_SAVED_GAME_STATE = "saved_game_state"
        private val RESCUE_RUN_ID = Regex("""rescuerun-(\d+)""")
    }
}
