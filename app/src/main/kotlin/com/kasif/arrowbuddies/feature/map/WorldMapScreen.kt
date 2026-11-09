package com.kasif.arrowbuddies.feature.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.buddies.BuddyAvatar
import com.kasif.arrowbuddies.core.buddies.model.BuddyExpression
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.designsystem.component.CandyButton
import com.kasif.arrowbuddies.core.designsystem.component.CandyButtonVariant
import com.kasif.arrowbuddies.core.designsystem.component.GameDialog
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
import com.kasif.arrowbuddies.engine.model.Direction
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMapScreen(
    uiState: WorldMapUiState,
    onNavigateToGame: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // Node vertical spacing
    val nodeStepDp = 84.dp
    val nodeStepPx = with(density) { nodeStepDp.toPx() }

    // Auto-scroll to current level on launch
    LaunchedEffect(uiState.currentLevelIndex) {
        val targetScrollPx = ((uiState.currentLevelIndex - 1) * nodeStepPx).toInt()
        scrollState.animateScrollTo(targetScrollPx.coerceAtLeast(0))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseCurrentNode")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.screen_map),
                        style = typography.title,
                        color = Ink
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = Ink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream
                )
            )
        },
        containerColor = Cream
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val totalLevels = 240
            val totalHeightDp = (totalLevels * 84 + 200).dp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Canvas Path connecting all level nodes
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(totalHeightDp)
                ) {
                    val w = size.width
                    val path = Path()

                    for (i in 0 until totalLevels) {
                        val y = (i * nodeStepPx) + 60.dp.toPx()
                        // Sinusoidal winding path
                        val x = (w * 0.5f) + (sin(i * 0.65f) * (w * 0.28f))

                        if (i == 0) {
                            path.moveTo(x, y)
                        } else {
                            val prevY = ((i - 1) * nodeStepPx) + 60.dp.toPx()
                            val prevX = (w * 0.5f) + (sin((i - 1) * 0.65f) * (w * 0.28f))
                            val cY = (prevY + y) * 0.5f
                            path.cubicTo(prevX, cY, x, cY, x, y)
                        }
                    }

                    // Dotted playful trail
                    drawPath(
                        path = path,
                        color = Color(0xFFDECDB4),
                        style = Stroke(
                            width = 6.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
                        )
                    )
                }

                // Level Nodes & World Banners
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (world in 1..8) {
                        // World Section Banner
                        WorldBanner(
                            world = world,
                            worldName = WorldMapViewModel.getWorldName(world),
                            isUnlocked = uiState.highestUnlockedLevel >= ((world - 1) * 30 + 1)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        for (lvl in 1..30) {
                            val flatIndex = ((world - 1) * 30) + lvl
                            val levelId = "w$world-l${lvl.toString().padStart(2, '0')}"
                            val isCompleted = flatIndex < uiState.currentLevelIndex
                            val isCurrent = flatIndex == uiState.currentLevelIndex
                            val isLocked = flatIndex > uiState.highestUnlockedLevel
                            val isBigRescue = (lvl == 15 || lvl == 30)

                            // Horizontal sine wave offset for node placement
                            val sineVal = sin((flatIndex - 1) * 0.65f)
                            val xOffsetDp = (sineVal * 110f).dp

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(nodeStepDp),
                                contentAlignment = Alignment.Center
                            ) {
                                LevelNode(
                                    levelNumber = lvl,
                                    flatIndex = flatIndex,
                                    isCompleted = isCompleted,
                                    isCurrent = isCurrent,
                                    isLocked = isLocked,
                                    isBigRescue = isBigRescue,
                                    pulseScale = if (isCurrent) pulseScale else 1.0f,
                                    onClick = {
                                        if (!isLocked) {
                                            onNavigateToGame(levelId)
                                        }
                                    },
                                    modifier = Modifier.offset(x = xOffsetDp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(36.dp))
                    }
                }
            }

            // World Unlock Cinematic Gate Modal
            uiState.unlockedWorldGate?.let { gate ->
                GameDialog(
                    visible = true,
                    onDismissRequest = { /* Dismiss */ }
                ) {
                    ToyPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "🎉 World Unlocked!",
                                style = typography.title,
                                color = Ink,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Welcome to ${gate.worldName}!",
                                style = typography.subtitle,
                                color = Grape,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Discover 3 new animal friends trapped in this world:",
                                style = typography.body,
                                color = Ink.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )

                            // 3 Silhouette species preview
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                gate.speciesSilhouettes.forEach { speciesName ->
                                    val speciesEnum = runCatching { BuddySpecies.valueOf(speciesName) }.getOrDefault(BuddySpecies.BUNNY)
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE8E0F0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        BuddyAvatar(
                                            species = speciesEnum,
                                            direction = Direction.UP,
                                            expression = BuddyExpression.WORRIED,
                                            size = 56.dp,
                                            modifier = Modifier.graphicsLayer { alpha = 0.45f }
                                        )
                                    }
                                }
                            }

                            CandyButton(
                                text = "Let's Explore!",
                                onClick = { onNavigateToGame("w${gate.world}-l01") },
                                variant = CandyButtonVariant.Primary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorldBanner(
    world: Int,
    worldName: String,
    isUnlocked: Boolean
) {
    val typography = ArrowBuddiesThemeTokens.typography
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isUnlocked) Grape else Color.Gray)
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Text(
            text = "World $world · $worldName",
            style = typography.subtitle.copy(fontSize = 16.sp),
            color = WhitePure,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LevelNode(
    levelNumber: Int,
    flatIndex: Int,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLocked: Boolean,
    isBigRescue: Boolean,
    pulseScale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val nodeSize = if (isBigRescue) 64.dp else 52.dp

    Column(
        modifier = modifier.clickable(enabled = !isLocked, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Active Buddy standing on current node
        if (isCurrent) {
            BuddyAvatar(
                species = BuddySpecies.BUNNY,
                direction = Direction.UP,
                expression = BuddyExpression.HAPPY,
                size = 36.dp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(nodeSize)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
                .clip(CircleShape)
                .background(
                    when {
                        isCurrent -> Mango
                        isCompleted -> Mint
                        isLocked -> Color(0xFFD6D0DE)
                        else -> WhitePure
                    }
                )
                .border(
                    width = if (isBigRescue) 4.dp else 3.dp,
                    color = if (isBigRescue) Color(0xFFFFD700) else if (isCurrent) WhitePure else Ink,
                    shape = CircleShape
                )
                .shadow(elevation = if (isCurrent) 6.dp else 2.dp, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "$levelNumber",
                    style = typography.title.copy(
                        fontSize = if (isBigRescue) 22.sp else 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = if (isCurrent || isCompleted) WhitePure else Ink
                )
            }
        }

        // Stars under completed node
        if (isCompleted) {
            Row(
                modifier = Modifier.padding(top = 2.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Mango,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}
