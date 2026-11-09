package com.kasif.arrowbuddies.feature.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    onNavigateBack: () -> Unit,
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
        // Back Button
        RoundIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onClick = onNavigateBack,
            contentDescription = stringResource(R.string.action_back),
            size = 46.dp
        )

        // Level Title Pill
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
            // Rescued counter pill
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
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Mango,
                        modifier = Modifier.size(16.dp)
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Dev Level Quick Switcher Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val devLevels = listOf(
                "w1-l01-dev" to "1",
                "w1-l02-dev" to "2",
                "w1-l03-dev" to "3"
            )
            for ((id, num) in devLevels) {
                val isSelected = currentLevelId == id || currentLevelId == num
                val chipBg = if (isSelected) Mango else WhitePure
                val chipText = if (isSelected) WhitePure else colors.textPrimary

                Box(
                    modifier = Modifier
                        .shadow(elevation = if (isSelected) 3.dp else 1.dp, shape = RoundedCornerShape(12.dp))
                        .background(chipBg, RoundedCornerShape(12.dp))
                        .border(1.dp, if (isSelected) Mango else Color(0xFFDDD2BC), RoundedCornerShape(12.dp))
                        .clickable { onSelectDevLevel(id) }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.game_dev_level_chip, num),
                        style = typography.caption.copy(fontSize = 13.sp),
                        color = chipText
                    )
                }
            }
        }

        // Action Buttons: Undo & Restart
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo Button
            RoundIconButton(
                icon = Icons.Default.Refresh,
                onClick = onUndo,
                enabled = canUndo,
                variant = CandyButtonVariant.Secondary,
                contentDescription = stringResource(R.string.game_undo),
                size = 50.dp
            )

            Spacer(modifier = Modifier.width(28.dp))

            // Restart Button
            RoundIconButton(
                icon = Icons.Default.Refresh,
                onClick = onRestart,
                variant = CandyButtonVariant.Danger,
                contentDescription = stringResource(R.string.game_restart),
                size = 50.dp
            )
        }
    }
}
