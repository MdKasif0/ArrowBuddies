package com.kasif.arrowbuddies.feature.album

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import com.kasif.arrowbuddies.core.designsystem.component.CounterPill
import com.kasif.arrowbuddies.core.designsystem.component.GameDialog
import com.kasif.arrowbuddies.core.designsystem.component.RoundIconButton
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
import com.kasif.arrowbuddies.core.economy.EconomyConfig
import com.kasif.arrowbuddies.core.model.AchievementType
import com.kasif.arrowbuddies.core.model.BuddyAccessory
import com.kasif.arrowbuddies.core.model.FriendshipTier
import com.kasif.arrowbuddies.engine.model.Direction
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AlbumScreen(
    uiState: AlbumUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onEvent: (AlbumUiEvent) -> Unit = {}
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val unlockedAchievementsCount = uiState.achievementsList.count { it.unlocked }

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
            // TOP HEADER BAR
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
                        text = stringResource(R.string.screen_album),
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
            // SEGMENTED TABS: [🐾 Buddies (X/24)] [🏆 Achievements (Y/12)]
            // ----------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp))
                    .background(Color(0xFFEFE8DB), RoundedCornerShape(18.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Buddies Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (uiState.selectedTab == AlbumTab.BUDDIES) WhitePure else Color.Transparent)
                        .border(
                            width = if (uiState.selectedTab == AlbumTab.BUDDIES) 1.5.dp else 0.dp,
                            color = if (uiState.selectedTab == AlbumTab.BUDDIES) Color(0xFFD4C8B5) else Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onEvent(AlbumUiEvent.SelectTab(AlbumTab.BUDDIES)) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🐾 Buddies (${uiState.discoveredSpeciesCount}/24)",
                        style = typography.caption.copy(
                            fontSize = 13.sp,
                            fontWeight = if (uiState.selectedTab == AlbumTab.BUDDIES) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (uiState.selectedTab == AlbumTab.BUDDIES) Ink else Ink.copy(alpha = 0.6f)
                    )
                }

                // Achievements Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (uiState.selectedTab == AlbumTab.ACHIEVEMENTS) WhitePure else Color.Transparent)
                        .border(
                            width = if (uiState.selectedTab == AlbumTab.ACHIEVEMENTS) 1.5.dp else 0.dp,
                            color = if (uiState.selectedTab == AlbumTab.ACHIEVEMENTS) Color(0xFFD4C8B5) else Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onEvent(AlbumUiEvent.SelectTab(AlbumTab.ACHIEVEMENTS)) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏆 Badges ($unlockedAchievementsCount/12)",
                        style = typography.caption.copy(
                            fontSize = 13.sp,
                            fontWeight = if (uiState.selectedTab == AlbumTab.ACHIEVEMENTS) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (uiState.selectedTab == AlbumTab.ACHIEVEMENTS) Ink else Ink.copy(alpha = 0.6f)
                    )
                }
            }

            // ----------------------------------------------------
            // TAB CONTENT
            // ----------------------------------------------------
            when (uiState.selectedTab) {
                AlbumTab.BUDDIES -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.speciesList, key = { it.species.name }) { item ->
                            if (item.unlocked) {
                                UnlockedSpeciesCard(
                                    item = item,
                                    onClick = { onEvent(AlbumUiEvent.ClickSpecies(item.species)) }
                                )
                            } else {
                                LockedSpeciesCard(
                                    species = item.species,
                                    onClick = { onEvent(AlbumUiEvent.ClickSpecies(item.species)) }
                                )
                            }
                        }
                    }
                }

                AlbumTab.ACHIEVEMENTS -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.achievementsList, key = { it.type.id }) { item ->
                            AchievementCard(item = item)
                        }
                    }
                }
            }
        }

        // ----------------------------------------------------
        // FLOATING DEBUG PANEL TOGGLE (BOTTOM-RIGHT)
        // ----------------------------------------------------
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = uiState.showDebugPanel,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
                            .background(WhitePure, RoundedCornerShape(16.dp))
                            .border(1.5.dp, Color(0xFFE2D6C0), RoundedCornerShape(16.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🛠️ QA Debug Berries",
                            style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = Ink
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CandyButton(
                                text = "+100 🫐",
                                onClick = { onEvent(AlbumUiEvent.GrantDebugBerries(100)) },
                                variant = CandyButtonVariant.Success,
                                modifier = Modifier.height(34.dp)
                            )
                            CandyButton(
                                text = "+500 🫐",
                                onClick = { onEvent(AlbumUiEvent.GrantDebugBerries(500)) },
                                variant = CandyButtonVariant.Primary,
                                modifier = Modifier.height(34.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .shadow(elevation = 4.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Cream)
                        .border(1.dp, Color(0xFFD4C8B5), CircleShape)
                        .clickable { onEvent(AlbumUiEvent.ToggleDebugPanel) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🛠️", fontSize = 16.sp)
                }
            }
        }

        // ----------------------------------------------------
        // WARDROBE & FUN FACT MODAL
        // ----------------------------------------------------
        if (uiState.selectedSpeciesModal != null) {
            val item = uiState.selectedSpeciesModal
            SpeciesWardrobeModal(
                item = item,
                onEquipAccessory = { acc -> onEvent(AlbumUiEvent.EquipAccessory(item.species.name, acc)) },
                onDismiss = { onEvent(AlbumUiEvent.DismissSpeciesModal) }
            )
        }
    }
}

/**
 * Card for Unlocked Species with Avatar, Name, Rescued Count & Friendship Progress
 */
@Composable
private fun UnlockedSpeciesCard(
    item: SpeciesAlbumUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography

    Box(
        modifier = modifier
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(WhitePure)
            .border(1.5.dp, Color(0xFFE6DCCD), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar with equipped accessory
            BuddyAvatar(
                species = item.species,
                direction = Direction.RIGHT,
                expression = BuddyExpression.HAPPY,
                accessory = item.equippedAccessory,
                size = 64.dp
            )

            // Buddy Name
            Text(
                text = stringResource(item.species.nameRes),
                style = typography.title.copy(fontSize = 16.sp),
                color = Ink,
                textAlign = TextAlign.Center
            )

            // Rescued Count Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF0EAE1))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Rescued: ${item.rescuedCount}",
                    style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = Ink.copy(alpha = 0.8f)
                )
            }

            // Friendship Tier Progress Bar
            val tier = item.tier
            val nextTarget = when (tier) {
                FriendshipTier.LOCKED -> EconomyConfig.TIER_BRONZE_THRESHOLD
                FriendshipTier.BRONZE -> EconomyConfig.TIER_SILVER_THRESHOLD
                FriendshipTier.SILVER -> EconomyConfig.TIER_GOLD_THRESHOLD
                FriendshipTier.GOLD -> EconomyConfig.TIER_GOLD_THRESHOLD
            }
            val progressFraction = (item.rescuedCount.toFloat() / nextTarget).coerceIn(0f, 1f)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (tier) {
                            FriendshipTier.LOCKED -> "Friend"
                            FriendshipTier.BRONZE -> "🥉 Bronze"
                            FriendshipTier.SILVER -> "🥈 Silver"
                            FriendshipTier.GOLD -> "🥇 Gold"
                        },
                        style = typography.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Ink.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (tier == FriendshipTier.GOLD) "MAX 👑" else "${item.rescuedCount}/$nextTarget",
                        style = typography.caption.copy(fontSize = 10.sp),
                        color = Ink.copy(alpha = 0.6f)
                    )
                }

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = when (tier) {
                        FriendshipTier.LOCKED -> Sky
                        FriendshipTier.BRONZE -> Color(0xFFCD7F32)
                        FriendshipTier.SILVER -> Color(0xFFA8A8A8)
                        FriendshipTier.GOLD -> Mango
                    },
                    trackColor = Color(0xFFEFE8DB),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Card for Locked Species: Dark mysterious silhouette with "?"
 */
@Composable
private fun LockedSpeciesCard(
    species: BuddySpecies,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography

    Box(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF382E4F), Color(0xFF221A33))
                )
            )
            .border(1.5.dp, Color(0xFF4E416D), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Dark silhouette placeholder with question mark
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1D162B)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "?",
                    style = typography.title.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFF8E82AA)
                )
            }

            // Locked Name
            Text(
                text = "???",
                style = typography.title.copy(fontSize = 16.sp),
                color = WhitePure.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            // World indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    modifier = Modifier.size(12.dp),
                    tint = Color(0xFF8E82AA)
                )
                Text(
                    text = "World ${species.worldIndex}",
                    style = typography.caption.copy(fontSize = 11.sp),
                    color = Color(0xFF8E82AA)
                )
            }
        }
    }
}

/**
 * Wardrobe & Kid-Friendly Fun Fact Modal
 */
@Composable
private fun SpeciesWardrobeModal(
    item: SpeciesAlbumUiModel,
    onEquipAccessory: (BuddyAccessory) -> Unit,
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
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Large Hero Avatar
                BuddyAvatar(
                    species = item.species,
                    direction = Direction.RIGHT,
                    expression = BuddyExpression.HAPPY,
                    accessory = item.equippedAccessory,
                    size = 88.dp
                )

                // Title & Tier
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(item.species.nameRes),
                        style = typography.title.copy(fontSize = 22.sp),
                        color = Ink
                    )
                    Text(
                        text = when (item.tier) {
                            FriendshipTier.LOCKED -> "New Friend 🐾"
                            FriendshipTier.BRONZE -> "Bronze Buddy 🥉 (${item.rescuedCount} Rescues)"
                            FriendshipTier.SILVER -> "Silver Pal 🥈 (${item.rescuedCount} Rescues)"
                            FriendshipTier.GOLD -> "Golden Best Friend 🥇 (${item.rescuedCount} Rescues)"
                        },
                        style = typography.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                        color = Ink.copy(alpha = 0.7f)
                    )
                }

                // Kid-friendly Fun Fact Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Cream)
                        .border(1.5.dp, Color(0xFFE2D6C0), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "💡 Fun Buddy Fact:",
                            style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = Coral
                        )
                        Text(
                            text = stringResource(item.funFactRes),
                            style = typography.body.copy(fontSize = 13.sp),
                            color = Ink
                        )
                    }
                }

                // Accessory Wardrobe Slots
                Text(
                    text = "Accessories Wardrobe ✨",
                    style = typography.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    color = Ink
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Slot 1: None (Default)
                    AccessorySlot(
                        title = "None",
                        emoji = "🚫",
                        unlocked = true,
                        isEquipped = item.equippedAccessory == BuddyAccessory.NONE,
                        onClick = { onEquipAccessory(BuddyAccessory.NONE) }
                    )

                    // Slot 2: Bow (Bronze)
                    val isBowUnlocked = item.tier >= FriendshipTier.BRONZE
                    AccessorySlot(
                        title = "Bow (10)",
                        emoji = "🎀",
                        unlocked = isBowUnlocked,
                        isEquipped = item.equippedAccessory == BuddyAccessory.BOW,
                        onClick = { if (isBowUnlocked) onEquipAccessory(BuddyAccessory.BOW) }
                    )

                    // Slot 3: Scarf (Silver)
                    val isScarfUnlocked = item.tier >= FriendshipTier.SILVER
                    AccessorySlot(
                        title = "Scarf (50)",
                        emoji = "🧣",
                        unlocked = isScarfUnlocked,
                        isEquipped = item.equippedAccessory == BuddyAccessory.SCARF,
                        onClick = { if (isScarfUnlocked) onEquipAccessory(BuddyAccessory.SCARF) }
                    )

                    // Slot 4: Crown (Gold)
                    val isCrownUnlocked = item.tier >= FriendshipTier.GOLD
                    AccessorySlot(
                        title = "Crown (150)",
                        emoji = "👑",
                        unlocked = isCrownUnlocked,
                        isEquipped = item.equippedAccessory == BuddyAccessory.CROWN,
                        onClick = { if (isCrownUnlocked) onEquipAccessory(BuddyAccessory.CROWN) }
                    )
                }

                CandyButton(
                    text = "Done",
                    onClick = onDismiss,
                    variant = CandyButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AccessorySlot(
    title: String,
    emoji: String,
    unlocked: Boolean,
    isEquipped: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(elevation = if (isEquipped) 4.dp else 1.dp, shape = RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(
                    when {
                        isEquipped -> Mint
                        unlocked -> WhitePure
                        else -> Color(0xFFE0E0E0)
                    }
                )
                .border(
                    width = if (isEquipped) 2.dp else 1.dp,
                    color = if (isEquipped) Ink else Color(0xFFD4C8B5),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(enabled = unlocked) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (unlocked) {
                Text(text = emoji, fontSize = 22.sp)
                if (isEquipped) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Ink),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Equipped",
                            modifier = Modifier.size(11.dp),
                            tint = WhitePure
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Gray
                )
            }
        }

        Text(
            text = title,
            style = typography.caption.copy(fontSize = 10.sp),
            color = if (unlocked) Ink else Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Achievement Item Card
 */
@Composable
private fun AchievementCard(
    item: AchievementAlbumUiModel,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val emoji = item.type.iconEmoji

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (item.unlocked) WhitePure else Color(0xFFF7F3EC))
            .border(
                width = 1.5.dp,
                color = if (item.unlocked) Color(0xFF78C850) else Color(0xFFE2D6C0),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Emoji Icon Box
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (item.unlocked) Mint else Color(0xFFECE4D6)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 26.sp)
            }

            // Info Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(item.type.titleRes),
                        style = typography.title.copy(fontSize = 15.sp),
                        color = Ink
                    )

                    if (item.unlocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Unlocked! ✨",
                                style = typography.caption.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(item.type.descRes),
                    style = typography.caption.copy(fontSize = 12.sp),
                    color = Ink.copy(alpha = 0.70f)
                )

                // Progress Bar
                val progressFraction = (item.progress.toFloat() / item.maxProgress).coerceIn(0f, 1f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (item.unlocked) Color(0xFF78C850) else Mango,
                        trackColor = Color(0xFFEAE2D3),
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "${item.progress}/${item.maxProgress}",
                        style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = Ink.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
