package com.kasif.arrowbuddies.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesInfoCard
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesNavButton
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesScreenScaffold

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNavigateToGame: (String) -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToSanctuary: () -> Unit,
    onNavigateToAlbum: () -> Unit,
    onNavigateToDaily: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    ArrowBuddiesScreenScaffold(
        title = stringResource(R.string.screen_home),
        modifier = modifier
    ) {
        ArrowBuddiesInfoCard(
            text = "Total Buddies Rescued: ${uiState.totalRescued}"
        )

        ArrowBuddiesNavButton(
            label = stringResource(R.string.action_play),
            onClick = { onNavigateToGame(uiState.currentLevelId) },
            isPrimary = true
        )

        ArrowBuddiesNavButton(
            label = stringResource(R.string.screen_map),
            onClick = onNavigateToMap
        )

        ArrowBuddiesNavButton(
            label = stringResource(R.string.screen_sanctuary),
            onClick = onNavigateToSanctuary
        )

        ArrowBuddiesNavButton(
            label = stringResource(R.string.screen_album),
            onClick = onNavigateToAlbum
        )

        ArrowBuddiesNavButton(
            label = stringResource(R.string.screen_daily),
            onClick = onNavigateToDaily
        )

        ArrowBuddiesNavButton(
            label = stringResource(R.string.screen_settings),
            onClick = onNavigateToSettings
        )
    }
}

