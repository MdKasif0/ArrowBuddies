package com.kasif.arrowbuddies.feature.game

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kasif.arrowbuddies.core.designsystem.component.SpeechBubble
import com.kasif.arrowbuddies.core.designsystem.component.TailPosition
import com.kasif.arrowbuddies.core.designsystem.theme.Ink
import com.kasif.arrowbuddies.core.designsystem.theme.Mango
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure

/**
 * Animated cartoon tutorial hand with pointing finger and optional speech bubble.
 */
@Composable
fun TutorialHandOverlay(
    targetX: Float,
    targetY: Float,
    speechText: String?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "handBounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Box(modifier = modifier) {
        // Speech Bubble placed above the target cell
        speechText?.let { text ->
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (targetX - 110.dp.toPx()).toInt().coerceAtLeast(16),
                            y = (targetY - 95.dp.toPx() - bounceOffset).toInt().coerceAtLeast(16)
                        )
                    }
                    .wrapContentSize()
            ) {
                SpeechBubble(
                    text = text,
                    tailPosition = TailPosition.BottomCenter
                )
            }
        }

        // Animated Hand Pointer
        Canvas(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (targetX - 22.dp.toPx()).toInt(),
                        y = (targetY - 12.dp.toPx() + bounceOffset).toInt()
                    )
                }
                .size(48.dp)
        ) {
            val w = size.width
            val h = size.height

            // Glow ripple under finger
            drawCircle(
                color = Mango.copy(alpha = 0.35f),
                radius = w * 0.45f,
                center = Offset(w * 0.45f, h * 0.2f)
            )

            // Cartoon pointing hand path
            val handPath = Path().apply {
                // Index finger pointing up-left
                moveTo(w * 0.40f, h * 0.15f)
                cubicTo(w * 0.35f, h * 0.10f, w * 0.25f, h * 0.10f, w * 0.22f, h * 0.18f)
                lineTo(w * 0.20f, h * 0.45f)
                // Knuckles
                cubicTo(w * 0.18f, h * 0.55f, w * 0.22f, h * 0.70f, w * 0.35f, h * 0.85f)
                // Palm bottom
                cubicTo(w * 0.50f, h * 0.95f, w * 0.75f, h * 0.90f, w * 0.85f, h * 0.75f)
                // Thumb & folded fingers
                cubicTo(w * 0.95f, h * 0.60f, w * 0.80f, h * 0.45f, w * 0.65f, h * 0.48f)
                cubicTo(w * 0.55f, h * 0.40f, w * 0.50f, h * 0.30f, w * 0.40f, h * 0.15f)
                close()
            }

            // Hand base fill
            drawPath(
                path = handPath,
                color = WhitePure,
                style = Fill
            )

            // Hand outline
            drawPath(
                path = handPath,
                color = Ink,
                style = Stroke(width = 3.dp.toPx())
            )

            // Finger lines
            drawLine(
                color = Ink,
                start = Offset(w * 0.45f, h * 0.52f),
                end = Offset(w * 0.62f, h * 0.56f),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Ink,
                start = Offset(w * 0.42f, h * 0.64f),
                end = Offset(w * 0.60f, h * 0.68f),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

