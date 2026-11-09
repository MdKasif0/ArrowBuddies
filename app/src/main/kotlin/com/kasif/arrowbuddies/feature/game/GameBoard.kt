package com.kasif.arrowbuddies.feature.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.buddies.BuddyAvatar
import com.kasif.arrowbuddies.core.buddies.model.BuddyExpression
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.designsystem.component.ParticleBurst
import com.kasif.arrowbuddies.core.designsystem.component.ParticleSystemState
import com.kasif.arrowbuddies.core.designsystem.component.SpeechBubble
import com.kasif.arrowbuddies.core.designsystem.component.TailPosition
import com.kasif.arrowbuddies.core.designsystem.theme.Mango
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure
import com.kasif.arrowbuddies.core.designsystem.theme.WorldTheme
import com.kasif.arrowbuddies.core.designsystem.theme.worldThemeFor
import com.kasif.arrowbuddies.engine.model.Buddy
import com.kasif.arrowbuddies.engine.model.Direction
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.PeekingPath
import com.kasif.arrowbuddies.engine.model.Pos
import com.kasif.arrowbuddies.engine.model.RefusalReason
import com.kasif.arrowbuddies.engine.model.Step
import com.kasif.arrowbuddies.engine.model.StepEvent
import com.kasif.arrowbuddies.engine.model.Tile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance 60fps playable game board with tactile obstacles and mechanics.
 * - Layer (a): Cached Canvas with 3D toy tray, freedom glow, checkered tiles, and tactile obstacles (Rock, Mud, Turntable, Portal).
 * - Layer (b): Animal buddies with graphicsLayer translation/squash animations, cage bars, key ribbons, and nightcaps.
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
    particleSystemState: ParticleSystemState = remember { ParticleSystemState() },
    hintBuddyId: String? = null,
    tutorialHandPos: Pos? = null,
    tutorialSpeechBubble: String? = null
) {
    val density = LocalDensity.current
    val viewConfig = LocalViewConfiguration.current
    val worldTheme = remember(level.world) { worldThemeFor(level.world) }

    // Screen shake animatables
    val screenShakeX = remember { Animatable(0f) }
    val screenShakeY = remember { Animatable(0f) }

    // Active animation state per buddy
    var activeEscapeBuddy by remember { mutableStateOf<EscapeAnimState?>(null) }
    var activeBonkBuddy by remember { mutableStateOf<BonkAnimState?>(null) }
    var activeMudStopBuddy by remember { mutableStateOf<MudStopAnimState?>(null) }
    val blockerJiggles = remember { mutableStateMapOf<Pos, Offset>() }

    // Active refusal speech bubbles and shakes
    var activeRefusal by remember { mutableStateOf<ActiveRefusal?>(null) }
    val refusalShakes = remember { mutableStateMapOf<String, Float>() }
    val awakenedBuddyIds = remember { mutableStateMapOf<String, Long>() }

    // Collect one-shot game effects
    LaunchedEffect(effects) {
        effects.collectLatest { effect ->
            when (effect) {
                is GameEffect.PlayEscape -> {
                    val startX = effect.buddy.pos.x.toFloat()
                    val startY = effect.buddy.pos.y.toFloat()
                    activeEscapeBuddy = EscapeAnimState(
                        buddy = effect.buddy,
                        startX = startX,
                        startY = startY,
                        path = effect.path,
                        exitSide = effect.exitSide
                    )
                }

                is GameEffect.PlayBonk -> {
                    val startX = effect.buddy.pos.x.toFloat()
                    val startY = effect.buddy.pos.y.toFloat()
                    activeBonkBuddy = BonkAnimState(
                        buddy = effect.buddy,
                        startX = startX,
                        startY = startY,
                        path = effect.path,
                        blockerPos = effect.blockerPos
                    )
                }

                is GameEffect.PlayMudStop -> {
                    val startX = effect.buddy.pos.x.toFloat()
                    val startY = effect.buddy.pos.y.toFloat()
                    activeMudStopBuddy = MudStopAnimState(
                        buddy = effect.buddy,
                        startX = startX,
                        startY = startY,
                        path = effect.path,
                        mudPos = effect.mudPos
                    )
                }

                is GameEffect.PlayRefusal -> {
                    launch {
                        activeRefusal = ActiveRefusal(effect.buddyId, effect.reason, effect.buddyPos)
                        refusalShakes[effect.buddyId] = -6f
                        delay(40)
                        refusalShakes[effect.buddyId] = 6f
                        delay(40)
                        refusalShakes[effect.buddyId] = -4f
                        delay(40)
                        refusalShakes[effect.buddyId] = 4f
                        delay(40)
                        refusalShakes.remove(effect.buddyId)
                        delay(1200)
                        if (activeRefusal?.buddyId == effect.buddyId) {
                            activeRefusal = null
                        }
                    }
                }

                is GameEffect.PlayKeyUnlock -> {
                    launch {
                        delay(150)
                        for (targetPos in effect.targetPositions) {
                            val targetPxX = (targetPos.x + 0.5f) * 60f
                            val targetPxY = (targetPos.y + 0.5f) * 60f
                            particleSystemState.spawnSparkle(targetPxX, targetPxY, count = 16)
                        }
                    }
                }

                is GameEffect.PlayWakeUp -> {
                    launch {
                        awakenedBuddyIds[effect.buddyId] = System.currentTimeMillis()
                        delay(450)
                        awakenedBuddyIds.remove(effect.buddyId)
                    }
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

                if (step.event == StepEvent.TURN) {
                    esc.currentDir.value = step.dirAfter
                }

                if (step.event == StepEvent.TELEPORT) {
                    launch { esc.scaleX.animateTo(0.1f, tween(50)) }
                    esc.scaleY.animateTo(0.1f, tween(50))
                    esc.animX.snapTo(targetX)
                    esc.animY.snapTo(targetY)
                    val portalPuffX = rimPaddingPx + (targetX + 0.5f) * cellSizePx
                    val portalPuffY = rimPaddingPx + (targetY + 0.5f) * cellSizePx
                    particleSystemState.spawnSparkle(portalPuffX, portalPuffY, count = 10)
                    launch { esc.scaleX.animateTo(1f, tween(60)) }
                    esc.scaleY.animateTo(1f, tween(60))
                } else {
                    launch { esc.animX.animateTo(targetX, tween(perStepMs, easing = LinearEasing)) }
                    esc.animY.animateTo(targetY, tween(perStepMs, easing = LinearEasing))

                    val puffX = rimPaddingPx + (targetX + 0.5f) * cellSizePx
                    val puffY = rimPaddingPx + (targetY + 0.5f) * cellSizePx
                    particleSystemState.spawnDustPuff(puffX, puffY, count = 6)
                }
            }

            // Dash outside board edge
            val lastDir = esc.currentDir.value
            val edgeX = esc.animX.value + lastDir.dx * 0.9f
            val edgeY = esc.animY.value + lastDir.dy * 0.9f
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

        // Drive Active Mud Stop Animation
        LaunchedEffect(activeMudStopBuddy) {
            val mudStop = activeMudStopBuddy ?: return@LaunchedEffect
            val path = mudStop.path
            val totalCells = path.size
            val dashMs = (totalCells * 55L).coerceAtMost(350L)

            mudStop.expression.value = BuddyExpression.DASHING
            val perStepMs = (dashMs / totalCells.coerceAtLeast(1)).toInt().coerceIn(35, 75)

            for (step in path) {
                val targetX = step.pos.x.toFloat()
                val targetY = step.pos.y.toFloat()

                if (step.event == StepEvent.TURN) {
                    mudStop.currentDir.value = step.dirAfter
                }

                if (step.event == StepEvent.TELEPORT) {
                    launch { mudStop.scaleX.animateTo(0.1f, tween(50)) }
                    mudStop.scaleY.animateTo(0.1f, tween(50))
                    mudStop.animX.snapTo(targetX)
                    mudStop.animY.snapTo(targetY)
                    val portalPuffX = rimPaddingPx + (targetX + 0.5f) * cellSizePx
                    val portalPuffY = rimPaddingPx + (targetY + 0.5f) * cellSizePx
                    particleSystemState.spawnSparkle(portalPuffX, portalPuffY, count = 8)
                    launch { mudStop.scaleX.animateTo(1f, tween(60)) }
                    mudStop.scaleY.animateTo(1f, tween(60))
                } else {
                    launch { mudStop.animX.animateTo(targetX, tween(perStepMs, easing = LinearEasing)) }
                    mudStop.animY.animateTo(targetY, tween(perStepMs, easing = LinearEasing))

                    val puffX = rimPaddingPx + (targetX + 0.5f) * cellSizePx
                    val puffY = rimPaddingPx + (targetY + 0.5f) * cellSizePx
                    particleSystemState.spawnDustPuff(puffX, puffY, count = 4)
                }
            }

            // Arrived on mud: splash & sink
            mudStop.expression.value = BuddyExpression.IDLE
            val mudPuffX = rimPaddingPx + (mudStop.mudPos.x + 0.5f) * cellSizePx
            val mudPuffY = rimPaddingPx + (mudStop.mudPos.y + 0.5f) * cellSizePx
            particleSystemState.spawnDustPuff(mudPuffX, mudPuffY, count = 12)
            activeMudStopBuddy = null
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
            // LAYER (A): 3D Toy Tray + Checkered Tiles + Tactile Obstacles (drawWithCache for 60fps)
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
                    val isMudStopping = activeMudStopBuddy?.buddy?.id == buddy.id
                    val isPressed = pressedBuddyId == buddy.id
                    val isWakingUp = awakenedBuddyIds.containsKey(buddy.id)
                    val isOnMud = level.tileAt(buddy.pos) is Tile.Mud

                    val currentExpression = when {
                        isEscaping -> activeEscapeBuddy?.expression?.value ?: BuddyExpression.HAPPY
                        isBonking -> activeBonkBuddy?.expression?.value ?: BuddyExpression.BONKED
                        isMudStopping -> activeMudStopBuddy?.expression?.value ?: BuddyExpression.IDLE
                        isPressed -> BuddyExpression.READY
                        buddy.cageColor != null -> BuddyExpression.CAGED
                        buddy.sleepy && !isWakingUp -> BuddyExpression.SLEEPY
                        isWakingUp -> BuddyExpression.READY
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
                    val currentDir: Direction

                    when {
                        isEscaping -> {
                            val esc = activeEscapeBuddy!!
                            currentAnimX = esc.animX.value
                            currentAnimY = esc.animY.value
                            currentScaleX = esc.scaleX.value
                            currentScaleY = esc.scaleY.value
                            currentAlpha = esc.alpha.value
                            currentDir = esc.currentDir.value
                        }
                        isBonking -> {
                            val bonk = activeBonkBuddy!!
                            currentAnimX = bonk.animX.value
                            currentAnimY = bonk.animY.value
                            currentScaleX = bonk.scaleX.value
                            currentScaleY = bonk.scaleY.value
                            currentAlpha = 1f
                            currentDir = bonk.currentDir.value
                        }
                        isMudStopping -> {
                            val mudStop = activeMudStopBuddy!!
                            currentAnimX = mudStop.animX.value
                            currentAnimY = mudStop.animY.value
                            currentScaleX = mudStop.scaleX.value
                            currentScaleY = mudStop.scaleY.value
                            currentAlpha = 1f
                            currentDir = mudStop.currentDir.value
                        }
                        else -> {
                            val jiggle = blockerJiggles[buddy.pos] ?: Offset.Zero
                            val refusalShake = refusalShakes[buddy.id] ?: 0f
                            currentAnimX = buddy.pos.x.toFloat() + ((jiggle.x + refusalShake) / cellSizePx)
                            currentAnimY = buddy.pos.y.toFloat() + (jiggle.y / cellSizePx)
                            currentScaleX = if (isPressed) 1.05f else 1f
                            currentScaleY = if (isPressed) 0.92f else 1f
                            currentAlpha = 1f
                            currentDir = buddy.dir
                        }
                    }

                    val isHinted = buddy.id == hintBuddyId
                    Box(
                        modifier = Modifier
                            .size(cellSizeDp)
                            .graphicsLayer {
                                translationX = currentAnimX * cellSizePx
                                translationY = currentAnimY * cellSizePx + (if (isOnMud && !isEscaping) with(density) { 3.dp.toPx() } else 0f)
                                scaleX = currentScaleX * (if (isWakingUp) 1.15f else 1f)
                                scaleY = currentScaleY * (if (isWakingUp) 1.15f else 1f)
                                alpha = currentAlpha
                            }
                    ) {
                        if (isHinted) {
                            val hintPulseAnim = rememberInfiniteTransition(label = "hintPulse")
                            val pulseScale by hintPulseAnim.animateFloat(
                                initialValue = 0.92f,
                                targetValue = 1.18f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(380, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )
                            Box(
                                modifier = Modifier
                                    .size(cellSizeDp)
                                    .graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                    }
                                    .border(3.5.dp, Mango, CircleShape)
                            )
                        }

                        BuddyAvatar(
                            species = species,
                            direction = currentDir,
                            expression = currentExpression,
                            size = cellSizeDp,
                            modifier = Modifier.size(cellSizeDp)
                        )

                        // Key carrier: ribbon across chest with dangling key
                        val carrierKeyColor = buddy.keyColor
                        if (carrierKeyColor != null) {
                            KeyCarrierRibbon(
                                avatarSize = cellSizeDp,
                                keyColor = carrierKeyColor
                            )
                        }

                        // Sleepy buddy: cute nightcap with pompom
                        if (buddy.sleepy && !isWakingUp) {
                            SleepyNightcap(avatarSize = cellSizeDp)
                        }

                        // Muddy feet splatters on bottom of buddy
                        if (isOnMud && !isEscaping) {
                            MuddyFeetSplatters(avatarSize = cellSizeDp)
                        }

                        // Caged buddy: rounded bars with colored padlock
                        val cagePadColor = buddy.cageColor
                        if (cagePadColor != null) {
                            CagedBuddyOverlay(
                                avatarSize = cellSizeDp,
                                cageColor = cagePadColor
                            )
                        }
                    }
                }

                // Refusal Speech Bubble Popup ("Needs a key!" or "Zzz...")
                if (activeRefusal != null) {
                    val refusal = activeRefusal!!
                    val bubbleText = when (refusal.reason) {
                        RefusalReason.CAGED -> stringResource(R.string.bubble_needs_key)
                        RefusalReason.SLEEPY -> stringResource(R.string.bubble_sleepy_mumble)
                    }
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (cellSizeDp * refusal.pos.x) - 14.dp,
                                y = (cellSizeDp * refusal.pos.y) - 46.dp
                            )
                    ) {
                        SpeechBubble(
                            text = bubbleText,
                            tailPosition = TailPosition.BottomCenter
                        )
                    }
                }
            }

            // Tutorial / Hint Hand Overlay
            if (tutorialHandPos != null) {
                val handTargetX = rimPaddingPx + (tutorialHandPos.x + 0.5f) * cellSizePx
                val handTargetY = rimPaddingPx + (tutorialHandPos.y + 0.5f) * cellSizePx
                TutorialHandOverlay(
                    targetX = handTargetX,
                    targetY = handTargetY,
                    speechText = tutorialSpeechBubble,
                    modifier = Modifier.fillMaxSize()
                )
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
    val currentDir = mutableStateOf(buddy.dir)
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
    val currentDir = mutableStateOf(buddy.dir)
}

/**
 * Mutable state holding mud stop dash animatables.
 */
private class MudStopAnimState(
    val buddy: Buddy,
    val startX: Float,
    val startY: Float,
    val path: List<Step>,
    val mudPos: Pos
) {
    val animX = Animatable(startX)
    val animY = Animatable(startY)
    val scaleX = Animatable(1f)
    val scaleY = Animatable(1f)
    val expression = mutableStateOf(BuddyExpression.DASHING)
    val currentDir = mutableStateOf(buddy.dir)
}

private data class ActiveRefusal(
    val buddyId: String,
    val reason: RefusalReason,
    val pos: Pos
)

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

    val direct = buddies.firstOrNull { it.pos.x == col && it.pos.y == row }
    if (direct != null) return direct

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

    // 7. Checkered grass tiles and interactive terrain obstacles
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

            val tileCx = tileLeft + cellSize * 0.5f
            val tileCy = tileTop + cellSize * 0.5f

            when (val tile = level.tileAt(Pos(c, r))) {
                is Tile.Rock -> {
                    drawTactileRock(
                        cx = tileCx,
                        cy = tileCy,
                        radius = cellSize * 0.38f,
                        accent = worldTheme.accent.main
                    )
                }
                is Tile.Mud -> {
                    drawTactileMud(
                        cx = tileCx,
                        cy = tileCy,
                        radius = cellSize * 0.44f
                    )
                }
                is Tile.Turntable -> {
                    drawTactileTurntable(
                        cx = tileCx,
                        cy = tileCy,
                        radius = cellSize * 0.42f
                    )
                }
                is Tile.Portal -> {
                    drawTactilePortal(
                        cx = tileCx,
                        cy = tileCy,
                        radius = cellSize * 0.40f,
                        pairId = tile.pairId
                    )
                }
                is Tile.Empty -> Unit
            }
        }
    }
}

/**
 * 3D vector stone obstacle with contact shadow, highlight arc, and moss flecks.
 */
private fun DrawScope.drawTactileRock(cx: Float, cy: Float, radius: Float, accent: Color) {
    drawOval(
        color = Color(0x402B2240),
        topLeft = Offset(cx - radius * 0.95f, cy + radius * 0.35f),
        size = Size(radius * 1.9f, radius * 0.75f)
    )

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

    drawArc(
        color = WhitePure.copy(alpha = 0.45f),
        startAngle = 200f,
        sweepAngle = 100f,
        useCenter = false,
        topLeft = Offset(cx - radius * 0.7f, cy - radius * 0.65f),
        size = Size(radius * 1.4f, radius * 0.8f),
        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
    )

    drawCircle(color = accent.copy(alpha = 0.85f), radius = radius * 0.14f, center = Offset(cx - radius * 0.32f, cy + radius * 0.22f))
    drawCircle(color = accent.copy(alpha = 0.85f), radius = radius * 0.10f, center = Offset(cx - radius * 0.15f, cy + radius * 0.32f))
}

/**
 * Tactical mud puddle with organic darker rim, chocolate gradient, bubbles, and gloss highlight.
 */
private fun DrawScope.drawTactileMud(cx: Float, cy: Float, radius: Float) {
    drawOval(
        color = Color(0x332B2240),
        topLeft = Offset(cx - radius * 0.95f, cy - radius * 0.70f + 3f),
        size = Size(radius * 1.9f, radius * 1.5f)
    )
    drawOval(
        color = Color(0xFF4A2E12),
        topLeft = Offset(cx - radius * 0.92f, cy - radius * 0.72f),
        size = Size(radius * 1.84f, radius * 1.44f)
    )
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF7A4B24), Color(0xFF5A3314)),
            center = Offset(cx - radius * 0.2f, cy - radius * 0.2f),
            radius = radius * 1.1f
        ),
        topLeft = Offset(cx - radius * 0.85f, cy - radius * 0.65f),
        size = Size(radius * 1.7f, radius * 1.3f)
    )
    drawArc(
        color = WhitePure.copy(alpha = 0.35f),
        startAngle = 190f,
        sweepAngle = 110f,
        useCenter = false,
        topLeft = Offset(cx - radius * 0.7f, cy - radius * 0.55f),
        size = Size(radius * 1.4f, radius * 0.7f),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )
    drawCircle(color = Color(0xFF8D5B2F), radius = radius * 0.16f, center = Offset(cx + radius * 0.35f, cy - radius * 0.15f))
    drawCircle(color = WhitePure.copy(alpha = 0.45f), radius = radius * 0.05f, center = Offset(cx + radius * 0.32f, cy - radius * 0.18f))
    drawCircle(color = Color(0xFF8D5B2F), radius = radius * 0.11f, center = Offset(cx - radius * 0.35f, cy + radius * 0.25f))
    drawCircle(color = WhitePure.copy(alpha = 0.45f), radius = radius * 0.04f, center = Offset(cx - radius * 0.37f, cy + radius * 0.22f))
}

/**
 * 3D wooden disc turntable with curved clockwise arrow carved in and brass center pin.
 */
private fun DrawScope.drawTactileTurntable(cx: Float, cy: Float, radius: Float, rotationDegrees: Float = 0f) {
    drawCircle(color = Color(0x332B2240), radius = radius + 2f, center = Offset(cx, cy + 3f))
    drawCircle(color = Color(0xFF6D431F), radius = radius, center = Offset(cx, cy + 3f))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFC18C5D), Color(0xFF9E6B3E)),
            center = Offset(cx - radius * 0.25f, cy - radius * 0.25f),
            radius = radius * 1.2f
        ),
        radius = radius,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = Color(0xFF7A4D27).copy(alpha = 0.45f),
        radius = radius * 0.72f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f)
    )

    rotate(degrees = rotationDegrees, pivot = Offset(cx, cy)) {
        val arrowR = radius * 0.52f
        val arcRect = Rect(cx - arrowR, cy - arrowR, cx + arrowR, cy + arrowR)
        drawArc(
            color = Color(0xFF4E2E14),
            startAngle = 200f,
            sweepAngle = 230f,
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = Stroke(width = radius * 0.16f, cap = StrokeCap.Round)
        )
        val rad = (70f * PI / 180f).toFloat()
        val tipX = cx + arrowR * cos(rad)
        val tipY = cy + arrowR * sin(rad)
        val arrowHeadPath = Path().apply {
            moveTo(tipX, tipY)
            lineTo(tipX + radius * 0.18f, tipY - radius * 0.12f)
            lineTo(tipX - radius * 0.05f, tipY - radius * 0.22f)
            close()
        }
        drawPath(arrowHeadPath, color = Color(0xFF4E2E14))
    }

    drawCircle(color = Color(0xFFE6A119), radius = radius * 0.18f, center = Offset(cx, cy))
    drawCircle(color = Color(0xFFFFD54F), radius = radius * 0.12f, center = Offset(cx - 1f, cy - 1f))
    drawCircle(color = WhitePure, radius = radius * 0.05f, center = Offset(cx - 2f, cy - 2f))
}

/**
 * Tactile portal ring pair with distinct colorblind-friendly inner symbols:
 * Pair 1 = Circle, Pair 2 = Diamond, Pair 3 = Star.
 */
private fun DrawScope.drawTactilePortal(cx: Float, cy: Float, radius: Float, pairId: Int) {
    val (neonColor, accentColor) = when (pairId) {
        2 -> Color(0xFFE040FB) to Color(0xFF7C4DFF)
        3 -> Color(0xFFFFAB00) to Color(0xFFFF6D00)
        else -> Color(0xFF00E5FF) to Color(0xFF00B0FF)
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(neonColor.copy(alpha = 0.45f), Color.Transparent),
            center = Offset(cx, cy),
            radius = radius * 1.35f
        ),
        radius = radius * 1.35f,
        center = Offset(cx, cy)
    )
    drawCircle(color = accentColor.copy(alpha = 0.6f), radius = radius * 0.95f, center = Offset(cx, cy), style = Stroke(width = 3.5f))
    drawCircle(color = neonColor, radius = radius * 0.82f, center = Offset(cx, cy), style = Stroke(width = 2.5f))
    drawCircle(color = WhitePure, radius = 2.5f, center = Offset(cx + radius * 0.65f, cy - radius * 0.4f))
    drawCircle(color = neonColor, radius = 2f, center = Offset(cx - radius * 0.65f, cy + radius * 0.4f))

    val symbolR = radius * 0.35f
    when (pairId) {
        2 -> {
            val diamondPath = Path().apply {
                moveTo(cx, cy - symbolR)
                lineTo(cx + symbolR, cy)
                lineTo(cx, cy + symbolR)
                lineTo(cx - symbolR, cy)
                close()
            }
            drawPath(diamondPath, color = WhitePure, style = Stroke(width = 3f, cap = StrokeCap.Round))
            drawPath(diamondPath, color = neonColor.copy(alpha = 0.3f), style = Fill)
        }
        3 -> {
            val starPath = Path()
            val numPoints = 5
            for (i in 0 until numPoints * 2) {
                val r = if (i % 2 == 0) symbolR else symbolR * 0.48f
                val angle = (i * PI / numPoints - PI / 2).toFloat()
                val x = cx + r * cos(angle)
                val y = cy + r * sin(angle)
                if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
            }
            starPath.close()
            drawPath(starPath, color = WhitePure, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
            drawPath(starPath, color = neonColor.copy(alpha = 0.3f), style = Fill)
        }
        else -> {
            drawCircle(color = WhitePure, radius = symbolR, center = Offset(cx, cy), style = Stroke(width = 3f))
            drawCircle(color = neonColor.copy(alpha = 0.3f), radius = symbolR, center = Offset(cx, cy), style = Fill)
        }
    }
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

/**
 * Caged buddy overlay: rounded metal bars with a colored padlock (Gold, Silver, Ruby).
 */
@Composable
private fun CagedBuddyOverlay(
    avatarSize: Dp,
    cageColor: String
) {
    Canvas(modifier = Modifier.size(avatarSize)) {
        val padColor = when (cageColor.lowercase()) {
            "silver" -> Color(0xFFB0BEC5)
            "ruby" -> Color(0xFFFF4757)
            else -> Color(0xFFFFB627) // gold
        }
        val padLip = when (cageColor.lowercase()) {
            "silver" -> Color(0xFF78909C)
            "ruby" -> Color(0xFFC0392B)
            else -> Color(0xFFE6A119)
        }

        val padding = size.width * 0.08f
        val frameRect = Rect(padding, padding, size.width - padding, size.height - padding)

        // Cage border frame
        drawRoundRect(
            color = Color(0xFF8E9AAF),
            topLeft = frameRect.topLeft,
            size = frameRect.size,
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 3.5f)
        )

        // Vertical bars
        val numBars = 3
        for (i in 1..numBars) {
            val barX = frameRect.left + (frameRect.width * (i / (numBars + 1f)))
            drawLine(
                color = Color(0xFF8E9AAF),
                start = Offset(barX, frameRect.top),
                end = Offset(barX, frameRect.bottom),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        // Center Padlock
        val lockCx = center.x
        val lockCy = center.y + size.height * 0.05f
        val lockW = size.width * 0.28f
        val lockH = size.height * 0.22f

        // Shackle arch
        val shackleW = lockW * 0.55f
        val shackleH = lockH * 0.65f
        drawArc(
            color = Color(0xFF718093),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(lockCx - shackleW * 0.5f, lockCy - lockH * 0.5f - shackleH * 0.75f),
            size = Size(shackleW, shackleH),
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )

        // Padlock body shadow / lip
        drawRoundRect(
            color = padLip,
            topLeft = Offset(lockCx - lockW * 0.5f, lockCy - lockH * 0.5f + 2f),
            size = Size(lockW, lockH),
            cornerRadius = CornerRadius(6f, 6f)
        )
        // Padlock body face
        drawRoundRect(
            color = padColor,
            topLeft = Offset(lockCx - lockW * 0.5f, lockCy - lockH * 0.5f),
            size = Size(lockW, lockH),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Keyhole
        drawCircle(color = Color(0xFF2B2240), radius = lockW * 0.12f, center = Offset(lockCx, lockCy - 1f))
        val keyholeSlot = Path().apply {
            moveTo(lockCx - lockW * 0.08f, lockCy)
            lineTo(lockCx + lockW * 0.08f, lockCy)
            lineTo(lockCx + lockW * 0.05f, lockCy + lockH * 0.32f)
            lineTo(lockCx - lockW * 0.05f, lockCy + lockH * 0.32f)
            close()
        }
        drawPath(keyholeSlot, color = Color(0xFF2B2240))
    }
}

/**
 * Key carrier accessory: diagonal sash ribbon across chest with dangling key.
 */
@Composable
private fun KeyCarrierRibbon(
    avatarSize: Dp,
    keyColor: String
) {
    Canvas(modifier = Modifier.size(avatarSize)) {
        val keyTone = when (keyColor.lowercase()) {
            "silver" -> Color(0xFFCFD8DC)
            "ruby" -> Color(0xFFFF5252)
            else -> Color(0xFFFFD54F) // gold
        }
        val keyOutline = when (keyColor.lowercase()) {
            "silver" -> Color(0xFF78909C)
            "ruby" -> Color(0xFFB71C1C)
            else -> Color(0xFFE6A119)
        }

        // Ribbon sash across chest
        val sashPath = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.42f)
            lineTo(size.width * 0.78f, size.height * 0.74f)
        }
        drawPath(sashPath, color = Color(0xFF2B2240).copy(alpha = 0.55f), style = Stroke(width = 4f, cap = StrokeCap.Round))

        // Dangling key badge on chest
        val keyX = size.width * 0.72f
        val keyY = size.height * 0.68f
        val keyR = size.width * 0.10f

        // Key head loop
        drawCircle(color = keyTone, radius = keyR, center = Offset(keyX, keyY))
        drawCircle(color = keyOutline, radius = keyR, center = Offset(keyX, keyY), style = Stroke(width = 1.5f))
        drawCircle(color = WhitePure, radius = keyR * 0.42f, center = Offset(keyX, keyY))

        // Key stem & teeth
        val stemEnd = Offset(keyX + keyR * 1.1f, keyY + keyR * 1.1f)
        drawLine(color = keyOutline, start = Offset(keyX + keyR * 0.7f, keyY + keyR * 0.7f), end = stemEnd, strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(color = keyOutline, start = stemEnd, end = Offset(stemEnd.x - 3f, stemEnd.y + 4f), strokeWidth = 2f, cap = StrokeCap.Round)
    }
}

/**
 * Sleepy buddy accessory: cozy nightcap with a white pompom perched on the buddy's head.
 */
@Composable
private fun SleepyNightcap(avatarSize: Dp) {
    Canvas(modifier = Modifier.size(avatarSize)) {
        val capPath = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.32f)
            quadraticTo(
                size.width * 0.35f, size.height * 0.08f,
                size.width * 0.65f, size.height * 0.12f
            )
            quadraticTo(
                size.width * 0.85f, size.height * 0.18f,
                size.width * 0.88f, size.height * 0.34f
            )
            quadraticTo(
                size.width * 0.55f, size.height * 0.26f,
                size.width * 0.25f, size.height * 0.32f
            )
            close()
        }

        drawPath(capPath, color = Color(0xFF7B61FF))
        // Cap stripe
        val stripePath = Path().apply {
            moveTo(size.width * 0.42f, size.height * 0.20f)
            lineTo(size.width * 0.52f, size.height * 0.29f)
        }
        drawPath(stripePath, color = Color(0xFFB39DDB), style = Stroke(width = 3f, cap = StrokeCap.Round))
        drawPath(capPath, color = Color(0xFF2B2240), style = Stroke(width = 1.5f))

        // Fluffy pompom at tip
        drawCircle(color = WhitePure, radius = size.width * 0.09f, center = Offset(size.width * 0.88f, size.height * 0.34f))
        drawCircle(color = Color(0xFF2B2240), radius = size.width * 0.09f, center = Offset(size.width * 0.88f, size.height * 0.34f), style = Stroke(width = 1.2f))
    }
}

/**
 * Cute mud splatters along the bottom of a buddy standing on a mud tile.
 */
@Composable
private fun MuddyFeetSplatters(avatarSize: Dp) {
    Canvas(modifier = Modifier.size(avatarSize)) {
        val mudColor = Color(0xFF5A3314)
        val bottomY = size.height * 0.86f

        drawOval(color = mudColor, topLeft = Offset(size.width * 0.25f, bottomY), size = Size(size.width * 0.14f, size.height * 0.07f))
        drawOval(color = mudColor, topLeft = Offset(size.width * 0.44f, bottomY + 2f), size = Size(size.width * 0.16f, size.height * 0.08f))
        drawOval(color = mudColor, topLeft = Offset(size.width * 0.65f, bottomY), size = Size(size.width * 0.13f, size.height * 0.07f))
    }
}
