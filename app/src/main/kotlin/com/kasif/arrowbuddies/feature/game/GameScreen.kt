package com.kasif.arrowbuddies.feature.game

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.designsystem.component.AnimatedSkyBackground
import com.kasif.arrowbuddies.core.designsystem.component.ParticleSystemState
import com.kasif.arrowbuddies.core.designsystem.modifier.LocalHaptics
import com.kasif.arrowbuddies.core.designsystem.modifier.LocalSound
import com.kasif.arrowbuddies.core.designsystem.theme.worldThemeFor
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GameScreen(
    uiState: GameUiState,
    effects: SharedFlow<GameEffect>,
    onEvent: (GameUiEvent) -> Unit,
    onNavigateNextLevel: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = context as? Activity
    val hapticsManager: HapticsManager? = LocalHaptics.current
    val soundManager: SoundManager? = LocalSound.current

    val particleSystemState = remember { ParticleSystemState() }
    var bumpRescuedCount by remember { mutableIntStateOf(0) }

    // Intercept back button to prompt "Leave this level?"
    BackHandler {
        onEvent(GameUiEvent.RequestLeaveLevel)
    }

    // Immersive mode & Keep Screen On during gameplay
    DisposableEffect(activity) {
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val insetsController = window?.let {
            WindowCompat.getInsetsController(it, it.decorView).apply {
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.systemBars())
            }
        }

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Handle Haptics, Audio hooks, and Confetti from one-shot effects
    LaunchedEffect(effects) {
        effects.collectLatest { effect ->
            when (effect) {
                is GameEffect.TriggerHaptic -> {
                    when (effect.type) {
                        HapticType.TICK -> hapticsManager?.tick(view)
                        HapticType.CONFIRM -> hapticsManager?.confirm(view)
                        HapticType.REJECT -> hapticsManager?.reject(view)
                    }
                }

                is GameEffect.PlaySound -> {
                    when (effect.type) {
                        SoundType.TAP -> soundManager?.playPop()
                        SoundType.ESCAPE -> soundManager?.playChime()
                        SoundType.BONK -> soundManager?.playBoing()
                        SoundType.SQUELCH -> soundManager?.playPop()
                        SoundType.WHOOSH -> soundManager?.playChime()
                        SoundType.KEY_UNLOCK -> soundManager?.playChime()
                        SoundType.CAGE_LOCKED -> soundManager?.playBoing()
                        SoundType.SLEEPY_YAWN -> soundManager?.playPop()
                        SoundType.WAKE_UP -> soundManager?.playPop()
                        SoundType.WIN -> {
                            soundManager?.playChime()
                            // Trigger victory confetti burst
                            val screenWidth = view.width.toFloat().coerceAtLeast(400f)
                            val screenHeight = view.height.toFloat().coerceAtLeast(600f)
                            particleSystemState.spawnConfetti(screenWidth * 0.5f, screenHeight * 0.35f, count = 100)
                        }
                        SoundType.LOSE -> soundManager?.playBoing()
                    }
                }

                is GameEffect.PlayEscape -> {
                    // Escape sequence started
                }

                else -> Unit
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Living animated sky backdrop for the current world biome
        val worldTheme = remember(uiState.level.world) {
            worldThemeFor(uiState.level.world)
        }
        AnimatedSkyBackground(
            worldTheme = worldTheme,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top HUD Bar (Hearts, Rescued counter, Level title, Pause button)
            GameTopBar(
                hearts = uiState.heartsRemaining,
                rescuedCount = uiState.rescuedCount,
                totalBuddies = uiState.totalBuddiesInLevel,
                levelName = stringResource(R.string.screen_game, uiState.level.index),
                onOpenPause = { onEvent(GameUiEvent.OpenPauseSheet) },
                bumpRescuedTrigger = bumpRescuedCount
            )

            // Playable 3D Board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                GameBoard(
                    level = uiState.level,
                    buddies = uiState.gameState.buddies,
                    safeBuddyIds = uiState.safeBuddyIds,
                    isInputLocked = uiState.isInputLocked,
                    pressedBuddyId = uiState.pressedBuddyId,
                    activePeekingPath = uiState.activePeekingPath,
                    effects = effects,
                    onBuddyTap = { buddyId -> onEvent(GameUiEvent.TapBuddy(buddyId)) },
                    onBuddyPressed = { buddyId -> onEvent(GameUiEvent.PressBuddy(buddyId)) },
                    onBuddyReleased = { onEvent(GameUiEvent.ReleaseBuddy) },
                    onPathPeekStart = { buddyId -> onEvent(GameUiEvent.StartPathPeek(buddyId)) },
                    onPathPeekEnd = { onEvent(GameUiEvent.EndPathPeek) },
                    onRescuedBump = { bumpRescuedCount++ },
                    particleSystemState = particleSystemState,
                    hintBuddyId = uiState.hintBuddyId,
                    tutorialHandPos = uiState.tutorialHandPos,
                    tutorialSpeechBubble = uiState.tutorialSpeechBubble,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom Controls (Hint, Undo, Restart)
            GameBottomControls(
                canUndo = uiState.canUndo,
                hintsCount = uiState.hintsAvailable,
                undosCount = uiState.undosAvailable,
                onHint = { onEvent(GameUiEvent.RequestHint) },
                onUndo = { onEvent(GameUiEvent.Undo) },
                onRestart = { onEvent(GameUiEvent.RestartLevel) },
                onSelectDevLevel = { levelId -> onEvent(GameUiEvent.LoadLevel(levelId)) },
                currentLevelId = uiState.levelId
            )
        }

        // Level Complete Celebration Sheet
        if (uiState.showLevelCompleteDialog) {
            LevelWinSheet(
                stars = uiState.starsEarned,
                rescuedBuddies = uiState.level.buddies,
                goldenBonus = uiState.goldenBerryBonus,
                newSpecies = uiState.newSpeciesUnlocked,
                onNextLevel = {
                    onEvent(GameUiEvent.DismissDialog)
                    onEvent(GameUiEvent.NextLevel)
                },
                onRestart = {
                    onEvent(GameUiEvent.DismissDialog)
                    onEvent(GameUiEvent.RestartLevel)
                },
                onNavigateToMap = {
                    onEvent(GameUiEvent.DismissDialog)
                    onNavigateBack()
                }
            )
        }

        // Level Failed Sheet (Gentle, non-shaming)
        if (uiState.showLevelFailedDialog) {
            LevelFailSheet(
                berries = uiState.berries,
                onRestart = {
                    onEvent(GameUiEvent.DismissDialog)
                    onEvent(GameUiEvent.RestartLevel)
                },
                onReviveWithHeart = {
                    onEvent(GameUiEvent.ReviveWithHeart)
                },
                onNavigateToMap = {
                    onEvent(GameUiEvent.DismissDialog)
                    onNavigateBack()
                }
            )
        }

        // In-game Pause Sheet
        if (uiState.showPauseSheet) {
            PauseSheet(
                soundEnabled = uiState.soundEnabled,
                musicEnabled = uiState.musicEnabled,
                hapticsEnabled = uiState.hapticsEnabled,
                onResume = { onEvent(GameUiEvent.DismissPauseSheet) },
                onRestart = {
                    onEvent(GameUiEvent.DismissPauseSheet)
                    onEvent(GameUiEvent.RestartLevel)
                },
                onNavigateToMap = {
                    onEvent(GameUiEvent.DismissPauseSheet)
                    onNavigateBack()
                },
                onToggleSound = { onEvent(GameUiEvent.ToggleSound(it)) },
                onToggleMusic = { onEvent(GameUiEvent.ToggleMusic(it)) },
                onToggleHaptics = { onEvent(GameUiEvent.ToggleHaptics(it)) }
            )
        }

        // Leave Level Confirmation Dialog
        if (uiState.showLeaveLevelDialog) {
            LeaveLevelDialog(
                onStay = { onEvent(GameUiEvent.DismissLeaveLevel) },
                onLeave = {
                    onEvent(GameUiEvent.DismissLeaveLevel)
                    onNavigateBack()
                }
            )
        }

        // Booster Purchase Modal
        if (uiState.showBoosterShop != null) {
            BoosterShopDialog(
                type = uiState.showBoosterShop,
                berries = uiState.berries,
                onBuy = { type -> onEvent(GameUiEvent.BuyBooster(type)) },
                onDismiss = { onEvent(GameUiEvent.DismissBoosterShop) }
            )
        }

        // First-time Mechanic Introduction Dialog
        if (uiState.activeMechanicIntro != null) {
            MechanicIntroDialog(
                mechanic = uiState.activeMechanicIntro,
                onDismiss = { onEvent(GameUiEvent.DismissMechanicIntro) }
            )
        }

        // Daily Reminder Opt-in Dialog (Level 5+)
        if (uiState.showReminderPromptDialog) {
            DailyReminderOptInDialog(
                onAccept = { onEvent(GameUiEvent.AcceptDailyReminder) },
                onDismiss = { onEvent(GameUiEvent.DismissDailyReminder) }
            )
        }
    }
}
