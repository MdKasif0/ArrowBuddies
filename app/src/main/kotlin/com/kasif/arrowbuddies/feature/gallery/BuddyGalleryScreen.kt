package com.kasif.arrowbuddies.feature.gallery

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.buddies.BuddyAvatar
import com.kasif.arrowbuddies.core.buddies.LocalSharedGameClock
import com.kasif.arrowbuddies.core.buddies.model.BuddyExpression
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.designsystem.component.CandyButton
import com.kasif.arrowbuddies.core.designsystem.component.CandyButtonVariant
import com.kasif.arrowbuddies.core.designsystem.component.ToyPanel
import com.kasif.arrowbuddies.core.designsystem.modifier.bounceClick
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.core.designsystem.theme.WorldTheme
import com.kasif.arrowbuddies.engine.model.Direction

/**
 * Debug screen showcasing all 24 buddy species across the 8 worlds.
 * Interactive controls allow changing expressions, directions, and scaling sizes (32dp to 160dp).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BuddyGalleryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDirection by remember { mutableStateOf(Direction.RIGHT) }
    var selectedExpression by remember { mutableStateOf(BuddyExpression.IDLE) }
    var avatarSizeDp by remember { mutableFloatStateOf(72f) }

    // 1. Shared Frame Clock: One single transition for all 24 buddies on screen
    val infiniteTransition = rememberInfiniteTransition(label = "GallerySharedClock")
    val animMillis = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60000f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GalleryMillis"
    )
    val sharedClockState = remember(infiniteTransition) {
        object : State<Long> {
            override val value: Long get() = animMillis.value.toLong()
        }
    }

    CompositionLocalProvider(LocalSharedGameClock provides sharedClockState) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.screen_buddy_gallery),
                                style = ArrowBuddiesTheme.typography.title,
                                color = ArrowBuddiesTheme.colors.ink
                            )
                            Text(
                                text = "24 Species • 8 Worlds • Pure Vector Compose",
                                style = ArrowBuddiesTheme.typography.caption,
                                color = ArrowBuddiesTheme.colors.ink.copy(alpha = 0.7f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.bounceClick()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint = ArrowBuddiesTheme.colors.ink
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ArrowBuddiesTheme.colors.cream
                    )
                )
            },
            containerColor = ArrowBuddiesTheme.colors.cream,
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Control Panel Header
                item(key = "controls_panel") {
                    ToyPanel(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Direction Selection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Direction:",
                                    style = ArrowBuddiesTheme.typography.subtitle,
                                    color = ArrowBuddiesTheme.colors.ink
                                )
                                CandyButton(
                                    text = "Spin 90° ↻",
                                    variant = CandyButtonVariant.Secondary,
                                    onClick = {
                                        selectedDirection = selectedDirection.turnClockwise90()
                                    },
                                    minHeight = 36.dp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Direction.entries.forEach { dir ->
                                    val isSelected = dir == selectedDirection
                                    val arrowGlyph = when (dir) {
                                        Direction.UP -> "↑ Up"
                                        Direction.RIGHT -> "→ Right"
                                        Direction.DOWN -> "↓ Down"
                                        Direction.LEFT -> "← Left"
                                    }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedDirection = dir },
                                        label = { Text(arrowGlyph, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ArrowBuddiesTheme.colors.mango,
                                            selectedLabelColor = ArrowBuddiesTheme.colors.ink
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Expression Selector
                            Text(
                                text = "Expression:",
                                style = ArrowBuddiesTheme.typography.subtitle,
                                color = ArrowBuddiesTheme.colors.ink
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BuddyExpression.entries.forEach { expr ->
                                    val isSelected = expr == selectedExpression
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedExpression = expr },
                                        label = { Text(stringResource(expr.labelRes)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ArrowBuddiesTheme.colors.mint,
                                            selectedLabelColor = ArrowBuddiesTheme.colors.ink
                                        )
                                    )
                                }
                            }

                            // Size Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Avatar Size: ${avatarSizeDp.toInt()}dp",
                                    style = ArrowBuddiesTheme.typography.subtitle,
                                    color = ArrowBuddiesTheme.colors.ink
                                )

                                // Preset size buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(36, 64, 96, 128).forEach { preset ->
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = if (avatarSizeDp.toInt() == preset) ArrowBuddiesTheme.colors.sky else Color(0xFFECEFF1),
                                                    shape = CircleShape
                                                )
                                                .clickable { avatarSizeDp = preset.toFloat() }
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${preset}p",
                                                style = ArrowBuddiesTheme.typography.caption,
                                                color = ArrowBuddiesTheme.colors.ink,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Slider(
                                value = avatarSizeDp,
                                onValueChange = { avatarSizeDp = it },
                                valueRange = 32f..160f,
                                colors = SliderDefaults.colors(
                                    thumbColor = ArrowBuddiesTheme.colors.mango,
                                    activeTrackColor = ArrowBuddiesTheme.colors.mango
                                )
                            )
                        }
                    }
                }

                // 24 Species grouped by 8 Worlds
                WorldTheme.allWorlds.forEach { world ->
                    val speciesInWorld = BuddySpecies.byWorld(world.index)

                    item(key = "world_header_${world.index}") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(world.accentColor, CircleShape)
                            )
                            Text(
                                text = "World ${world.index}: ${world.name}",
                                style = ArrowBuddiesTheme.typography.title,
                                color = ArrowBuddiesTheme.colors.ink
                            )
                        }
                    }

                    item(key = "world_grid_${world.index}") {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            maxItemsInEachRow = if (avatarSizeDp > 100f) 1 else if (avatarSizeDp > 60f) 2 else 3
                        ) {
                            speciesInWorld.forEach { species ->
                                ToyPanel(
                                    modifier = Modifier
                                        .weight(1f)
                                        .bounceClick(),
                                    backgroundColor = Color.White
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Buddy Avatar Display
                                        BuddyAvatar(
                                            species = species,
                                            direction = selectedDirection,
                                            expression = selectedExpression,
                                            size = avatarSizeDp.dp,
                                            phaseSeed = (species.ordinal * 1337) % 10000
                                        )

                                        // Species Name (English + Hindi)
                                        Text(
                                            text = stringResource(species.nameRes),
                                            style = ArrowBuddiesTheme.typography.subtitle,
                                            color = ArrowBuddiesTheme.colors.ink,
                                            textAlign = TextAlign.Center
                                        )

                                        // Feature Tag
                                        Text(
                                            text = "Ear: ${species.earType.name.lowercase()} • Tail: ${species.tailType.name.lowercase()}",
                                            style = ArrowBuddiesTheme.typography.caption.copy(fontSize = 11.sp),
                                            color = ArrowBuddiesTheme.colors.ink.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Preview(name = "Buddy Gallery Preview")
@Composable
private fun BuddyGalleryPreview() {
    ArrowBuddiesTheme {
        BuddyGalleryScreen(onNavigateBack = {})
    }
}
