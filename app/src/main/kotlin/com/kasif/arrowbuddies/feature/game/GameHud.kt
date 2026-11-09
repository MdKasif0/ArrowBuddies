package com.kasif.arrowbuddies.feature.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.designsystem.component.CandyButtonVariant
import com.kasif.arrowbuddies.core.designsystem.component.HeartsRow
import com.kasif.arrowbuddies.core.designsystem.component.RoundIconButton
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.designsystem.theme.Mango
import com.kasif.arrowbuddies.core.designsystem.theme.Mint
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure

@Composable
fun GameTopBar(
    hearts: Int,
    rescuedCount: Int,
    totalBuddies: Int,
    levelName: String,
    onOpenPause: () -> Unit,
    modifier: Modifier = Modifier,
    bumpRescuedTrigger: Int = 0
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val colors = ArrowBuddiesThemeTokens.colors

    // Rescued pill bump animation
    val rescuedScale = remember { Animatable(1f) }
    LaunchedEffect(bumpRescuedTrigger) {
        if (bumpRescuedTrigger > 0) {
            rescuedScale.snapTo(1.3f)
            rescuedScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium)
            )
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pause Button
        RoundIconButton(
            icon = Icons.Default.Menu,
            onClick = onOpenPause,
            contentDescription = stringResource(R.string.game_paused),
            size = 46.dp
        )

        // Level Title Pill ("Level 12")
        Box(
            modifier = Modifier
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
                .background(WhitePure, RoundedCornerShape(16.dp))
                .border(1.5.dp, Color(0xFFE2D6C0), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = levelName,
                style = typography.title.copy(fontSize = 16.sp),
                color = colors.textPrimary
            )
        }

        // Stats: Rescued counter & Hearts
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rescued counter pill with tiny face emoji
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = rescuedScale.value
                        scaleY = rescuedScale.value
                    }
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp))
                    .background(Color(0xFFE8F8F2), RoundedCornerShape(14.dp))
                    .border(1.5.dp, Mint.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🐾",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "$rescuedCount/$totalBuddies",
                        style = typography.caption.copy(fontSize = 14.sp),
                        color = colors.textPrimary
                    )
                }
            }

            // Hearts Row
            HeartsRow(
                heartsRemaining = hearts,
                totalHearts = 3,
                heartSize = 26.dp
            )
        }
    }
}

@Composable
fun GameBottomControls(
    canUndo: Boolean,
    hintsCount: Int,
    undosCount: Int,
    onHint: () -> Unit,
    onUndo: () -> Unit,
    onRestart: () -> Unit,
    onSelectDevLevel: (String) -> Unit,
    currentLevelId: String,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val colors = ArrowBuddiesThemeTokens.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Main In-Game Boosters & Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hint Button with badge count
            RoundIconButton(
                icon = Icons.Default.Info,
                onClick = onHint,
                variant = CandyButtonVariant.Primary,
                badgeCount = hintsCount,
                contentDescription = stringResource(R.string.booster_hint),
                size = 54.dp
            )

            Spacer(modifier = Modifier.width(28.dp))

            // Undo Button with badge count
            RoundIconButton(
                icon = Icons.Default.Refresh,
                onClick = onUndo,
                enabled = true, // tapping at 0 opens purchase sheet
                variant = CandyButtonVariant.Secondary,
                badgeCount = undosCount,
                contentDescription = stringResource(R.string.game_undo),
                size = 54.dp
            )

            Spacer(modifier = Modifier.width(28.dp))

            // Restart Button
            RoundIconButton(
                icon = Icons.Default.Refresh,
                onClick = onRestart,
                variant = CandyButtonVariant.Danger,
                contentDescription = stringResource(R.string.game_restart),
                size = 54.dp
            )
        }
    }
}
