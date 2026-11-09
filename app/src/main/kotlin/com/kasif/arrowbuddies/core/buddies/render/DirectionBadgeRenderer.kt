package com.kasif.arrowbuddies.core.buddies.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.kasif.arrowbuddies.engine.model.Direction

/**
 * Draws the high-contrast directional arrow badge attached to the active movement edge of the buddy.
 * Fully readable at 36dp and by color-blind players.
 */
object DirectionBadgeRenderer {

    fun drawBadge(
        drawScope: DrawScope,
        ctx: BuddyDrawContext,
        direction: Direction,
        animatedAngleDegrees: Float,
        accentColor: Color
    ) {
        with(drawScope) {
            val badgeRadius = ctx.minDim * 0.19f
            val dist = ctx.bodyRadius * 0.98f

            // Position badge along the edge corresponding to the buddy's face direction
            val badgeCenter = when (direction) {
                Direction.UP -> Offset(ctx.centerX, ctx.centerY - dist)
                Direction.DOWN -> Offset(ctx.centerX, ctx.centerY + dist)
                Direction.LEFT -> Offset(ctx.centerX - dist, ctx.centerY)
                Direction.RIGHT -> Offset(ctx.centerX + dist, ctx.centerY)
            }

            // 1. Soft contact drop shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.28f),
                radius = badgeRadius,
                center = badgeCenter + Offset(0f, badgeRadius * 0.18f)
            )

            // 2. 3D Dark bottom lip
            drawCircle(
                color = Color(0xFF1E162D),
                radius = badgeRadius,
                center = badgeCenter + Offset(0f, badgeRadius * 0.10f)
            )

            // 3. Vibrant Accent Face Disc
            drawCircle(
                color = accentColor,
                radius = badgeRadius,
                center = badgeCenter
            )

            // 4. Subtle top gloss highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = badgeRadius * 0.70f,
                center = badgeCenter - Offset(0f, badgeRadius * 0.22f)
            )

            // 5. Dark Ink outline for high contrast
            drawCircle(
                color = ctx.outlineColor,
                radius = badgeRadius,
                center = badgeCenter,
                style = Stroke(width = ctx.outlineWidth)
            )

            // 6. Bold White Arrow (rotated by animatedAngleDegrees with spring)
            rotate(
                degrees = animatedAngleDegrees,
                pivot = badgeCenter
            ) {
                val arrowPath = Path().apply {
                    val tipY = badgeCenter.y - badgeRadius * 0.56f
                    val wingY = badgeCenter.y + badgeRadius * 0.38f
                    val wingW = badgeRadius * 0.50f
                    val stemW = badgeRadius * 0.20f
                    val stemBottomY = badgeCenter.y + badgeRadius * 0.54f
                    val notchY = badgeCenter.y + badgeRadius * 0.10f

                    // Bold arrow shape
                    moveTo(badgeCenter.x, tipY)
                    lineTo(badgeCenter.x + wingW, wingY)
                    lineTo(badgeCenter.x + stemW, notchY)
                    lineTo(badgeCenter.x + stemW, stemBottomY)
                    lineTo(badgeCenter.x - stemW, stemBottomY)
                    lineTo(badgeCenter.x - stemW, notchY)
                    lineTo(badgeCenter.x - wingW, wingY)
                    close()
                }

                // Arrow soft drop shadow for depth
                drawPath(
                    path = arrowPath,
                    color = Color.Black.copy(alpha = 0.22f),
                    style = Fill
                )

                // Pure Crisp White Arrow Fill
                drawPath(
                    path = arrowPath,
                    color = Color.White,
                    style = Fill
                )

                // Dark outline around arrow for maximum legibility at 36dp
                drawPath(
                    path = arrowPath,
                    color = ctx.outlineColor.copy(alpha = 0.85f),
                    style = Stroke(width = maxOf(1f, ctx.outlineWidth * 0.65f))
                )
            }
        }
    }
}

