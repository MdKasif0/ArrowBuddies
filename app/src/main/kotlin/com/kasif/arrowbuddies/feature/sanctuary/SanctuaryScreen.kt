package com.kasif.arrowbuddies.feature.sanctuary

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.buddies.BuddyAvatar
import com.kasif.arrowbuddies.core.buddies.model.BuddyExpression
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
import com.kasif.arrowbuddies.data.db.entity.DecorItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

private val ZONE_WIDTH = 440.dp
private const val TOTAL_ZONES = 8

@Composable
fun SanctuaryScreen(
    uiState: SanctuaryUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onEvent: (SanctuaryUiEvent) -> Unit = {}
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Shared clock for smooth 60fps decor animations
    var clockMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L)
            clockMs += 16L
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sanctuaryIdle")
    val treatPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "treatPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF87CEEB))
    ) {
        // ----------------------------------------------------
        // PARALLAX HABITAT VIEWPORT (Scrollable)
        // ----------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
        ) {
            val totalHabitatWidth = ZONE_WIDTH * TOTAL_ZONES

            // LAYER 1: SKY GRADIENT (Parallax factor 0.35)
            SkyParallaxLayer(
                scrollOffsetPx = scrollState.value.toFloat(),
                totalWidth = totalHabitatWidth,
                clockMs = clockMs
            )

            // LAYER 2: ROLLING HILLS & SILHOUETTES (Parallax factor 0.65)
            HillsParallaxLayer(
                scrollOffsetPx = scrollState.value.toFloat(),
                totalWidth = totalHabitatWidth
            )

            // LAYER 3: GROUND, DECORATIONS, SIGNBOARD & BUDDIES (Natural 1.0x)
            Row(
                modifier = Modifier
                    .width(totalHabitatWidth)
                    .fillMaxHeight()
            ) {
                for (zoneIndex in 0 until TOTAL_ZONES) {
                    val zoneId = zoneIndex + 1
                    val isUnlocked = zoneId <= uiState.unlockedZonesCount
                    val decorId = when (zoneId) {
                        1 -> "decor_pond_1"
                        2 -> "decor_swing_2"
                        3 -> "decor_flower_bed_3"
                        4 -> "decor_firepit_4"
                        5 -> "decor_hammock_5"
                        6 -> "decor_lanterns_6"
                        7 -> "decor_kite_7"
                        8 -> "decor_rangoli_mat_8"
                        else -> "decor_pond_1"
                    }
                    val decorItem = uiState.decorItems[decorId]

                    ZoneGroundSection(
                        zoneIndex = zoneIndex,
                        isUnlocked = isUnlocked,
                        decorItem = decorItem,
                        clockMs = clockMs,
                        onDecorClick = { onEvent(SanctuaryUiEvent.ClickDecorSlot(decorId)) },
                        onTreatBasketClick = { onEvent(SanctuaryUiEvent.TapTreatBasket) },
                        isTreatTimeAvailable = uiState.isTreatTimeAvailable,
                        totalRescued = uiState.totalRescued,
                        modifier = Modifier.width(ZONE_WIDTH)
                    )
                }
            }

            // ROAMING BUDDIES LAYER
            BuddiesLayer(
                buddies = uiState.buddies,
                clockMs = clockMs,
                onBuddyTap = { id -> onEvent(SanctuaryUiEvent.TapBuddy(id)) }
            )
        }

        // ----------------------------------------------------
        // TOP HUD (Floating)
        // ----------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
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

            // Habitat Zone Badge
            Box(
                modifier = Modifier
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(18.dp))
                    .background(WhitePure, RoundedCornerShape(18.dp))
                    .border(1.5.dp, Color(0xFFE2D6C0), RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${uiState.activeZoneName} 🌿",
                    style = typography.title.copy(fontSize = 17.sp),
                    color = Ink
                )
            }

            // Treat Time Basket Button
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        if (uiState.isTreatTimeAvailable) {
                            scaleX = treatPulse
                            scaleY = treatPulse
                        }
                    }
                    .shadow(
                        elevation = if (uiState.isTreatTimeAvailable) 6.dp else 2.dp,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (uiState.isTreatTimeAvailable) Mango else Color(0xFFD6CCC2))
                    .border(
                        width = 1.5.dp,
                        color = if (uiState.isTreatTimeAvailable) Coral else Color(0xFFB5A89B),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onEvent(SanctuaryUiEvent.TapTreatBasket) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (uiState.isTreatTimeAvailable) "🍯 Treat! (+20)" else "🍯 Claimed",
                        style = typography.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        color = if (uiState.isTreatTimeAvailable) Ink else Ink.copy(alpha = 0.5f)
                    )
                }
            }

            // Berry counter pill
            CounterPill(
                count = uiState.berries,
                icon = "🫐"
            )
        }

        // ----------------------------------------------------
        // BOTTOM ZONE NAVIGATION BAR (Floating Pill Strip)
        // ----------------------------------------------------
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp))
                .background(WhitePure.copy(alpha = 0.94f), RoundedCornerShape(24.dp))
                .border(1.5.dp, Color(0xFFE2D6C0), RoundedCornerShape(24.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val zoneLabels = listOf(
                "Meadow 🌸", "Bog 🐸", "Clock ⚙️", "Ruins 🏛️",
                "Forest 🔮", "Burrow 🦔", "Oasis 🌴", "Festival 🎆"
            )

            for (i in 0 until TOTAL_ZONES) {
                val isUnlocked = (i + 1) <= uiState.unlockedZonesCount
                val isCurrent = uiState.activeZoneIndex == i

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            when {
                                isCurrent -> Mint
                                isUnlocked -> Cream
                                else -> Color(0xFFE8E8E8)
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (isCurrent) Ink else Color(0xFFD4C8B5),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable(enabled = isUnlocked) {
                            onEvent(SanctuaryUiEvent.SelectZone(i))
                            scope.launch {
                                val targetPx = with(density) { (ZONE_WIDTH * i).toPx().toInt() }
                                scrollState.animateScrollTo(targetPx)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Text(
                            text = zoneLabels[i],
                            style = typography.caption.copy(
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = Ink
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                modifier = Modifier.size(12.dp),
                                tint = Ink.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "W${i + 1}",
                                style = typography.caption.copy(fontSize = 11.sp),
                                color = Ink.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }

        // ----------------------------------------------------
        // BUY DECORATION DIALOG
        // ----------------------------------------------------
        if (uiState.selectedDecorToBuy != null) {
            val item = uiState.selectedDecorToBuy
            val decorName = when (item.id) {
                "decor_pond_1" -> "Koi Pond"
                "decor_swing_2" -> "Tree Swing"
                "decor_flower_bed_3" -> "Flower Bed"
                "decor_firepit_4" -> "Warm Firepit"
                "decor_hammock_5" -> "Hammock"
                "decor_lanterns_6" -> "Glow Lanterns"
                "decor_kite_7" -> "Desert Kite"
                "decor_rangoli_mat_8" -> "Rangoli & Mango Tree"
                else -> "Cozy Decor"
            }

            GameDialog(
                visible = true,
                onDismissRequest = { onEvent(SanctuaryUiEvent.DismissDecorDialog) }
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
                            text = "Build $decorName ✨",
                            style = typography.title.copy(fontSize = 22.sp),
                            color = Ink,
                            textAlign = TextAlign.Center
                        )

                        // Preview Box
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Cream)
                                .border(2.dp, Color(0xFFE2D6C0), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            SanctuaryDecor(decorId = item.id, size = 95.dp, timeMs = clockMs)
                        }

                        Text(
                            text = "Cost: ${item.costBerries} 🫐 (You have: ${uiState.berries} 🫐)",
                            style = typography.body,
                            color = Ink.copy(alpha = 0.85f)
                        )

                        CandyButton(
                            text = "Build for ${item.costBerries} 🫐",
                            onClick = { onEvent(SanctuaryUiEvent.ConfirmBuyDecor(item.id)) },
                            variant = CandyButtonVariant.Success,
                            enabled = uiState.berries >= item.costBerries,
                            modifier = Modifier.fillMaxWidth()
                        )

                        CandyButton(
                            text = stringResource(R.string.action_cancel),
                            onClick = { onEvent(SanctuaryUiEvent.DismissDecorDialog) },
                            variant = CandyButtonVariant.Ghost,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Layer 1: Sky Parallax Gradient + Clouds
 */
@Composable
private fun SkyParallaxLayer(
    scrollOffsetPx: Float,
    totalWidth: androidx.compose.ui.unit.Dp,
    clockMs: Long
) {
    val parallaxShift = scrollOffsetPx * 0.35f

    Canvas(
        modifier = Modifier
            .width(totalWidth)
            .fillMaxHeight()
            .graphicsLayer { translationX = -parallaxShift }
    ) {
        val w = size.width
        val h = size.height

        // Dynamic 8-zone sky gradient spanning the entire panoramic length
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF80D0F5), // Zone 1
                    Color(0xFFE0C080), // Zone 2
                    Color(0xFFFFAA80), // Zone 3
                    Color(0xFF90A4AE), // Zone 4
                    Color(0xFF5C6BC0), // Zone 5
                    Color(0xFF3F51B5), // Zone 6
                    Color(0xFFFFB74D), // Zone 7
                    Color(0xFFAB47BC)  // Zone 8
                )
            ),
            size = Size(w, h * 0.70f)
        )

        // Floating fluffy clouds across the sky
        for (i in 0 until 16) {
            val cloudBaseX = (i * w / 16f)
            val drift = ((clockMs / 50.0 + i * 150) % (w / 16f)).toFloat()
            val cx = (cloudBaseX + drift) % w
            val cy = h * (0.08f + (i % 4) * 0.05f)

            drawCircle(
                color = WhitePure.copy(alpha = 0.45f),
                radius = 32f + (i % 3) * 10f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = WhitePure.copy(alpha = 0.55f),
                radius = 42f + (i % 3) * 8f,
                center = Offset(cx + 25f, cy - 6f)
            )
            drawCircle(
                color = WhitePure.copy(alpha = 0.45f),
                radius = 30f + (i % 3) * 10f,
                center = Offset(cx + 50f, cy)
            )
        }
    }
}

/**
 * Layer 2: Rolling Hills & Distant Silhouettes
 */
@Composable
private fun HillsParallaxLayer(
    scrollOffsetPx: Float,
    totalWidth: androidx.compose.ui.unit.Dp
) {
    val parallaxShift = scrollOffsetPx * 0.60f

    Canvas(
        modifier = Modifier
            .width(totalWidth)
            .fillMaxHeight()
            .graphicsLayer { translationX = -parallaxShift }
    ) {
        val w = size.width
        val h = size.height
        val groundLine = h * 0.58f

        // Distant gentle rolling hills
        val hillsPath = Path().apply {
            moveTo(0f, groundLine)
            var curX = 0f
            while (curX < w) {
                val nextX = curX + 220f
                val midX = (curX + nextX) / 2f
                val hillHeight = groundLine - 55f - (sin(curX / 300.0) * 35f).toFloat()
                quadraticTo(midX, hillHeight, nextX, groundLine)
                curX = nextX
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(
            path = hillsPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF6BBF59).copy(alpha = 0.70f), Color(0xFF4A9338).copy(alpha = 0.90f)),
                startY = groundLine - 90f,
                endY = groundLine
            )
        )
    }
}

/**
 * Layer 3: Zone Ground Section with Checkers, Flora, Signboard, Treat Basket & Decor
 */
@Composable
private fun ZoneGroundSection(
    zoneIndex: Int,
    isUnlocked: Boolean,
    decorItem: DecorItem?,
    clockMs: Long,
    onDecorClick: () -> Unit,
    onTreatBasketClick: () -> Unit,
    isTreatTimeAvailable: Boolean,
    totalRescued: Int,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography

    // Zone ground colors
    val groundColor = when (zoneIndex + 1) {
        1 -> Color(0xFF78C850) // Lush meadow
        2 -> Color(0xFF6B8E23) // Bog olive
        3 -> Color(0xFFD4A373) // Warm brass
        4 -> Color(0xFF73937E) // Ruins stone-moss
        5 -> Color(0xFF2D6A4F) // Forest deep teal
        6 -> Color(0xFF52616B) // Burrow slate
        7 -> Color(0xFFE9C46A) // Oasis warm sand
        8 -> Color(0xFF9B5DE5) // Celebration violet
        else -> Color(0xFF78C850)
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
    ) {
        // Ground base & checker grass
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            val w = size.width
            val h = size.height
            val groundTop = h * 0.58f

            // Ground base fill
            drawRect(
                color = if (isUnlocked) groundColor else groundColor.copy(alpha = 0.45f),
                topLeft = Offset(0f, groundTop),
                size = Size(w, h - groundTop)
            )

            // Top highlight stripe
            drawRect(
                color = WhitePure.copy(alpha = 0.25f),
                topLeft = Offset(0f, groundTop),
                size = Size(w, 8f)
            )

            // Gentle decorative checkers on ground
            val tileSize = 44f
            val rows = ((h - groundTop) / tileSize).toInt()
            val cols = (w / tileSize).toInt()
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if ((r + c) % 2 == 1) {
                        drawRect(
                            color = Color.Black.copy(alpha = 0.05f),
                            topLeft = Offset(c * tileSize, groundTop + 14f + r * tileSize),
                            size = Size(tileSize, tileSize)
                        )
                    }
                }
            }

            // Stepping stones pathway along the ground
            for (s in 0 until 5) {
                val stoneX = s * (w / 5f) + 30f
                val stoneY = groundTop + 90f + (s % 2) * 15f
                drawOval(
                    color = Color(0xFFDDD2C2),
                    topLeft = Offset(stoneX, stoneY),
                    size = Size(36f, 18f)
                )
            }
        }

        // ZONE 1 EXCLUSIVE: Welcome Signboard & Treat Basket
        if (zoneIndex == 0) {
            // Signboard at Entrance (x = 35dp)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 30.dp, y = 300.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
                    .background(Color(0xFF8B5A2B), RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFF5C3A1E), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🐾 Sanctuary",
                        style = typography.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        color = Cream
                    )
                    Text(
                        text = "Rescued: $totalRescued/240",
                        style = typography.caption.copy(fontSize = 11.sp),
                        color = WhitePure.copy(alpha = 0.90f)
                    )
                }
            }

            // Treat Picnic Basket at Entrance (x = 180dp)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 175.dp, y = 320.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTreatBasketClick() }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isTreatTimeAvailable) {
                        Text(
                            text = "✨ Tap!",
                            style = typography.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Coral
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .shadow(elevation = 4.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(if (isTreatTimeAvailable) Mango else Color(0xFFC7B198))
                            .border(2.dp, Color(0xFF8B5A2B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🧺", fontSize = 26.sp)
                    }
                }
            }
        }

        // FIXED DECORATION SLOT (Placed at x = 270dp in each zone)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 260.dp, y = 280.dp)
        ) {
            if (isUnlocked) {
                if (decorItem?.owned == true) {
                    // Render unlocked, owned active decoration
                    SanctuaryDecor(
                        decorId = decorItem.id,
                        size = 110.dp,
                        timeMs = clockMs
                    )
                } else if (decorItem != null) {
                    // Empty slot: show cute wooden build signboard
                    Box(
                        modifier = Modifier
                            .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Cream)
                            .border(2.dp, Color(0xFFD4C8B5), RoundedCornerShape(16.dp))
                            .clickable { onDecorClick() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "🔨 Build Slot",
                                style = typography.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                color = Ink
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Mango)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${decorItem.costBerries} 🫐",
                                    style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    color = Ink
                                )
                            }
                        }
                    }
                }
            } else {
                // Zone is locked
                Box(
                    modifier = Modifier
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Zone",
                            modifier = Modifier.size(16.dp),
                            tint = WhitePure
                        )
                        Text(
                            text = "Unlocks in World ${zoneIndex + 1}",
                            style = typography.caption.copy(fontSize = 12.sp),
                            color = WhitePure
                        )
                    }
                }
            }
        }
    }
}

/**
 * Roaming buddies drawn along the panoramic habitat ground
 */
@Composable
private fun BuddiesLayer(
    buddies: List<SanctuaryBuddyUiModel>,
    clockMs: Long,
    onBuddyTap: (String) -> Unit
) {
    val typography = ArrowBuddiesThemeTokens.typography

    for (buddy in buddies) {
        val groundBaseY = 370f + buddy.groundY
        val hopOffset = if (buddy.state == SanctuaryBuddyState.HOP || buddy.state == SanctuaryBuddyState.CHEER) {
            -18f * kotlin.math.abs(sin(clockMs / 120.0)).toFloat()
        } else 0f

        Box(
            modifier = Modifier
                .offset(x = buddy.x.dp, y = (groundBaseY + hopOffset).dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onBuddyTap(buddy.id) }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Heart Pop Particle
                if (buddy.heartPop) {
                    Text(
                        text = "❤️",
                        fontSize = 18.sp,
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = -12f
                            }
                    )
                }

                // Speech Bubble
                if (!buddy.bubbleText.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .shadow(elevation = 3.dp, shape = RoundedCornerShape(10.dp))
                            .background(WhitePure, RoundedCornerShape(10.dp))
                            .border(1.dp, Ink, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = buddy.bubbleText,
                            style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = Ink
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Buddy Avatar
                BuddyAvatar(
                    species = buddy.species,
                    direction = buddy.direction,
                    expression = when (buddy.state) {
                        SanctuaryBuddyState.HOP -> BuddyExpression.HAPPY
                        SanctuaryBuddyState.CHEER -> BuddyExpression.HAPPY
                        SanctuaryBuddyState.NAP -> BuddyExpression.SLEEPY
                        SanctuaryBuddyState.WALK -> BuddyExpression.READY
                        else -> BuddyExpression.IDLE
                    },
                    accessory = buddy.accessory,
                    size = 52.dp
                )
            }
        }
    }
}
