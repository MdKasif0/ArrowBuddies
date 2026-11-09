package com.kasif.arrowbuddies.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesInfoCard
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesNavButton
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesScreenScaffold

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    ArrowBuddiesScreenScaffold(
        title = stringResource(R.string.screen_settings),
        modifier = modifier
    ) {
        ArrowBuddiesInfoCard(
            text = "Sound: ${if (uiState.soundEnabled) "ON" else "OFF"}  •  Music: ${if (uiState.musicEnabled) "ON" else "OFF"}  •  Haptics: ${if (uiState.hapticsEnabled) "ON" else "OFF"}"
        )

        ArrowBuddiesNavButton(
            label = "Toggle Sound (${if (uiState.soundEnabled) "ON" else "OFF"})",
            onClick = { onEvent(SettingsUiEvent.ToggleSound(!uiState.soundEnabled)) }
        )

        ArrowBuddiesNavButton(
            label = "Toggle Music (${if (uiState.musicEnabled) "ON" else "OFF"})",
            onClick = { onEvent(SettingsUiEvent.ToggleMusic(!uiState.musicEnabled)) }
        )

        ArrowBuddiesNavButton(
            label = "Toggle Haptics (${if (uiState.hapticsEnabled) "ON" else "OFF"})",
            onClick = { onEvent(SettingsUiEvent.ToggleHaptics(!uiState.hapticsEnabled)) }
        )

        ArrowBuddiesNavButton(
            label = stringResource(R.string.action_back),
            onClick = onNavigateBack
        )
    }
}

