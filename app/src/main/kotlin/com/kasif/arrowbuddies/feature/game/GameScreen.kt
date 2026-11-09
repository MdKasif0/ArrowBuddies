package com.kasif.arrowbuddies.feature.game

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesInfoCard
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesNavButton
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesScreenScaffold

@Composable
fun GameScreen(
    uiState: GameUiState,
    onNavigateNextLevel: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Immersive mode & Keep Screen On during gameplay only
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

    ArrowBuddiesScreenScaffold(
        title = stringResource(R.string.screen_game, uiState.levelId),
        modifier = modifier
    ) {
        ArrowBuddiesInfoCard(
            text = "Hearts: ${uiState.heartsRemaining}/3  •  Rescued: ${uiState.rescuedCount}/${uiState.totalBuddiesInLevel}"
        )

        ArrowBuddiesInfoCard(
            text = stringResource(R.string.placeholder_game_instruction)
        )

        ArrowBuddiesNavButton(
            label = stringResource(R.string.action_next_level),
            onClick = {
                val next = (uiState.levelId.toIntOrNull() ?: 1) + 1
                onNavigateNextLevel(next.toString())
            },
            isPrimary = true
        )

        ArrowBuddiesNavButton(
            label = stringResource(R.string.action_back),
            onClick = onNavigateBack
        )
    }
}

