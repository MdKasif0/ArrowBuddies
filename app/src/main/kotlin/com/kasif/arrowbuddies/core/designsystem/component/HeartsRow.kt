package com.kasif.arrowbuddies.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens

/**
 * 3 Hearts row.
 * When a heart is lost, it shakes/cracks, falls downward, and fades out.
 */
@Composable
fun HeartsRow(
    heartsRemaining: Int,
    modifier: Modifier = Modifier,
    totalHearts: Int = 3,
    heartSize: Dp = 32.dp
) {
    val heartsLabel = stringResource(R.string.talkback_hud_hearts, heartsRemaining)
    Row(
        modifier = modifier.semantics { contentDescription = heartsLabel },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalHearts) {
            val isFilled = i < heartsRemaining
            HeartItem(
                isFilled = isFilled,
                size = heartSize
            )
        }
    }
}

@Composable
private fun HeartItem(
    isFilled: Boolean,
    size: Dp
) {
    val colors = ArrowBuddiesThemeTokens.colors

    // Falling / fading animation when lost
    val fallOffsetY = remember { Animatable(0f) }
    val rotationAngle = remember { Animatable(0f) }
    val heartAlpha = remember { Animatable(if (isFilled) 1f else 0.25f) }
    val crackProgress = remember { Animatable(0f) }

    LaunchedEffect(isFilled) {
        if (!isFilled) {
            // Crack & Fall sequence
            crackProgress.animateTo(1f, tween(100))
            rotationAngle.animateTo(-18f, tween(120, easing = FastOutSlowInEasing))
            fallOffsetY.animateTo(40f, tween(260, easing = FastOutSlowInEasing))
            heartAlpha.animateTo(0.25f, tween(200))
        } else {
            // Restore immediately
            fallOffsetY.snapTo(0f)
            rotationAngle.snapTo(0f)
            crackProgress.snapTo(0f)
            heartAlpha.animateTo(1f, tween(150))
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                translationY = fallOffsetY.value
                rotationZ = rotationAngle.value
                alpha = heartAlpha.value
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Vector heart path
            val heartPath = Path().apply {
                moveTo(w * 0.5f, h * 0.82f)
                cubicTo(w * 0.15f, h * 0.60f, 0f, h * 0.42f, 0f, h * 0.25f)
                cubicTo(0f, h * 0.08f, w * 0.18f, 0f, w * 0.35f, 0f)
                cubicTo(w * 0.43f, 0f, w * 0.48f, h * 0.08f, w * 0.5f, h * 0.14f)
                cubicTo(w * 0.52f, h * 0.08f, w * 0.57f, 0f, w * 0.65f, 0f)
                cubicTo(w * 0.82f, 0f, w, h * 0.08f, w, h * 0.25f)
                cubicTo(w, h * 0.42f, w * 0.85f, h * 0.60f, w * 0.5f, h * 0.82f)
                close()
            }

            // Fill
            val fillColor = if (isFilled) colors.coral.main else colors.cream.lip
            drawPath(heartPath, color = fillColor, style = Fill)

            // 3D Bottom shade / border
            drawPath(
                heartPath,
                color = if (isFilled) colors.coral.lip else Color(0xFFC7B69C),
                style = Stroke(width = 3.dp.toPx())
            )

            // Top Gloss Highlight (only if filled)
            if (isFilled) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.65f),
                    radius = w * 0.12f,
                    center = Offset(w * 0.32f, h * 0.26f)
                )
            }

            // Crack zigzag line if cracked
            if (crackProgress.value > 0f) {
                val crackPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.44f, h * 0.35f)
                    lineTo(w * 0.56f, h * 0.52f)
                    lineTo(w * 0.48f, h * 0.72f)
                }
                drawPath(
                    path = crackPath,
                    color = Color.White,
                    style = Stroke(width = 2.dp.toPx() * crackProgress.value)
                )
            }
        }
    }
}

@Preview(name = "Hearts Row", showBackground = true, backgroundColor = 0xFFFFF7EC)
@Composable
private fun HeartsRowPreview() {
    ArrowBuddiesTheme {
        HeartsRow(
            heartsRemaining = 2,
            modifier = Modifier.size(width = 160.dp, height = 50.dp)
        )
    }
}

