package com.kasif.arrowbuddies.core.buddies

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kasif.arrowbuddies.core.buddies.model.BuddyExpression
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.buddies.render.BuddyDrawContext
import com.kasif.arrowbuddies.core.buddies.render.BuddyRenderer
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.engine.model.Direction

/**
 * Shared game frame clock to drive 40+ buddy idle animations with zero recomposition.
 */
val LocalSharedGameClock = compositionLocalOf<State<Long>?> { null }

/**
 * BuddyAvatar: Fully procedural vector character drawn with DrawScope paths and gradients.
 * Zero bitmaps, zero external art, optimized with `drawWithCache` to ensure 60 fps.
 *
 * @param species The [BuddySpecies] of the buddy (one of 24 distinct animals).
 * @param direction The movement/facing direction [Direction], indicated by posture and arrow badge.
 * @param expression Emotional state [BuddyExpression] (Idle, Worried, Ready, Dashing, Bonked, Happy, Sleepy, Caged).
 * @param size Avatar bounding box size (e.g. 36dp for small grid cells up to 160dp in hero/album view).
 * @param modifier Optional modifier.
 * @param phaseSeed Unique seed to desynchronize breathing, bobbing, and blinking across multiple buddies.
 * @param sharedClock Optional shared clock override. If omitted, uses [LocalSharedGameClock] or an internal clock.
 */
@Composable
fun BuddyAvatar(
    species: BuddySpecies,
    direction: Direction,
    modifier: Modifier = Modifier,
    expression: BuddyExpression = BuddyExpression.IDLE,
    size: Dp = 64.dp,
    phaseSeed: Int = remember(species) { (species.ordinal * 739) % 10000 },
    sharedClock: State<Long>? = null
) {
    // 1. Ambient Clock or Fallback Transition (draw phase only)
    val ambientClock = LocalSharedGameClock.current
    val effectiveClock: State<Long> = sharedClock
        ?: ambientClock
        ?: run {
            val infiniteTransition = rememberInfiniteTransition(label = "BuddyLocalClock")
            val animFloat = infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 60000f,
                animationSpec = infiniteRepeatable(
                    animation = tween(60000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "BuddyLocalTimeMs"
            )
            remember(infiniteTransition) {
                object : State<Long> {
                    override val value: Long get() = animFloat.value.toLong()
                }
            }
        }

    // 2. Shortest-path Direction Angle Spring Rotation
    var currentAngle by remember { mutableFloatStateOf(direction.angleDegrees) }
    LaunchedEffect(direction) {
        val target = direction.angleDegrees
        var diff = (target - currentAngle) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        currentAngle += diff
    }

    val animatedBadgeAngle by animateFloatAsState(
        targetValue = currentAngle,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "BadgeAngleSpring"
    )

    // 3. High Performance drawWithCache Box
    Box(
        modifier = modifier
            .size(size)
            .drawWithCache {
                // CACHE PHASE: Only re-executes when Size or Species changes!
                val ctx = BuddyDrawContext(
                    size = this.size,
                    species = species
                )

                // DRAW PHASE: Re-executes on clock/angle ticks with zero allocations
                onDrawBehind {
                    BuddyRenderer.renderBuddy(
                        drawScope = this,
                        ctx = ctx,
                        direction = direction,
                        expression = expression,
                        timeMs = effectiveClock.value,
                        phaseSeed = phaseSeed,
                        animatedAngleDegrees = animatedBadgeAngle
                    )
                }
            }
    )
}

@Preview(name = "Buddy Avatar - World 1 Basics")
@Composable
private fun BuddyAvatarWorld1Preview() {
    ArrowBuddiesTheme {
        Box {
            BuddyAvatar(
                species = BuddySpecies.BUNNY,
                direction = Direction.RIGHT,
                expression = BuddyExpression.IDLE,
                size = 80.dp
            )
        }
    }
}

@Preview(name = "Buddy Avatar - Expressions")
@Composable
private fun BuddyAvatarExpressionsPreview() {
    ArrowBuddiesTheme {
        Box {
            BuddyAvatar(
                species = BuddySpecies.PUPPY,
                direction = Direction.UP,
                expression = BuddyExpression.HAPPY,
                size = 80.dp
            )
        }
    }
}

@Preview(name = "Buddy Avatar - Bonked Tiger")
@Composable
private fun BuddyAvatarBonkedPreview() {
    ArrowBuddiesTheme {
        Box {
            BuddyAvatar(
                species = BuddySpecies.TIGER_CUB,
                direction = Direction.LEFT,
                expression = BuddyExpression.BONKED,
                size = 80.dp
            )
        }
    }
}
