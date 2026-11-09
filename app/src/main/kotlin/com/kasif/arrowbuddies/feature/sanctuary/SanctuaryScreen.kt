package com.kasif.arrowbuddies.feature.sanctuary

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesInfoCard
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesNavButton
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesScreenScaffold

@Composable
fun SanctuaryScreen(
    uiState: SanctuaryUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    ArrowBuddiesScreenScaffold(
        title = stringResource(R.string.screen_sanctuary),
        modifier = modifier
    ) {
        ArrowBuddiesInfoCard(
            text = "Active Zone: ${uiState.activeZoneName} (Zones Unlocked: ${uiState.unlockedZonesCount}/8)"
        )

        ArrowBuddiesNavButton(
            label = stringResource(R.string.action_back),
            onClick = onNavigateBack
        )
    }
}

