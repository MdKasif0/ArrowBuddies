package com.kasif.arrowbuddies.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kasif.arrowbuddies.core.designsystem.modifier.LocalHaptics
import com.kasif.arrowbuddies.core.designsystem.modifier.LocalSound
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesMotion
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3 Stars row for level completion.
 * Stars pop in sequentially with rising animation, haptic ticks, and sound chimes.
 */
@Composable
fun StarRow(
    starsEarned: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 44.dp,
    animateSequential: Boolean = true
) {
    val soundManager = LocalSound.current
    val hapticsManager = LocalHaptics.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 3) {
            val isEarned = i < starsEarned
            StarItem(
                isEarned = isEarned,
                index = i,
                size = starSize,
                animateSequential = animateSequential,
                onStarFilled = {
                    hapticsManager?.tick()
                    soundManager.playChime()
                }
            )
        }
    }
}

@Composable
private fun StarItem(
    isEarned: Boolean,
    index: Int,
    size: Dp,
    animateSequential: Boolean,
    onStarFilled: () -> Unit
) {
    val colors = ArrowBuddiesThemeTokens.colors
    val scale = remember { Animatable(if (isEarned && !animateSequential) 1f else 0.4f) }
    val rotation = remember { Animatable(if (isEarned && !animateSequential) 0f else -30f) }

    LaunchedEffect(isEarned) {
        if (isEarned) {
            if (animateSequential) {
                delay(index * 220L)
            }
            onStarFilled()
            scale.animateTo(1.35f, ArrowBuddiesMotion.Snappy)
            rotation.animateTo(10f, ArrowBuddiesMotion.Snappy)
            scale.animateTo(1f, ArrowBuddiesMotion.Bouncy)
            rotation.animateTo(0f, ArrowBuddiesMotion.Bouncy)
        } else {
            scale.snapTo(1f)
            rotation.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                rotationZ = rotation.value
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val outerRadius = this.size.width * 0.48f
            val innerRadius = outerRadius * 0.48f

            val starPath = Path()
            val points = 5
            var angle = -Math.PI / 2

            for (p in 0 until points * 2) {
                val r = if (p % 2 == 0) outerRadius else innerRadius
                val x = cx + (r * cos(angle)).toFloat()
                val y = cy + (r * sin(angle)).toFloat()
                if (p == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
                angle += Math.PI / points
            }
            starPath.close()

            if (isEarned) {
                // Gold gradient fill
                drawPath(
                    path = starPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFDF7A),
                            colors.mango.main
                        )
                    ),
                    style = Fill
                )
                // Darker 3D lip stroke
                drawPath(
                    path = starPath,
                    color = colors.mango.lip,
                    style = Stroke(width = 3.dp.toPx())
                )
                // Top Gloss sparkle
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = outerRadius * 0.18f,
                    center = Offset(cx - outerRadius * 0.25f, cy - outerRadius * 0.35f)
                )
            } else {
                // Empty / inactive silhouette
                drawPath(
                    path = starPath,
                    color = colors.cream.lip,
                    style = Fill
                )
                drawPath(
                    path = starPath,
                    color = Color(0xFFC7B69C),
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
        }
    }
}

@Preview(name = "Star Row", showBackground = true, backgroundColor = 0xFFFFF7EC)
@Composable
private fun StarRowPreview() {
    ArrowBuddiesTheme {
        StarRow(
            starsEarned = 3,
            modifier = Modifier.size(width = 200.dp, height = 60.dp),
            animateSequential = false
        )
    }
}

