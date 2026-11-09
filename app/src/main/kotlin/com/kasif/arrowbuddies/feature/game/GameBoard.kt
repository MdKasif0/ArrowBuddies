package com.kasif.arrowbuddies.feature.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.dp
import com.kasif.arrowbuddies.core.buddies.BuddyAvatar
import com.kasif.arrowbuddies.core.buddies.model.BuddyExpression
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.designsystem.component.ParticleBurst
import com.kasif.arrowbuddies.core.designsystem.component.ParticleSystemState
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure
import com.kasif.arrowbuddies.core.designsystem.theme.WorldTheme
import com.kasif.arrowbuddies.core.designsystem.theme.worldThemeFor
import com.kasif.arrowbuddies.engine.model.Buddy
import com.kasif.arrowbuddies.engine.model.Direction
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.Pos
import com.kasif.arrowbuddies.engine.model.Step
import com.kasif.arrowbuddies.engine.model.Tile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * High-performance 60fps playable game board.
 * - Layer (a): Cached Canvas with 3D toy tray, freedom glow, flowers, checkered tiles, and rocks.
 * - Layer (b): Animal buddies with graphicsLayer translation/squash animations (zero relayout).
 * - Layer (c): Particles and Juice (confetti, sparkles, dust puffs).
 */
@Composable
fun GameBoard(
    level: LevelDef,
    buddies: List<Buddy>,
    safeBuddyIds: Set<String>,
    isInputLocked: Boolean,
    pressedBuddyId: String?,
    activePeekingPath: PeekingPath?,
    effects: SharedFlow<GameEffect>,
    onBuddyTap: (String) -> Unit,
    onBuddyPressed: (String) -> Unit,
    onBuddyReleased: () -> Unit,
    onPathPeekStart: (String) -> Unit,
    onPathPeekEnd: () -> Unit,
    onRescuedBump: () -> Unit,
    modifier: Modifier = Modifier,
    particleSystemState: ParticleSystemState = remember { ParticleSystemState() }
) {
    val density = LocalDensity.current
    val viewConfig = LocalViewConfiguration.current
    val worldTheme = remember(level.world) { worldThemeFor(level.world) }

    // Screen shake animatable (4dp, 180ms)
    val screenShakeX = remember { Animatable(0f) }
    val screenShakeY = remember { Animatable(0f) }

    // Active animation state per buddy
    var activeEscapeBuddy by remember { mutableStateOf<EscapeAnimState?>(null) }
    var activeBonkBuddy by remember { mutableStateOf<BonkAnimState?>(null) }
    val blockerJiggles = remember { mutableStateMapOf<Pos, Offset>() }

    // Collect one-shot game effects
    LaunchedEffect(effects) {
        effects.collectLatest { effect ->
            when (effect) {
                is GameEffect.PlayEscape -> {
                    val startX = effect.buddy.pos.x.toFloat()
                    val startY = effect.buddy.pos.y.toFloat()
                    val animState = EscapeAnimState(
                        buddy = effect.buddy,
                        startX = startX,
                        startY = startY,
                        path = effect.path,
                        exitSide = effect.exitSide
                    )
                    activeEscapeBuddy = animState
                }

                is GameEffect.PlayBonk -> {
                    val startX = effect.buddy.pos.x.toFloat()
                    val startY = effect.buddy.pos.y.toFloat()
                    val animState = BonkAnimState(
                        buddy = effect.buddy,
                        startX = startX,
                        startY = startY,
                        path = effect.path,
                        blockerPos = effect.blockerPos
                    )
                    activeBonkBuddy = animState
                }

                is GameEffect.ScreenShake -> {
                    launch {
                        val duration = effect.durationMs
                        val start = System.currentTimeMillis()
                        while (System.currentTimeMillis() - start < duration) {
                            val elapsed = (System.currentTimeMillis() - start).toFloat() / duration
                            val decay = (1f - elapsed).coerceAtLeast(0f)
                            val amp = effect.intensityDp * decay
                            screenShakeX.snapTo((Math.random().toFloat() * 2f - 1f) * amp)
                            screenShakeY.snapTo((Math.random().toFloat() * 2f - 1f) * amp)
                            delay(20)
                        }
                        screenShakeX.snapTo(0f)
                        screenShakeY.snapTo(0f)
                    }
                }

                else -> Unit
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = screenShakeX.value * density.density
                translationY = screenShakeY.value * density.density
            },
        contentAlignment = Alignment.Center
    ) {
        val maxBoardWidthDp = 560.dp
        val rimPaddingDp = 16.dp
        val rimPaddingPx = with(density) { rimPaddingDp.toPx() }

        val availableWidthDp = (maxWidth.coerceAtMost(maxBoardWidthDp) - rimPaddingDp * 2).coerceAtLeast(100.dp)
        val availableHeightDp = (maxHeight - rimPaddingDp * 2).coerceAtLeast(100.dp)

        val cellSizeDp = minOf(
            availableWidthDp / level.cols,
            availableHeightDp / level.rows
        )
        val cellSizePx = with(density) { cellSizeDp.toPx() }

        val boardWidthDp = cellSizeDp * level.cols
        val boardHeightDp = cellSizeDp * level.rows
        val boardWidthPx = with(density) { boardWidthDp.toPx() }
        val boardHeightPx = with(density) { boardHeightDp.toPx() }

        val trayWidthDp = boardWidthDp + rimPaddingDp * 2
        val trayHeightDp = boardHeightDp + rimPaddingDp * 2

        // Drive Active Escape Animation
        LaunchedEffect(activeEscapeBuddy) {
            val esc = activeEscapeBuddy ?: return@LaunchedEffect
            val path = esc.path
            val totalCells = path.size
            val dashMs = (totalCells * 55L).coerceAtMost(450L)

            // 1. Anticipation squash back (70ms)
            esc.expression.value = BuddyExpression.READY
            when (esc.buddy.dir) {
                Direction.UP -> { esc.scaleY.animateTo(0.88f, tween(70)); esc.animY.animateTo(esc.startY + 0.12f, tween(70)) }
                Direction.DOWN -> { esc.scaleY.animateTo(0.88f, tween(70)); esc.animY.animateTo(esc.startY - 0.12f, tween(70)) }
                Direction.LEFT -> { esc.scaleX.animateTo(0.88f, tween(70)); esc.animX.animateTo(esc.startX + 0.12f, tween(70)) }
                Direction.RIGHT -> { esc.scaleX.animateTo(0.88f, tween(70)); esc.animX.animateTo(esc.startX - 0.12f, tween(70)) }
            }

            // 2. Dash along steps
            esc.expression.value = BuddyExpression.DASHING
            esc.scaleX.snapTo(1f)
            esc.scaleY.snapTo(1f)

            val perStepMs = (dashMs / totalCells.coerceAtLeast(1)).toInt().coerceIn(35, 75)
            for (step in path) {
                val targetX = step.pos.x.toFloat()
                val targetY = step.pos.y.toFloat()
                launch { esc.animX.animateTo(targetX, tween(perStepMs, easing = LinearEasing)) }
                esc.animY.animateTo(targetY, tween(perStepMs, easing = LinearEasing))

                // Spawn dust puff at buddy tail
                val puffX = rimPaddingPx + (targetX + 0.5f) * cellSizePx
                val puffY = rimPaddingPx + (targetY + 0.5f) * cellSizePx
                particleSystemState.spawnDustPuff(puffX, puffY, count = 6)
            }

            // Dash outside board edge
            val edgeX = esc.animX.value + esc.buddy.dir.dx * 0.9f
            val edgeY = esc.animY.value + esc.buddy.dir.dy * 0.9f
            launch { esc.animX.animateTo(edgeX, tween(80, easing = LinearEasing)) }
            esc.animY.animateTo(edgeY, tween(80, easing = LinearEasing))

            // 3. Leaves board edge -> Arcs up toward HUD rescued counter (320ms)
            esc.expression.value = BuddyExpression.HAPPY
            val hudTargetX = boardWidthPx * 0.85f / cellSizePx
            val hudTargetY = -1.5f
            launch { esc.scaleX.animateTo(0.4f, tween(320, easing = FastOutSlowInEasing)) }
            launch { esc.scaleY.animateTo(0.4f, tween(320, easing = FastOutSlowInEasing)) }
            launch { esc.alpha.animateTo(0f, tween(320, easing = FastOutLinearInEasing)) }
            launch { esc.animX.animateTo(hudTargetX, tween(320, easing = FastOutSlowInEasing)) }
            esc.animY.animateTo(hudTargetY, tween(320, easing = FastOutSlowInEasing))

            onRescuedBump()
            particleSystemState.spawnHeartPop(hudTargetX * cellSizePx + rimPaddingPx, 20f, count = 12)
            activeEscapeBuddy = null
        }

        // Drive Active Bonk Animation
        LaunchedEffect(activeBonkBuddy) {
            val bonk = activeBonkBuddy ?: return@LaunchedEffect
            val path = bonk.path
            val totalCells = path.size
            val dashMs = (totalCells * 45L).coerceAtMost(250L)

            // 1. Dash toward blocker
            bonk.expression.value = BuddyExpression.DASHING
            val targetX = bonk.blockerPos.x.toFloat() - bonk.buddy.dir.dx * 0.82f
            val targetY = bonk.blockerPos.y.toFloat() - bonk.buddy.dir.dy * 0.82f

            launch { bonk.animX.animateTo(targetX, tween(dashMs.toInt(), easing = LinearEasing)) }
            bonk.animY.animateTo(targetY, tween(dashMs.toInt(), easing = LinearEasing))

            // 2. Squash flat on impact
            bonk.expression.value = BuddyExpression.BONKED
            when (bonk.buddy.dir) {
                Direction.UP, Direction.DOWN -> {
                    launch { bonk.scaleY.animateTo(0.65f, tween(50)) }
                    bonk.scaleX.animateTo(1.28f, tween(50))
                }
                Direction.LEFT, Direction.RIGHT -> {
                    launch { bonk.scaleX.animateTo(0.65f, tween(50)) }
                    bonk.scaleY.animateTo(1.28f, tween(50))
                }
            }

            // Blocker jiggle
            launch {
                val jiggleOffset = 6f
                blockerJiggles[bonk.blockerPos] = Offset(bonk.buddy.dir.dx * jiggleOffset, bonk.buddy.dir.dy * jiggleOffset)
                delay(60)
                blockerJiggles[bonk.blockerPos] = Offset(-bonk.buddy.dir.dx * (jiggleOffset * 0.5f), -bonk.buddy.dir.dy * (jiggleOffset * 0.5f))
                delay(60)
                blockerJiggles.remove(bonk.blockerPos)
            }

            // 3. Spring back to start cell with swirly eyes
            launch { bonk.scaleX.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)) }
            launch { bonk.scaleY.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)) }
            launch { bonk.animX.animateTo(bonk.startX, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium)) }
            bonk.animY.animateTo(bonk.startY, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium))

            delay(150)
            activeBonkBuddy = null
        }

        Box(
            modifier = Modifier
                .size(trayWidthDp, trayHeightDp)
                .pointerInput(buddies, isInputLocked) {
                    if (isInputLocked) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val buddy = findBuddyAt(
                            touchOffset = down.position,
                            cellSizePx = cellSizePx,
                            rimPaddingPx = rimPaddingPx,
                            buddies = buddies
                        )
                        if (buddy != null) {
                            onBuddyPressed(buddy.id)
                            val longPressTimeout = viewConfig.longPressTimeoutMillis
                            val upOrCancel = withTimeoutOrNull(longPressTimeout) {
                                waitForUpOrCancellation()
                            }
                            if (upOrCancel != null) {
                                // Tap!
                                onBuddyReleased()
                                onBuddyTap(buddy.id)
                            } else {
                                // Held past timeout -> Path Peek!
                                onPathPeekStart(buddy.id)
                                waitForUpOrCancellation()
                                onPathPeekEnd()
                                onBuddyReleased()
                            }
                        }
                    }
                }
        ) {
            // LAYER (A): 3D Toy Tray + Checkered Tiles + Rocks (drawWithCache for 60fps)
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        onDrawBehind {
                            drawToyTray(
                                trayWidth = size.width,
                                trayHeight = size.height,
                                rimPadding = rimPaddingPx,
                                cols = level.cols,
                                rows = level.rows,
                                cellSize = cellSizePx,
                                worldTheme = worldTheme,
                                level = level
                            )
                        }
                    }
            )

            // LAYER (B): Buddies Container (graphicsLayer translations, zero relayout)
            Box(
                modifier = Modifier
                    .size(boardWidthDp, boardHeightDp)
                    .offset(rimPaddingDp, rimPaddingDp)
            ) {
                for (buddy in buddies) {
                    val isEscaping = activeEscapeBuddy?.buddy?.id == buddy.id
                    val isBonking = activeBonkBuddy?.buddy?.id == buddy.id
                    val isPressed = pressedBuddyId == buddy.id

                    val currentExpression = when {
                        isEscaping -> activeEscapeBuddy?.expression?.value ?: BuddyExpression.HAPPY
                        isBonking -> activeBonkBuddy?.expression?.value ?: BuddyExpression.BONKED
                        isPressed -> BuddyExpression.READY
                        safeBuddyIds.contains(buddy.id) -> BuddyExpression.IDLE
                        else -> BuddyExpression.WORRIED
                    }

                    val species = remember(buddy.species) {
                        runCatching { BuddySpecies.valueOf(buddy.species.uppercase()) }.getOrElse {
                            BuddySpecies.entries.firstOrNull { it.id.equals(buddy.species, ignoreCase = true) }
                                ?: BuddySpecies.BUNNY
                        }
                    }

                    val currentAnimX: Float
                    val currentAnimY: Float
                    val currentScaleX: Float
                    val currentScaleY: Float
                    val currentAlpha: Float

                    when {
                        isEscaping -> {
                            val esc = activeEscapeBuddy!!
                            currentAnimX = esc.animX.value
                            currentAnimY = esc.animY.value
                            currentScaleX = esc.scaleX.value
                            currentScaleY = esc.scaleY.value
                            currentAlpha = esc.alpha.value
                        }
                        isBonking -> {
                            val bonk = activeBonkBuddy!!
                            currentAnimX = bonk.animX.value
                            currentAnimY = bonk.animY.value
                            currentScaleX = bonk.scaleX.value
                            currentScaleY = bonk.scaleY.value
                            currentAlpha = 1f
                        }
                        else -> {
                            val jiggle = blockerJiggles[buddy.pos] ?: Offset.Zero
                            currentAnimX = buddy.pos.x.toFloat() + (jiggle.x / cellSizePx)
                            currentAnimY = buddy.pos.y.toFloat() + (jiggle.y / cellSizePx)
                            currentScaleX = if (isPressed) 1.05f else 1f
                            currentScaleY = if (isPressed) 0.92f else 1f
                            currentAlpha = 1f
                        }
                    }

                    BuddyAvatar(
                        species = species,
                        direction = buddy.dir,
                        expression = currentExpression,
                        size = cellSizeDp,
                        modifier = Modifier
                            .size(cellSizeDp)
                            .graphicsLayer {
                                translationX = currentAnimX * cellSizePx
                                translationY = currentAnimY * cellSizePx
                                scaleX = currentScaleX
                                scaleY = currentScaleY
                                alpha = currentAlpha
                            }
                    )
                }
            }

            // Path Peek Overlay
            if (activePeekingPath != null) {
                PathPeekOverlay(
                    peekingPath = activePeekingPath,
                    cellSizePx = cellSizePx,
                    rimPaddingPx = rimPaddingPx
                )
            }

            // LAYER (C): Particles overlay (Zero per-frame heap allocations)
            ParticleBurst(
                state = particleSystemState,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Mutable state holding escape dash & arc animatables.
 */
private class EscapeAnimState(
    val buddy: Buddy,
    val startX: Float,
    val startY: Float,
    val path: List<Step>,
    val exitSide: Direction
) {
    val animX = Animatable(startX)
    val animY = Animatable(startY)
    val scaleX = Animatable(1f)
    val scaleY = Animatable(1f)
    val alpha = Animatable(1f)
    val expression = mutableStateOf(BuddyExpression.READY)
}

/**
 * Mutable state holding bonk dash, squash, & springback animatables.
 */
private class BonkAnimState(
    val buddy: Buddy,
    val startX: Float,
    val startY: Float,
    val path: List<Step>,
    val blockerPos: Pos
) {
    val animX = Animatable(startX)
    val animY = Animatable(startY)
    val scaleX = Animatable(1f)
    val scaleY = Animatable(1f)
    val expression = mutableStateOf(BuddyExpression.DASHING)
}

/**
 * Find candidate buddy matching touch location with 30% cell edge tolerance.
 */
private fun findBuddyAt(
    touchOffset: Offset,
    cellSizePx: Float,
    rimPaddingPx: Float,
    buddies: List<Buddy>
): Buddy? {
    val relX = touchOffset.x - rimPaddingPx
    val relY = touchOffset.y - rimPaddingPx
    if (relX < -cellSizePx * 0.3f || relY < -cellSizePx * 0.3f) return null

    val rawCol = relX / cellSizePx
    val rawRow = relY / cellSizePx
    val col = rawCol.toInt()
    val row = rawRow.toInt()

    // 1. Direct cell match
    val direct = buddies.firstOrNull { it.pos.x == col && it.pos.y == row }
    if (direct != null) return direct

    // 2. Tolerance match (within 30% of a cell to nearest buddy center)
    var bestBuddy: Buddy? = null
    var minDistanceSq = Float.MAX_VALUE
    val maxRadius = 0.5f + 0.30f
    val maxRadiusSq = maxRadius * maxRadius

    for (b in buddies) {
        val cx = b.pos.x + 0.5f
        val cy = b.pos.y + 0.5f
        val dx = rawCol - cx
        val dy = rawRow - cy
        val distSq = dx * dx + dy * dy
        if (distSq <= maxRadiusSq && distSq < minDistanceSq) {
            minDistanceSq = distSq
            bestBuddy = b
        }
    }
    return bestBuddy
}

/**
 * Procedural rendering of the 3D toy tray, checkered tiles, rocks, and freedom flowers.
 */
private fun DrawScope.drawToyTray(
    trayWidth: Float,
    trayHeight: Float,
    rimPadding: Float,
    cols: Int,
    rows: Int,
    cellSize: Float,
    worldTheme: WorldTheme,
    level: LevelDef
) {
    val cornerRadius = 24f

    // 1. Freedom glow outside rim
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(worldTheme.accent.main.copy(alpha = 0.28f), Color.Transparent),
            center = Offset(trayWidth * 0.5f, trayHeight * 0.5f),
            radius = maxOf(trayWidth, trayHeight) * 0.6f
        ),
        topLeft = Offset(-12f, -12f),
        size = Size(trayWidth + 24f, trayHeight + 24f),
        cornerRadius = CornerRadius(cornerRadius + 8f, cornerRadius + 8f)
    )

    // 2. Tiny decorative freedom flowers outside rim
    drawFreedomFlowers(trayWidth, trayHeight, rimPadding, worldTheme.accent.main)

    // 3. Tray 3D raised rim shadow / lip
    val lipDepth = 6f
    drawRoundRect(
        color = Color(0x3D2B2240),
        topLeft = Offset(0f, lipDepth),
        size = Size(trayWidth, trayHeight),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // 4. Tray base surface
    drawRoundRect(
        color = worldTheme.boardBase,
        topLeft = Offset.Zero,
        size = Size(trayWidth, trayHeight),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // 5. Tray top specular highlight arc
    drawRoundRect(
        color = WhitePure.copy(alpha = 0.35f),
        topLeft = Offset(2f, 2f),
        size = Size(trayWidth - 4f, 3f),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // 6. Inner shadow border around board well
    drawRoundRect(
        color = Color(0x222B2240),
        topLeft = Offset(rimPadding - 2f, rimPadding - 2f),
        size = Size(cols * cellSize + 4f, rows * cellSize + 4f),
        cornerRadius = CornerRadius(14f, 14f)
    )

    // 7. Checkered grass tiles
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val tileLeft = rimPadding + c * cellSize
            val tileTop = rimPadding + r * cellSize
            val tileColor = if ((c + r) % 2 == 0) worldTheme.tileCheckerA else worldTheme.tileCheckerB

            drawRoundRect(
                color = tileColor,
                topLeft = Offset(tileLeft + 1f, tileTop + 1f),
                size = Size(cellSize - 2f, cellSize - 2f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Draw rock obstacle
            if (level.tileAt(Pos(c, r)) is Tile.Rock) {
                drawTactileRock(
                    cx = tileLeft + cellSize * 0.5f,
                    cy = tileTop + cellSize * 0.5f,
                    radius = cellSize * 0.38f,
                    accent = worldTheme.accent.main
                )
            }
        }
    }
}

/**
 * 3D vector stone obstacle with contact shadow, highlight arc, and moss flecks.
 */
private fun DrawScope.drawTactileRock(cx: Float, cy: Float, radius: Float, accent: Color) {
    // 1. Contact shadow
    drawOval(
        color = Color(0x402B2240),
        topLeft = Offset(cx - radius * 0.95f, cy + radius * 0.35f),
        size = Size(radius * 1.9f, radius * 0.75f)
    )

    // 2. 3D Bottom darker contour / lip
    val rockPath = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = cx - radius * 0.88f,
                top = cy - radius * 0.78f + 3f,
                right = cx + radius * 0.88f,
                bottom = cy + radius * 0.88f + 3f,
                radiusX = radius * 0.45f,
                radiusY = radius * 0.45f
            )
        )
    }
    drawPath(rockPath, color = Color(0xFF475263))

    // 3. Stone body
    val bodyPath = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = cx - radius * 0.88f,
                top = cy - radius * 0.78f,
                right = cx + radius * 0.88f,
                bottom = cy + radius * 0.88f,
                radiusX = radius * 0.45f,
                radiusY = radius * 0.45f
            )
        )
    }
    drawPath(
        bodyPath,
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF8B9BB4), Color(0xFF5D6B82)),
            center = Offset(cx - radius * 0.25f, cy - radius * 0.25f),
            radius = radius * 1.2f
        )
    )

    // 4. Specular top highlight arc
    drawArc(
        color = WhitePure.copy(alpha = 0.45f),
        startAngle = 200f,
        sweepAngle = 100f,
        useCenter = false,
        topLeft = Offset(cx - radius * 0.7f, cy - radius * 0.65f),
        size = Size(radius * 1.4f, radius * 0.8f),
        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
    )

    // 5. Cute biome moss flecks
    drawCircle(
        color = accent.copy(alpha = 0.85f),
        radius = radius * 0.14f,
        center = Offset(cx - radius * 0.32f, cy + radius * 0.22f)
    )
    drawCircle(
        color = accent.copy(alpha = 0.85f),
        radius = radius * 0.10f,
        center = Offset(cx - radius * 0.15f, cy + radius * 0.32f)
    )
}

/**
 * Tiny freedom flowers drawn outside the rim.
 */
private fun DrawScope.drawFreedomFlowers(trayWidth: Float, trayHeight: Float, rimPadding: Float, accent: Color) {
    val flowerPositions = listOf(
        Offset(trayWidth * 0.25f, 7f),
        Offset(trayWidth * 0.75f, 7f),
        Offset(trayWidth * 0.2f, trayHeight - 7f),
        Offset(trayWidth * 0.8f, trayHeight - 7f),
        Offset(7f, trayHeight * 0.35f),
        Offset(7f, trayHeight * 0.65f),
        Offset(trayWidth - 7f, trayHeight * 0.35f),
        Offset(trayWidth - 7f, trayHeight * 0.65f)
    )

    for (pos in flowerPositions) {
        val petalRadius = 2.8f
        val petalColor = WhitePure
        val centerColor = Color(0xFFFFD54F)

        drawCircle(petalColor, petalRadius, Offset(pos.x - petalRadius, pos.y))
        drawCircle(petalColor, petalRadius, Offset(pos.x + petalRadius, pos.y))
        drawCircle(petalColor, petalRadius, Offset(pos.x, pos.y - petalRadius))
        drawCircle(petalColor, petalRadius, Offset(pos.x, pos.y + petalRadius))
        drawCircle(centerColor, petalRadius * 0.9f, pos)
    }
}
