package com.kasif.arrowbuddies.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesMotion
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure

enum class TailPosition {
    BottomLeft,
    BottomCenter,
    BottomRight
}

/**
 * A tactile speech bubble for tutorial prompts and buddy dialogs.
 * Includes pop-in spring animation, custom tail vector, and subtle 3D border.
 */
@Composable
fun SpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    tailPosition: TailPosition = TailPosition.BottomLeft,
    backgroundColor: Color = WhitePure,
    borderColor: Color = Color(0xFFE4D5BC),
    tailHeight: Dp = 10.dp
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val colors = ArrowBuddiesThemeTokens.colors
    val scale = remember { Animatable(0.7f) }

    LaunchedEffect(text) {
        scale.snapTo(0.7f)
        scale.animateTo(1f, ArrowBuddiesMotion.Bouncy)
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val th = tailHeight.toPx()
            val h = size.height - th
            val cr = 18.dp.toPx()

            // Bubble body + tail path
            val path = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = 0f,
                        top = 0f,
                        right = w,
                        bottom = h,
                        cornerRadius = CornerRadius(cr, cr)
                    )
                )

                // Add bottom pointing tail
                val tailX = when (tailPosition) {
                    TailPosition.BottomLeft -> w * 0.22f
                    TailPosition.BottomCenter -> w * 0.5f
                    TailPosition.BottomRight -> w * 0.78f
                }
                moveTo(tailX - 12.dp.toPx(), h)
                lineTo(tailX, h + th)
                lineTo(tailX + 12.dp.toPx(), h)
            }

            // Fill
            drawPath(path, color = backgroundColor, style = Fill)
            // 3D Lip Border
            drawPath(path, color = borderColor, style = Stroke(width = 2.dp.toPx()))

            // Top Inner White Highlight
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = androidx.compose.ui.geometry.Offset(cr, 2.dp.toPx()),
                end = androidx.compose.ui.geometry.Offset(w - cr, 2.dp.toPx()),
                strokeWidth = 2.5.dp.toPx()
            )
        }

        Text(
            text = text,
            style = typography.body.copy(color = colors.textPrimary),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).padding(bottom = tailHeight)
        )
    }
}

@Preview(name = "Speech Bubble", showBackground = true, backgroundColor = 0xFFFFF7EC)
@Composable
private fun SpeechBubblePreview() {
    ArrowBuddiesTheme {
        Box(modifier = Modifier.padding(24.dp)) {
            SpeechBubble(text = "Tap me to dash to freedom! 🐰")
        }
    }
}
