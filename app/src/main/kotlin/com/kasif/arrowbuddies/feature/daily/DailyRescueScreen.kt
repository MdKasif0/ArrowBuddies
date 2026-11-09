package com.kasif.arrowbuddies.feature.daily

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesInfoCard
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesNavButton
import com.kasif.arrowbuddies.core.designsystem.component.ArrowBuddiesScreenScaffold

@Composable
fun DailyRescueScreen(
    uiState: DailyRescueUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    ArrowBuddiesScreenScaffold(
        title = stringResource(R.string.screen_daily),
        modifier = modifier
    ) {
        ArrowBuddiesInfoCard(
            text = "Daily Streak: ${uiState.streakDays} Days  •  Completed: ${if (uiState.isCompletedToday) "Yes" else "No"}"
        )

        ArrowBuddiesNavButton(
            label = stringResource(R.string.action_back),
            onClick = onNavigateBack
        )
    }
}

