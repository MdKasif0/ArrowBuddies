package com.kasif.arrowbuddies.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.designsystem.theme.WorldTheme

private data class CloudConfig(
    val yFraction: Float,
    val widthFraction: Float,
    val speedMultiplier: Float,
    val initialOffsetFraction: Float,
    val alpha: Float
)

private val StaticClouds = listOf(
    CloudConfig(yFraction = 0.08f, widthFraction = 0.32f, speedMultiplier = 1.0f, initialOffsetFraction = 0.1f, alpha = 0.45f),
    CloudConfig(yFraction = 0.18f, widthFraction = 0.42f, speedMultiplier = 0.7f, initialOffsetFraction = 0.6f, alpha = 0.55f),
    CloudConfig(yFraction = 0.32f, widthFraction = 0.28f, speedMultiplier = 1.3f, initialOffsetFraction = 0.35f, alpha = 0.40f),
    CloudConfig(yFraction = 0.45f, widthFraction = 0.38f, speedMultiplier = 0.85f, initialOffsetFraction = 0.85f, alpha = 0.50f)
)

/**
 * High performance animated sky background:
 * - World sky top/bottom vertical gradient
 * - 4 drifting clouds with parallax speeds
 * - ZERO recompositions per frame: the infinite transition is read exclusively in the draw phase
 */
@Composable
fun AnimatedSkyBackground(
    modifier: Modifier = Modifier,
    worldTheme: WorldTheme = ArrowBuddiesThemeTokens.worldTheme
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skyDriftTransition")
    val timeState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 120_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skyDriftProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Draw World Sky Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(worldTheme.skyTop, worldTheme.skyBottom),
                startY = 0f,
                endY = h
            ),
            size = size
        )

        // Read time strictly in draw phase (no composition invalidation)
        val elapsed = timeState.value

        // 2. Draw Soft Clouds with Parallax
        for (cloud in StaticClouds) {
            val cloudW = w * cloud.widthFraction
            val cloudH = cloudW * 0.42f
            val travelDistance = w + cloudW
            val rawX = (cloud.initialOffsetFraction * w + elapsed * cloud.speedMultiplier * 20f)
            val cloudX = (rawX % travelDistance) - cloudW
            val cloudY = h * cloud.yFraction

            val cloudColor = Color.White.copy(alpha = cloud.alpha)

            // Draw puffy cloud composed of 3 overlapping soft circles + base pill
            val r1 = cloudH * 0.6f
            val r2 = cloudH * 0.85f
            val r3 = cloudH * 0.55f

            drawCircle(
                color = cloudColor,
                radius = r1,
                center = Offset(cloudX + cloudW * 0.28f, cloudY + cloudH * 0.55f)
            )
            drawCircle(
                color = cloudColor,
                radius = r2,
                center = Offset(cloudX + cloudW * 0.52f, cloudY + cloudH * 0.42f)
            )
            drawCircle(
                color = cloudColor,
                radius = r3,
                center = Offset(cloudX + cloudW * 0.75f, cloudY + cloudH * 0.58f)
            )
            // Base rounded support
            drawRoundRect(
                color = cloudColor,
                topLeft = Offset(cloudX + cloudW * 0.15f, cloudY + cloudH * 0.35f),
                size = Size(cloudW * 0.70f, cloudH * 0.55f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cloudH * 0.3f, cloudH * 0.3f)
            )
        }
    }
}

@Preview(name = "Animated Sky", showBackground = true)
@Composable
private fun AnimatedSkyBackgroundPreview() {
    ArrowBuddiesTheme {
        AnimatedSkyBackground()
    }
}
