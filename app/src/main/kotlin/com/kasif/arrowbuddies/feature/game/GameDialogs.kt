package com.kasif.arrowbuddies.feature.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.buddies.BuddyAvatar
import com.kasif.arrowbuddies.core.buddies.model.BuddyExpression
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.designsystem.component.CandyButton
import com.kasif.arrowbuddies.core.designsystem.component.CandyButtonVariant
import com.kasif.arrowbuddies.core.designsystem.component.GameDialog
import com.kasif.arrowbuddies.core.designsystem.component.StarRow
import com.kasif.arrowbuddies.core.designsystem.component.ToggleSwitch
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
import com.kasif.arrowbuddies.engine.model.Buddy
import com.kasif.arrowbuddies.engine.model.Direction

/**
 * Win celebration sheet: shows dancing buddies, sequential stars, berry reward,
 * and optional "New Friend!" flip card.
 */
@Composable
fun LevelWinSheet(
    stars: Int,
    rescuedBuddies: List<Buddy>,
    goldenBonus: Int,
    newSpecies: BuddySpecies?,
    onNextLevel: () -> Unit,
    onRestart: () -> Unit,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val totalBerries = 10 + (5 * stars) + goldenBonus

    val infiniteTransition = rememberInfiniteTransition(label = "buddyDance")
    val hopOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hop"
    )

    // New Friend Card Flip Animation
    val cardFlipAnim = remember { Animatable(0f) }
    LaunchedEffect(newSpecies) {
        if (newSpecies != null) {
            cardFlipAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        }
    }

    GameDialog(
        visible = true,
        onDismissRequest = { /* Modal */ },
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Dancing Buddies on top of the panel
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                val previewBuddies = rescuedBuddies.take(4)
                previewBuddies.forEachIndexed { index, buddy ->
                    val speciesEnum = runCatching { BuddySpecies.valueOf(buddy.species) }.getOrDefault(BuddySpecies.BUNNY)
                    BuddyAvatar(
                        species = speciesEnum,
                        direction = Direction.UP,
                        expression = BuddyExpression.HAPPY,
                        size = 54.dp,
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = if (index % 2 == 0) hopOffset else -hopOffset
                            }
                            .padding(horizontal = 4.dp)
                    )
                }
            }

            ToyPanel(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.game_all_buddies_rescued),
                        style = typography.title.copy(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold),
                        color = Ink,
                        textAlign = TextAlign.Center
                    )

                    // 3 Stars fill
                    StarRow(
                        starsEarned = stars.coerceIn(1, 3),
                        starSize = 36.dp
                    )

                    // Berries reward pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF3DB))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+$totalBerries 🫐",
                            style = typography.title.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                            color = Ink
                        )
                        if (goldenBonus > 0) {
                            Text(
                                text = " (Golden Bonus!)",
                                style = typography.caption,
                                color = Mango,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // New Friend card if unlocked
                    newSpecies?.let { species ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Cream)
                                .border(2.dp, Mango, RoundedCornerShape(16.dp))
                                .graphicsLayer {
                                    rotationY = (1f - cardFlipAnim.value) * 90f
                                    cameraDistance = 12f * density
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                BuddyAvatar(
                                    species = species,
                                    direction = Direction.UP,
                                    expression = BuddyExpression.HAPPY,
                                    size = 52.dp
                                )
                                Column {
                                    Text(
                                        text = stringResource(R.string.game_new_friend_unlocked),
                                        style = typography.caption,
                                        color = Mango,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(species.nameRes),
                                        style = typography.title.copy(fontSize = 18.sp),
                                        color = Ink
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Buttons
                    CandyButton(
                        text = stringResource(R.string.game_next_level),
                        onClick = onNextLevel,
                        variant = CandyButtonVariant.Primary,
                        minHeight = 58.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CandyButton(
                            text = stringResource(R.string.game_replay),
                            onClick = onRestart,
                            variant = CandyButtonVariant.Secondary,
                            modifier = Modifier.weight(1f)
                        )
                        CandyButton(
                            text = stringResource(R.string.screen_map),
                            onClick = onNavigateToMap,
                            variant = CandyButtonVariant.Ghost,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Gentle fail sheet: encourages the player without shaming.
 */
@Composable
fun LevelFailSheet(
    berries: Int,
    onRestart: () -> Unit,
    onReviveWithHeart: () -> Unit,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography

    GameDialog(
        visible = true,
        onDismissRequest = { /* Modal */ },
        modifier = modifier
    ) {
        ToyPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.game_level_failed_title),
                    style = typography.title.copy(fontSize = 22.sp),
                    color = Ink,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.game_level_failed_subtitle),
                    style = typography.body,
                    color = Ink.copy(alpha = 0.70f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Primary Try Again (Free)
                CandyButton(
                    text = stringResource(R.string.game_try_again),
                    onClick = onRestart,
                    variant = CandyButtonVariant.Primary,
                    minHeight = 56.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                // Continue with +1 Heart (40 berries)
                CandyButton(
                    text = stringResource(R.string.game_revive_heart_button),
                    onClick = onReviveWithHeart,
                    variant = CandyButtonVariant.Success,
                    modifier = Modifier.fillMaxWidth()
                )

                /* Hook: Future Rewarded Video Ad Revive */

                Text(
                    text = stringResource(R.string.screen_map),
                    style = typography.subtitle.copy(fontSize = 15.sp),
                    color = Ink.copy(alpha = 0.50f),
                    modifier = Modifier
                        .clickable { onNavigateToMap() }
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Pause sheet with audio/haptics toggles and game navigation.
 */
@Composable
fun PauseSheet(
    soundEnabled: Boolean,
    musicEnabled: Boolean,
    hapticsEnabled: Boolean,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onNavigateToMap: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography

    GameDialog(
        visible = true,
        onDismissRequest = onResume,
        modifier = modifier
    ) {
        ToyPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.game_paused),
                    style = typography.title.copy(fontSize = 24.sp),
                    color = Ink
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Settings toggles in pause sheet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sounds", style = typography.body, color = Ink)
                    ToggleSwitch(checked = soundEnabled, onCheckedChange = onToggleSound)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Music", style = typography.body, color = Ink)
                    ToggleSwitch(checked = musicEnabled, onCheckedChange = onToggleMusic)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Haptics", style = typography.body, color = Ink)
                    ToggleSwitch(checked = hapticsEnabled, onCheckedChange = onToggleHaptics)
                }

                Spacer(modifier = Modifier.height(6.dp))

                CandyButton(
                    text = stringResource(R.string.game_resume),
                    onClick = onResume,
                    variant = CandyButtonVariant.Primary,
                    minHeight = 56.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CandyButton(
                        text = stringResource(R.string.game_restart),
                        onClick = onRestart,
                        variant = CandyButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    CandyButton(
                        text = stringResource(R.string.screen_map),
                        onClick = onNavigateToMap,
                        variant = CandyButtonVariant.Ghost,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Confirmation dialog when attempting to exit an active level.
 */
@Composable
fun LeaveLevelDialog(
    onStay: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography

    GameDialog(
        visible = true,
        onDismissRequest = onStay,
        modifier = modifier
    ) {
        ToyPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.game_leave_level_title),
                    style = typography.title.copy(fontSize = 22.sp),
                    color = Ink,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.game_leave_level_subtitle),
                    style = typography.body,
                    color = Ink.copy(alpha = 0.70f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                CandyButton(
                    text = stringResource(R.string.game_leave_level_stay),
                    onClick = onStay,
                    variant = CandyButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )

                CandyButton(
                    text = stringResource(R.string.game_leave_level_confirm),
                    onClick = onLeave,
                    variant = CandyButtonVariant.Danger,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Booster purchase modal when inventory is exhausted.
 */
@Composable
fun BoosterShopDialog(
    type: BoosterType,
    berries: Int,
    onBuy: (BoosterType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val price = when (type) {
        BoosterType.Hint -> 30
        BoosterType.Undo -> 20
        BoosterType.ExtraHeart -> 40
    }
    val title = when (type) {
        BoosterType.Hint -> stringResource(R.string.booster_need_hint)
        BoosterType.Undo -> stringResource(R.string.booster_need_undo)
        BoosterType.ExtraHeart -> stringResource(R.string.booster_need_heart)
    }

    GameDialog(
        visible = true,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        ToyPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = title,
                    style = typography.title.copy(fontSize = 22.sp),
                    color = Ink,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Price: $price 🫐 (You have: $berries 🫐)",
                    style = typography.body,
                    color = Ink.copy(alpha = 0.75f)
                )

                CandyButton(
                    text = "Get for $price 🫐",
                    onClick = { onBuy(type) },
                    variant = CandyButtonVariant.Success,
                    enabled = berries >= price,
                    modifier = Modifier.fillMaxWidth()
                )

                CandyButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = onDismiss,
                    variant = CandyButtonVariant.Ghost,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Polite opt-in prompt for 6 PM Daily Rescue reminder, shown once after level 5.
 */
@Composable
fun DailyReminderOptInDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography

    GameDialog(
        visible = true,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        ToyPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.reminder_prompt_title),
                    style = typography.title.copy(fontSize = 22.sp),
                    color = Ink,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.reminder_prompt_body),
                    style = typography.body,
                    color = Ink.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                CandyButton(
                    text = stringResource(R.string.reminder_prompt_confirm),
                    onClick = onAccept,
                    variant = CandyButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )

                CandyButton(
                    text = stringResource(R.string.reminder_prompt_dismiss),
                    onClick = onDismiss,
                    variant = CandyButtonVariant.Ghost,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
