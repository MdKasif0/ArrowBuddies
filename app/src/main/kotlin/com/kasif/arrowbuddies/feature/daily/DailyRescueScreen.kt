package com.kasif.arrowbuddies.feature.daily

import androidx.compose.ui.res.pluralStringResource
import com.kasif.arrowbuddies.core.economy.EconomyConfig
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.designsystem.component.CandyButton
import com.kasif.arrowbuddies.core.designsystem.component.CandyButtonVariant
import com.kasif.arrowbuddies.core.designsystem.component.CounterPill
import com.kasif.arrowbuddies.core.designsystem.component.RoundIconButton
import com.kasif.arrowbuddies.core.designsystem.component.StarRow
import com.kasif.arrowbuddies.core.designsystem.component.ToyPanel
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.designsystem.theme.Coral
import com.kasif.arrowbuddies.core.designsystem.theme.Cream
import com.kasif.arrowbuddies.core.designsystem.theme.Grape
import com.kasif.arrowbuddies.core.designsystem.theme.Ink
import com.kasif.arrowbuddies.core.designsystem.theme.Mango
import com.kasif.arrowbuddies.core.designsystem.theme.Mint
import com.kasif.arrowbuddies.core.designsystem.theme.Sky
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure
import com.kasif.arrowbuddies.core.designsystem.theme.worldThemeFor

@Composable
fun DailyRescueScreen(
    uiState: DailyRescueUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onPlayDailyLevel: (String) -> Unit = {},
    onEvent: (DailyRescueUiEvent) -> Unit = {}
) {
    val shareText = pluralStringResource(R.plurals.daily_share_text, uiState.todayMistakes, uiState.todayMistakes)
    val typography = ArrowBuddiesThemeTokens.typography
    val context = LocalContext.current
    val worldTheme = worldThemeFor(uiState.worldThemeIndex)

    val infiniteTransition = rememberInfiniteTransition(label = "dailyPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F6F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ----------------------------------------------------
            // HEADER BAR
            // ----------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onNavigateBack,
                    contentDescription = stringResource(R.string.action_back),
                    size = 46.dp
                )

                Box(
                    modifier = Modifier
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(18.dp))
                        .background(WhitePure, RoundedCornerShape(18.dp))
                        .border(1.5.dp, Color(0xFFE2D6C0), RoundedCornerShape(18.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.screen_daily),
                        style = typography.title.copy(fontSize = 18.sp),
                        color = Ink
                    )
                }

                CounterPill(
                    count = uiState.berries,
                    icon = Icons.Default.Star,
                    iconTint = Mango
                )
            }

            // ----------------------------------------------------
            // SCROLLABLE BODY
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ----------------------------------------------------
                // 7-DAY STREAK CALENDAR CARD
                // ----------------------------------------------------
                ToyPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "🔥", fontSize = 20.sp)
                                Text(
                                    text = pluralStringResource(R.plurals.ui_daily_streak, uiState.streakDays, uiState.streakDays),
                                    style = typography.title.copy(fontSize = 17.sp),
                                    color = Ink
                                )
                            }
                            Text(
                                text = stringResource(R.string.ui_daily_loop),
                                style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = Ink.copy(alpha = 0.5f)
                            )
                        }

                        // Horizontal Row of 7 Days
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (day in uiState.streakRewards) {
                                StreakDayCard(reward = day)
                            }
                        }
                    }
                }

                // ----------------------------------------------------
                // TODAY'S PUZZLE HERO CARD
                // ----------------------------------------------------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(worldTheme.skyTop, worldTheme.skyBottom)
                            )
                        )
                        .border(2.dp, Color(0xFFE2D6C0), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.ui_daily_today_puzzle),
                            style = typography.title.copy(fontSize = 22.sp),
                            color = Ink,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = uiState.todayDateFormatted,
                            style = typography.body.copy(fontSize = 14.sp),
                            color = Ink.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )

                        // Badges Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(WhitePure.copy(alpha = 0.90f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.ui_daily_world_biome, uiState.worldThemeIndex),
                                    style = typography.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                    color = Ink
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Mango)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.ui_daily_reward, EconomyConfig.BERRIES_DAILY_RESCUE),
                                    style = typography.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                    color = Ink
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Completion or Play Button
                        if (uiState.isCompletedToday) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StarRow(
                                    starsEarned = uiState.todayStars,
                                    starSize = 34.dp
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Mint)
                                        .border(1.5.dp, Ink, RoundedCornerShape(14.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = pluralStringResource(R.plurals.ui_daily_completed, uiState.todayMistakes, uiState.todayMistakes),
                                        style = typography.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                        color = Ink
                                    )
                                }

                                // Share Result Button
                                CandyButton(
                                    text = stringResource(R.string.ui_daily_share),
                                    onClick = {
                                        onEvent(DailyRescueUiEvent.ShareResult)
                                        shareResult(context, shareText)
                                    },
                                    variant = CandyButtonVariant.Success,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            scaleX = pulseScale
                                            scaleY = pulseScale
                                        }
                                )

                                // Replay without pressure
                                CandyButton(
                                    text = stringResource(R.string.ui_daily_play_again),
                                    onClick = { onPlayDailyLevel(uiState.dailyLevelId) },
                                    variant = CandyButtonVariant.Ghost,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            CandyButton(
                                text = stringResource(R.string.ui_daily_play_today, EconomyConfig.BERRIES_DAILY_RESCUE),
                                onClick = {
                                    onEvent(DailyRescueUiEvent.PlayDailyRescue)
                                    onPlayDailyLevel(uiState.dailyLevelId)
                                },
                                variant = CandyButtonVariant.Primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 7-Day Streak Calendar Day Card
 */
@Composable
private fun StreakDayCard(
    reward: StreakDayReward,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography

    val bgColor = when {
        reward.isClaimed -> Mint
        reward.isCurrent -> WhitePure
        else -> Color(0xFFF7F3EC)
    }

    val borderColor = when {
        reward.isCurrent -> Coral
        reward.isClaimed -> Ink
        else -> Color(0xFFE2D6C0)
    }

    Box(
        modifier = modifier
            .width(66.dp)
            .shadow(elevation = if (reward.isCurrent) 4.dp else 1.dp, shape = RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(width = if (reward.isCurrent) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.ui_daily_day, reward.dayNumber),
                style = typography.caption.copy(
                    fontSize = 11.sp,
                    fontWeight = if (reward.isCurrent) FontWeight.Bold else FontWeight.Medium
                ),
                color = Ink
            )

            if (reward.hasDecorGift) {
                Text(text = "🎁", fontSize = 20.sp)
            } else {
                Text(text = "🫐", fontSize = 16.sp)
            }

            Text(
                text = "+${reward.berries}",
                style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = if (reward.isClaimed) Color(0xFF1B5E20) else Ink
            )

            if (reward.isClaimed) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Ink),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.ui_cd_claimed),
                        modifier = Modifier.size(11.dp),
                        tint = WhitePure
                    )
                }
            } else if (reward.isCurrent) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Coral)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ui_daily_today),
                        style = typography.caption.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = WhitePure
                    )
                }
            }
        }
    }
}

/**
 * Shares daily rescue result with Android share sheet and copies to clipboard
 */
private fun shareResult(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("ArrowBuddies Daily Rescue", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied result to clipboard! 📋", Toast.LENGTH_SHORT).show()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Daily Rescue Result")
    context.startActivity(shareIntent)
}
