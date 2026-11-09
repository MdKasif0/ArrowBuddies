package com.kasif.arrowbuddies.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesNavButton
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesScreenScaffold

@Composable
fun SplashScreen(
    uiState: SplashUiState,
    onNavigateToGame: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    ArrowBuddiesScreenScaffold(
        title = stringResource(R.string.screen_splash),
        modifier = modifier
    ) {
        Image(
            painter = painterResource(R.drawable.ic_splash_icon),
            contentDescription = stringResource(R.string.desc_splash_logo),
            modifier = Modifier.size(120.dp)
        )

        ArrowBuddiesNavButton(
            label = stringResource(R.string.action_play),
            onClick = { onNavigateToGame(uiState.initialLevelId) },
            isPrimary = true
        )

        ArrowBuddiesNavButton(
            label = stringResource(R.string.screen_home),
            onClick = onNavigateToHome
        )
    }
}

