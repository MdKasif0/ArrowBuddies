package com.kasif.arrowbuddies.feature.album

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesInfoCard
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesNavButton
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesScreenScaffold

@Composable
fun AlbumScreen(
    uiState: AlbumUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    ArrowBuddiesScreenScaffold(
        title = stringResource(R.string.screen_album),
        modifier = modifier
    ) {
        ArrowBuddiesInfoCard(
            text = "Discovered: ${uiState.discoveredSpeciesCount} of ${uiState.totalSpeciesCount} Buddies"
        )

        ArrowBuddiesNavButton(
            label = stringResource(R.string.action_back),
            onClick = onNavigateBack
        )
    }
}

