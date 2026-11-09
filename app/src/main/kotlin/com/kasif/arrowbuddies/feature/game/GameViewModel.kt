package com.kasif.arrowbuddies.feature.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.data.repository.GameRepository
import com.kasif.arrowbuddies.data.repository.LevelRepository
import com.kasif.arrowbuddies.engine.GameEngine
import com.kasif.arrowbuddies.engine.model.GameState
import com.kasif.arrowbuddies.engine.model.GameStatus
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.MoveResult
import com.kasif.arrowbuddies.engine.model.RefusalReason
import com.kasif.arrowbuddies.engine.model.Tile
import com.kasif.arrowbuddies.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val levelRepository: LevelRepository,
    private val gameRepository: GameRepository,
    private val savedStateHandle: SavedStateHandle,
    private val hapticsManager: HapticsManager,
    private val soundManager: SoundManager
) : ViewModel() {

    private val json = Json {
        ignoreUnknownKeys = true
        allowStructuredMapKeys = true
    }

    private val route = runCatching { savedStateHandle.toRoute<Route.Game>() }.getOrNull()
    private val initialLevelId = route?.levelId
        ?: savedStateHandle.get<String>("levelId")
        ?: "1"

    private val _effects = MutableSharedFlow<GameEffect>(extraBufferCapacity = 16)
    val effects: SharedFlow<GameEffect> = _effects.asSharedFlow()

    private val _uiState: MutableStateFlow<GameUiState>

    init {
        val restoredStateJson = savedStateHandle.get<String>(KEY_SAVED_GAME_STATE)
        val restoredState = restoredStateJson?.let {
            runCatching { json.decodeFromString(GameState.serializer(), it) }.getOrNull()
        }

        val levelDef: LevelDef
        val gameState: GameState

        if (restoredState != null) {
            levelDef = restoredState.level
            gameState = restoredState
        } else {
            levelDef = levelRepository.loadLevel(initialLevelId)
            gameState = GameState(level = levelDef, buddies = levelDef.buddies)
        }

        val safeBuddyIds = computeSafeBuddyIds(gameState)
        _uiState = MutableStateFlow(
            GameUiState(
                levelId = levelDef.id,
                level = levelDef,
                gameState = gameState,
                safeBuddyIds = safeBuddyIds
            )
        )

        checkMechanicIntro(levelDef)
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
                // Lock input during escape animation (55 ms per cell, max 450 ms)
                val dashMs = (result.path.size * 55L).coerceAtMost(450L)
                val totalAnimMs = 70L + dashMs + 320L

                _uiState.update { it.copy(isInputLocked = true, pressedBuddyId = null, activePeekingPath = null) }

                viewModelScope.launch {
                    _effects.emit(GameEffect.PlayEscape(buddyId, result.path, result.exitSide, buddy))
                    _effects.emit(GameEffect.PlaySound(SoundType.ESCAPE))

                    // Key Carrier Escaped -> Unlock matching cages!
                    if (buddy.keyColor != null) {
                        val matchingCages = currentState.gameState.buddies.filter { it.cageColor == buddy.keyColor }
                        if (matchingCages.isNotEmpty()) {
                            _effects.emit(
                                GameEffect.PlayKeyUnlock(
                                    keyColor = buddy.keyColor,
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
                        _effects.emit(GameEffect.PlaySound(SoundType.WIN))
                        delay(500L)
                        _uiState.update { it.copy(showLevelCompleteDialog = true) }
                    }
                }
            }

            is MoveResult.Bonked -> {
                val dashMs = (result.path.size * 45L).coerceAtMost(280L)

                _uiState.update { it.copy(isInputLocked = true, pressedBuddyId = null, activePeekingPath = null) }

                viewModelScope.launch {
                    _effects.emit(GameEffect.PlayBonk(buddyId, result.path, result.blockerPos, result.blockerKind, buddy))
                    _effects.emit(GameEffect.PlaySound(SoundType.BONK))

                    delay(dashMs)
                    // Impact! Soft screen shake & reject haptic
                    _effects.emit(GameEffect.ScreenShake(intensityDp = 4f, durationMs = 180L))
                    _effects.emit(GameEffect.TriggerHaptic(HapticType.REJECT))

                    delay(260L) // Star orbit and bounce back
                    persistGameState(outcome.newState)
                    val safeIds = computeSafeBuddyIds(outcome.newState)
                    _uiState.update {
                        it.copy(
                            gameState = outcome.newState,
                            isInputLocked = false,
                            safeBuddyIds = safeIds
                        )
                    }

                    if (outcome.newState.status == GameStatus.LOST) {
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

                viewModelScope.launch {
                    _effects.emit(GameEffect.PlayMudStop(buddyId, result.path, targetPos, buddy))
                    _effects.emit(GameEffect.PlaySound(SoundType.SQUELCH))

                    // Sleepy buddies waking up if neighbor vacated
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

    private fun handleUndo() {
        val currentState = _uiState.value
        if (currentState.isInputLocked || currentState.gameState.status != GameStatus.PLAYING) return

        val restored = GameEngine.undo(currentState.gameState) ?: return
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
        viewModelScope.launch {
            _effects.emit(GameEffect.TriggerHaptic(HapticType.TICK))
        }
    }

    private fun handleRestart() {
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
                safeBuddyIds = safeIds
            )
        }
        viewModelScope.launch {
            _effects.emit(GameEffect.TriggerHaptic(HapticType.TICK))
        }
    }

    private fun handleNextLevel() {
        val available = levelRepository.getAvailableDevLevelIds()
        val currentIndex = available.indexOf(_uiState.value.level.id).coerceAtLeast(0)
        val nextId = available[(currentIndex + 1) % available.size]
        handleLoadLevel(nextId)
    }

    private fun handleLoadLevel(levelId: String) {
        val nextLevel = levelRepository.loadLevel(levelId)
        val initialState = GameState(level = nextLevel, buddies = nextLevel.buddies)
        persistGameState(initialState)
        savedStateHandle["levelId"] = levelId
        val safeIds = computeSafeBuddyIds(initialState)

        _uiState.update {
            it.copy(
                levelId = nextLevel.id,
                level = nextLevel,
                gameState = initialState,
                isInputLocked = false,
                pressedBuddyId = null,
                activePeekingPath = null,
                showLevelCompleteDialog = false,
                showLevelFailedDialog = false,
                safeBuddyIds = safeIds
            )
        }
        checkMechanicIntro(nextLevel)
    }

    private fun handleDismissDialog() {
        _uiState.update {
            it.copy(
                showLevelCompleteDialog = false,
                showLevelFailedDialog = false
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

    companion object {
        const val KEY_SAVED_GAME_STATE = "saved_game_state"
    }
}
