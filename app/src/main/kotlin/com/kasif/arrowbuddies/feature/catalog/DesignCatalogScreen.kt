package com.kasif.arrowbuddies.feature.catalog

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasif.arrowbuddies.core.designsystem.component.AnimatedSkyBackground
import com.kasif.arrowbuddies.core.designsystem.component.CandyButton
import com.kasif.arrowbuddies.core.designsystem.component.CandyButtonVariant
import com.kasif.arrowbuddies.core.designsystem.component.CounterPill
import com.kasif.arrowbuddies.core.designsystem.component.GameDialog
import com.kasif.arrowbuddies.core.designsystem.component.HeartsRow
import com.kasif.arrowbuddies.core.designsystem.component.JellyProgressBar
import com.kasif.arrowbuddies.core.designsystem.component.ParticleBurst
import com.kasif.arrowbuddies.core.designsystem.component.ParticleSystemState
import com.kasif.arrowbuddies.core.designsystem.component.RoundIconButton
import com.kasif.arrowbuddies.core.designsystem.component.SpeechBubble
import com.kasif.arrowbuddies.core.designsystem.component.StarRow
import com.kasif.arrowbuddies.core.designsystem.component.ToggleSwitch
import com.kasif.arrowbuddies.core.designsystem.component.ToyPanel
import com.kasif.arrowbuddies.core.designsystem.theme.AllWorldThemes
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.designsystem.theme.MediumRoundedShape
import com.kasif.arrowbuddies.core.designsystem.theme.SmallRoundedShape
import com.kasif.arrowbuddies.core.designsystem.theme.TactileColor
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure
import com.kasif.arrowbuddies.core.designsystem.theme.WorldTheme

@Composable
fun DesignCatalogScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedWorld by remember { mutableStateOf(AllWorldThemes[0]) }
    val particleState = remember { ParticleSystemState() }

    var berryCount by remember { mutableIntStateOf(120) }
    var heartsRemaining by remember { mutableIntStateOf(3) }
    var starsEarned by remember { mutableIntStateOf(3) }
    var jellyProgress by remember { mutableFloatStateOf(0.6f) }
    var switchA by remember { mutableStateOf(true) }
    var switchB by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    ArrowBuddiesTheme(worldTheme = selectedWorld) {
        val colors = ArrowBuddiesThemeTokens.colors
        val typography = ArrowBuddiesThemeTokens.typography
        val spacing = ArrowBuddiesThemeTokens.spacing

        Box(modifier = modifier.fillMaxSize()) {
            // Live Parallax Drifting Sky for the Selected World
            AnimatedSkyBackground(worldTheme = selectedWorld)

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.medium)
            ) {
                Spacer(modifier = Modifier.height(spacing.extraLarge))

                // Header with Back button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoundIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = onNavigateBack,
                        variant = CandyButtonVariant.Ghost,
                        size = 46.dp
                    )
                    Spacer(modifier = Modifier.width(spacing.small))
                    Text(
                        text = "Design System Catalog",
                        style = typography.title,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(spacing.medium))

                // Section 1: World Theme Picker (8 Worlds)
                SectionHeader("1. World Themes (8 Biomes)")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AllWorldThemes.forEach { theme ->
                        val isSelected = theme.id == selectedWorld.id
                        WorldChip(
                            theme = theme,
                            isSelected = isSelected,
                            onClick = { selectedWorld = theme }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.medium))

                // World details card
                ToyPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "World ${selectedWorld.id}: ${selectedWorld.name}",
                            style = typography.subtitle,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Mechanic: ${selectedWorld.mechanic} • Buddies: ${selectedWorld.species.joinToString(", ")}",
                            style = typography.body,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TileSwatch(selectedWorld.tileCheckerA, "Tile A")
                            TileSwatch(selectedWorld.tileCheckerB, "Tile B")
                            TileSwatch(selectedWorld.boardBase, "Board")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.large))

                // Section 2: Tactile Color Palette
                SectionHeader("2. Tactile Palette (Main, Lip, Highlight)")
                TactileColorCard("Mango (#FFB627)", colors.mango)
                TactileColorCard("Coral (#FF6B6B)", colors.coral)
                TactileColorCard("Mint (#3DD6A3)", colors.mint)
                TactileColorCard("Sky (#4CC9F0)", colors.sky)
                TactileColorCard("Grape (#7B61FF)", colors.grape)
                TactileColorCard("Cream (#FFF7EC)", colors.cream)
                TactileColorCard("Ink (#2B2240)", colors.ink)

                Spacer(modifier = Modifier.height(spacing.large))

                // Section 3: Typography (Baloo 2)
                SectionHeader("3. Typography (Baloo 2 with Tabular Figures)")
                ToyPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Display: ArrowBuddies 123", style = typography.display, color = colors.textPrimary)
                        Text("Title: Forest Level 45", style = typography.title, color = colors.textPrimary)
                        Text("Subtitle: Big Rescue Ahead!", style = typography.subtitle, color = colors.textPrimary)
                        Text("Button: RESCUE BUDDIES!", style = typography.button, color = colors.mango.lip)
                        Text("Body: Tap an animal to dash to the edge.", style = typography.body, color = colors.textPrimary)
                        Text("Caption / Hindi: नमस्ते प्यारे साथी (Hello Buddies)", style = typography.caption, color = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(spacing.large))

                // Section 4: 3D Candy Buttons
                SectionHeader("4. 3D Candy Buttons (Press Down Mechanic)")
                CandyButton(
                    text = "Primary Button (Mango)",
                    variant = CandyButtonVariant.Primary,
                    leadingIcon = Icons.Default.PlayArrow,
                    onClick = {
                        particleState.spawnSparkle(screenWidthPx * 0.5f, screenHeightPx * 0.4f)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                CandyButton(
                    text = "Secondary Button (Sky)",
                    variant = CandyButtonVariant.Secondary,
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(10.dp))
                CandyButton(
                    text = "Danger / Bonk (Coral)",
                    variant = CandyButtonVariant.Danger,
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(10.dp))
                CandyButton(
                    text = "Success / Rescue (Mint)",
                    variant = CandyButtonVariant.Success,
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(10.dp))
                CandyButton(
                    text = "Special (Grape)",
                    variant = CandyButtonVariant.Special,
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(spacing.large))

                // Section 5: Round Icon Buttons & Counter Pills
                SectionHeader("5. Round Icon Buttons & Counter Pills")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoundIconButton(
                        icon = Icons.Default.PlayArrow,
                        variant = CandyButtonVariant.Primary,
                        badgeCount = 3,
                        onClick = { berryCount += 10 }
                    )
                    RoundIconButton(
                        icon = Icons.Default.Refresh,
                        variant = CandyButtonVariant.Secondary,
                        badgeCount = 1,
                        onClick = { berryCount -= 5 }
                    )
                    CounterPill(
                        count = berryCount,
                        icon = Icons.Default.Star,
                        iconTint = colors.mango.main
                    )
                    CounterPill(
                        count = heartsRemaining,
                        icon = Icons.Default.Favorite,
                        iconTint = colors.coral.main
                    )
                }

                Spacer(modifier = Modifier.height(spacing.large))

                // Section 6: Hearts Row & Star Row
                SectionHeader("6. Hearts Row (Crack/Fall) & Star Row")
                ToyPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Hearts ($heartsRemaining/3):", style = typography.body, color = colors.textPrimary)
                            HeartsRow(heartsRemaining = heartsRemaining)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CandyButton(
                                text = "Lose Heart",
                                variant = CandyButtonVariant.Danger,
                                minHeight = 44.dp,
                                onClick = {
                                    if (heartsRemaining > 0) heartsRemaining--
                                },
                                modifier = Modifier.weight(1f)
                            )
                            CandyButton(
                                text = "Reset Hearts",
                                variant = CandyButtonVariant.Success,
                                minHeight = 44.dp,
                                onClick = { heartsRemaining = 3 },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Stars ($starsEarned/3):", style = typography.body, color = colors.textPrimary)
                            StarRow(starsEarned = starsEarned)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CandyButton(
                                text = "1 Star",
                                variant = CandyButtonVariant.Ghost,
                                minHeight = 44.dp,
                                onClick = { starsEarned = 1 },
                                modifier = Modifier.weight(1f)
                            )
                            CandyButton(
                                text = "2 Stars",
                                variant = CandyButtonVariant.Ghost,
                                minHeight = 44.dp,
                                onClick = { starsEarned = 2 },
                                modifier = Modifier.weight(1f)
                            )
                            CandyButton(
                                text = "3 Stars!",
                                variant = CandyButtonVariant.Primary,
                                minHeight = 44.dp,
                                onClick = {
                                    starsEarned = 3
                                    particleState.spawnConfetti(screenWidthPx * 0.5f, screenHeightPx * 0.5f)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.large))

                // Section 7: Jelly Progress Bar
                SectionHeader("7. Jelly Progress Bar (Squishy Wobble)")
                ToyPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Progress: ${(jellyProgress * 100).toInt()}%", style = typography.body, color = colors.textPrimary)
                        JellyProgressBar(progress = jellyProgress)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CandyButton(
                                text = "-20%",
                                variant = CandyButtonVariant.Ghost,
                                minHeight = 44.dp,
                                onClick = { jellyProgress = (jellyProgress - 0.2f).coerceAtLeast(0f) },
                                modifier = Modifier.weight(1f)
                            )
                            CandyButton(
                                text = "+20%",
                                variant = CandyButtonVariant.Success,
                                minHeight = 44.dp,
                                onClick = { jellyProgress = (jellyProgress + 0.2f).coerceAtMost(1f) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.large))

                // Section 8: Speech Bubble & Chunky Toggles
                SectionHeader("8. Speech Bubble & Chunky Toggles")
                SpeechBubble(
                    text = "Welcome to ArrowBuddies! Tap a buddy to dash to freedom!",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                ToyPanel(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Sound Effects", style = typography.body, color = colors.textPrimary)
                            Text("Music Track", style = typography.body, color = colors.textPrimary)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ToggleSwitch(checked = switchA, onCheckedChange = { switchA = it })
                            ToggleSwitch(checked = switchB, onCheckedChange = { switchB = it })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.large))

                // Section 9: Particle System Bursts & Modal Dialog
                SectionHeader("9. Particles & Modal Dialog")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CandyButton(
                        text = "🎉 Confetti Burst",
                        variant = CandyButtonVariant.Primary,
                        onClick = {
                            particleState.spawnConfetti(screenWidthPx * 0.5f, screenHeightPx * 0.4f, count = 100)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    CandyButton(
                        text = "✨ Sparkle Burst",
                        variant = CandyButtonVariant.Secondary,
                        onClick = {
                            particleState.spawnSparkle(screenWidthPx * 0.5f, screenHeightPx * 0.4f, count = 60)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                CandyButton(
                    text = "Open Game Dialog",
                    variant = CandyButtonVariant.Special,
                    onClick = { showDialog = true }
                )

                Spacer(modifier = Modifier.height(spacing.huge))
            }

            // Particle System Canvas Overlay (Zero allocations per frame)
            ParticleBurst(state = particleState)

            // Game Dialog Modal
            GameDialog(
                visible = showDialog,
                onDismissRequest = { showDialog = false }
            ) {
                ToyPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Level 1 Cleared! 🌟",
                            style = typography.title,
                            color = colors.textPrimary
                        )
                        StarRow(starsEarned = 3, animateSequential = false)
                        Text(
                            text = "All 4 Buddies escaped unharmed!",
                            style = typography.body,
                            color = colors.textSecondary
                        )
                        CandyButton(
                            text = "Next Adventure",
                            variant = CandyButtonVariant.Primary,
                            onClick = { showDialog = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = ArrowBuddiesThemeTokens.typography.subtitle,
        color = ArrowBuddiesThemeTokens.colors.textPrimary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun WorldChip(
    theme: WorldTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = ArrowBuddiesThemeTokens.colors
    val typography = ArrowBuddiesThemeTokens.typography

    Box(
        modifier = Modifier
            .shadow(if (isSelected) 4.dp else 1.dp, shape = SmallRoundedShape)
            .background(if (isSelected) theme.accent.main else WhitePure, shape = SmallRoundedShape)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) theme.accent.lip else Color(0xFFE4D5BC),
                shape = SmallRoundedShape
            )
            .clip(SmallRoundedShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = "W${theme.id}: ${theme.name}",
            style = typography.caption.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) theme.accent.onColor else colors.textPrimary
            )
        )
    }
}

@Composable
private fun TileSwatch(color: Color, label: String) {
    val typography = ArrowBuddiesThemeTokens.typography
    val colors = ArrowBuddiesThemeTokens.colors

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color, shape = RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFF2B2240).copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = typography.caption, color = colors.textSecondary)
    }
}

@Composable
private fun TactileColorCard(
    label: String,
    tactile: TactileColor
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val colors = ArrowBuddiesThemeTokens.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(WhitePure, shape = SmallRoundedShape)
            .border(1.dp, Color(0xFFEADBCE), shape = SmallRoundedShape)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = typography.body.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Swatch(tactile.highlight, "High")
                Swatch(tactile.main, "Main")
                Swatch(tactile.lip, "Lip")
            }
        }
    }
}

@Composable
private fun Swatch(color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color, shape = CircleShape)
                .border(1.dp, Color.Black.copy(alpha = 0.15f), CircleShape)
        )
        Text(label, style = ArrowBuddiesThemeTokens.typography.caption.copy(fontSize = 10.sp), color = Color(0xFF6B6280))
    }
}

@Preview(name = "Design Catalog", showBackground = true)
@Composable
private fun DesignCatalogScreenPreview() {
    ArrowBuddiesTheme {
        DesignCatalogScreen(onNavigateBack = {})
    }
}
