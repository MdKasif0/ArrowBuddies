package com.kasif.arrowbuddies.feature.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.designsystem.component.CandyButton
import com.kasif.arrowbuddies.core.designsystem.component.CandyButtonVariant
import com.kasif.arrowbuddies.core.designsystem.component.GameDialog
import com.kasif.arrowbuddies.core.designsystem.component.StarRow
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens

@Composable
fun LevelCompleteDialog(
    stars: Int,
    onNextLevel: () -> Unit,
    onRestart: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val colors = ArrowBuddiesThemeTokens.colors

    GameDialog(
        title = stringResource(R.string.game_level_complete),
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StarRow(
                filledStars = stars.coerceIn(1, 3),
                totalStars = 3
            )

            Text(
                text = stringResource(R.string.game_level_complete_subtitle),
                style = typography.body,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            CandyButton(
                label = stringResource(R.string.game_next_level),
                onClick = onNextLevel,
                variant = CandyButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )

            CandyButton(
                label = stringResource(R.string.game_restart),
                onClick = onRestart,
                variant = CandyButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LevelFailedDialog(
    onRestart: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val colors = ArrowBuddiesThemeTokens.colors

    GameDialog(
        title = stringResource(R.string.game_level_failed),
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.game_level_failed_subtitle),
                style = typography.body,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            CandyButton(
                label = stringResource(R.string.game_try_again),
                onClick = onRestart,
                variant = CandyButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
