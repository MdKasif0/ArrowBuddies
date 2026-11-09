package com.kasif.arrowbuddies.feature.game

import android.app.Activity
import android.view.WindowManager
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
            com.kasif.arrowbuddies.core.designsystem.theme.worldThemeFor(uiState.level.world)
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
            // Top HUD Bar (Hearts, Rescued counter, Level title, Back)
            GameTopBar(
                hearts = uiState.heartsRemaining,
                rescuedCount = uiState.rescuedCount,
                totalBuddies = uiState.totalBuddiesInLevel,
                levelName = stringResource(R.string.screen_game, uiState.level.index),
                onNavigateBack = onNavigateBack,
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
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom Controls (Undo, Restart, Dev level switcher)
            GameBottomControls(
                canUndo = uiState.canUndo,
                onUndo = { onEvent(GameUiEvent.Undo) },
                onRestart = { onEvent(GameUiEvent.RestartLevel) },
                onSelectDevLevel = { levelId -> onEvent(GameUiEvent.LoadLevel(levelId)) },
                currentLevelId = uiState.levelId
            )
        }

        // Level Complete Dialog
        if (uiState.showLevelCompleteDialog) {
            LevelCompleteDialog(
                stars = uiState.starsEarned,
                onNextLevel = {
                    onEvent(GameUiEvent.DismissDialog)
                    onEvent(GameUiEvent.NextLevel)
                },
                onRestart = {
                    onEvent(GameUiEvent.DismissDialog)
                    onEvent(GameUiEvent.RestartLevel)
                },
                onDismiss = { onEvent(GameUiEvent.DismissDialog) }
            )
        }

        // Level Failed Dialog
        if (uiState.showLevelFailedDialog) {
            LevelFailedDialog(
                onRestart = {
                    onEvent(GameUiEvent.DismissDialog)
                    onEvent(GameUiEvent.RestartLevel)
                },
                onDismiss = { onEvent(GameUiEvent.DismissDialog) }
            )
        }
    }
}
