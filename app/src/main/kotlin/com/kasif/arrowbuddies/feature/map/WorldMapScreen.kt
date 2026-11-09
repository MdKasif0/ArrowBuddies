package com.kasif.arrowbuddies.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesInfoCard
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesNavButton
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesScreenScaffold

@Composable
fun WorldMapScreen(
    uiState: WorldMapUiState,
    onNavigateToGame: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    ArrowBuddiesScreenScaffold(
        title = stringResource(R.string.screen_map),
        modifier = modifier
    ) {
        ArrowBuddiesInfoCard(
            text = "World ${uiState.selectedWorldId}: Sunny Meadow (Unlocked: ${uiState.unlockedWorldCount}/8)"
        )

        ArrowBuddiesNavButton(
            label = "Enter Level 1",
            onClick = { onNavigateToGame("1") },
            isPrimary = true
        )

        ArrowBuddiesNavButton(
            label = "Enter Level 2",
            onClick = { onNavigateToGame("2") }
        )

        ArrowBuddiesNavButton(
            label = stringResource(R.string.action_back),
            onClick = onNavigateBack
        )
    }
}

