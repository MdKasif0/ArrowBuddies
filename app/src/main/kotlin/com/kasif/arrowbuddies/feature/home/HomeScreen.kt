package com.kasif.arrowbuddies.feature.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.buddies.BuddyAvatar
import com.kasif.arrowbuddies.core.buddies.model.BuddyExpression
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.designsystem.component.AnimatedSkyBackground
import com.kasif.arrowbuddies.core.designsystem.component.CandyButton
import com.kasif.arrowbuddies.core.designsystem.component.CandyButtonVariant
import com.kasif.arrowbuddies.core.designsystem.component.CounterPill
import com.kasif.arrowbuddies.core.designsystem.modifier.LocalSound
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
import com.kasif.arrowbuddies.engine.model.Direction

@OptIn(ExperimentalLayoutApi::class)
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
    val typography = ArrowBuddiesThemeTokens.typography
    val soundManager = LocalSound.current

    LaunchedEffect(Unit) {
        soundManager.playMenuMusic()
    }

    // Pulsing button animation
    val infiniteTransition = rememberInfiniteTransition(label = "homePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Gentle buddy hop animation
    val buddyHop by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hop"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Live Sanctuary Sky Background
        AnimatedSkyBackground(
            modifier = Modifier.fillMaxSize(),
            worldTheme = worldThemeFor(1)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Berries Pill + Settings Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CounterPill(
                    count = uiState.berries,
                    icon = Icons.Default.Star,
                    iconTint = Mango,
                    modifier = Modifier.clickable { /* Future Shop hook */ }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onNavigateToMap,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(WhitePure)
                            .shadow(2.dp, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = stringResource(R.string.screen_map),
                            tint = Grape
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(WhitePure)
                            .shadow(2.dp, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.screen_settings),
                            tint = Ink
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Big Total Rescued Counter
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onNavigateToSanctuary() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${uiState.totalRescued}",
                        style = typography.display.copy(fontSize = 44.sp, fontWeight = FontWeight.ExtraBold),
                        color = Ink
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🐾",
                        fontSize = 32.sp
                    )
                }
                Text(
                    text = stringResource(R.string.home_total_rescued_subtitle),
                    style = typography.subtitle,
                    color = Ink.copy(alpha = 0.70f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Live Peeking Buddy in the sanctuary grass
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer { translationY = buddyHop }
                    .clickable { onNavigateToSanctuary() },
                contentAlignment = Alignment.Center
            ) {
                BuddyAvatar(
                    species = uiState.nextSpecies,
                    direction = Direction.UP,
                    expression = BuddyExpression.HAPPY,
                    size = 96.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Huge Pulsing "Play · Level X" Button
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
            ) {
                CandyButton(
                    text = stringResource(R.string.home_play_level, uiState.currentLevelIndex),
                    onClick = { onNavigateToGame(uiState.currentLevelId) },
                    variant = CandyButtonVariant.Primary,
                    leadingIcon = Icons.Default.PlayArrow,
                    minHeight = 64.dp,
                    modifier = Modifier.fillMaxWidth(0.92f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Feature Cards Grid (Daily Rescue, Rescue Run, World Map, Album)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Daily Rescue Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WhitePure),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToDaily() }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "☀️", fontSize = 24.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Mango.copy(alpha = 0.20f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (uiState.isDailyDone) "DONE" else "+50 🫐",
                                    style = typography.caption,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.screen_daily),
                            style = typography.title.copy(fontSize = 18.sp),
                            color = Ink
                        )
                        Text(
                            text = if (uiState.isDailyDone) "Come back tomorrow" else uiState.dailyCountdownFormatted,
                            style = typography.caption,
                            color = Ink.copy(alpha = 0.60f)
                        )
                    }
                }

                // Rescue Run Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = if (uiState.isRescueRunUnlocked) WhitePure else Color(0xFFF2EFF7)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = uiState.isRescueRunUnlocked) {
                            onNavigateToGame("rescuerun-1")
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "🏃‍♂️", fontSize = 24.sp)
                            if (!uiState.isRescueRunUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Mint.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ENDLESS",
                                        style = typography.caption,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.screen_rescue_run),
                            style = typography.title.copy(fontSize = 18.sp),
                            color = if (uiState.isRescueRunUnlocked) Ink else Color.Gray
                        )
                        Text(
                            text = if (uiState.isRescueRunUnlocked) "Infinite levels" else "Unlocks at World 2",
                            style = typography.caption,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // World Map Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WhitePure),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToMap() }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "🗺️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.screen_map),
                            style = typography.title.copy(fontSize = 18.sp),
                            color = Ink
                        )
                        Text(
                            text = "8 Worlds · 240 Levels",
                            style = typography.caption,
                            color = Ink.copy(alpha = 0.60f)
                        )
                    }
                }

                // Buddy Album Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WhitePure),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToAlbum() }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "📖", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.screen_album),
                            style = typography.title.copy(fontSize = 18.sp),
                            color = Ink
                        )
                        Text(
                            text = "${uiState.unlockedSpeciesCount}/24 Species Discovered",
                            style = typography.caption,
                            color = Ink.copy(alpha = 0.60f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
