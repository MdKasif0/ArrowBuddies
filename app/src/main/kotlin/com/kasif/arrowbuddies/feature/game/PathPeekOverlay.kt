package com.kasif.arrowbuddies.feature.game

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.kasif.arrowbuddies.core.designsystem.theme.Coral
import com.kasif.arrowbuddies.core.designsystem.theme.Mint
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure
import com.kasif.arrowbuddies.engine.model.Pos
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated path peek overlay.
 * Displays a dotted line from the buddy to the first blocker (Red X) or to the exit (Green Sparkle).
 */
@Composable
fun PathPeekOverlay(
    peekingPath: PeekingPath,
    cellSizePx: Float,
    rimPaddingPx: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PathPeekMarchingAnts")
    val phaseOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PhaseOffset"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val startCenter = posToCenter(peekingPath.startPos, cellSizePx, rimPaddingPx)
        val endCenter = posToCenter(peekingPath.endPos, cellSizePx, rimPaddingPx)

        val pathColor = if (peekingPath.isEscape) Mint else Coral
        val dottedEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), phaseOffset)

        // Draw path line with shadow and bright core
        drawLine(
            color = Color(0x662B2240),
            start = startCenter,
            end = endCenter,
            strokeWidth = 10f,
            cap = StrokeCap.Round,
            pathEffect = dottedEffect
        )
        drawLine(
            color = pathColor,
            start = startCenter,
            end = endCenter,
            strokeWidth = 6f,
            cap = StrokeCap.Round,
            pathEffect = dottedEffect
        )

        // Draw endpoint badge
        if (peekingPath.isEscape) {
            drawSparkleExit(endCenter, 20f * pulseScale)
        } else {
            drawBlockerX(endCenter, 18f * pulseScale)
        }
    }
}

private fun posToCenter(pos: Pos, cellSizePx: Float, rimPaddingPx: Float): Offset {
    val x = rimPaddingPx + (pos.x + 0.5f) * cellSizePx
    val y = rimPaddingPx + (pos.y + 0.5f) * cellSizePx
    return Offset(x, y)
}

private fun DrawScope.drawSparkleExit(center: Offset, radius: Float) {
    // Glowing outer circle
    drawCircle(
        color = Mint.copy(alpha = 0.35f),
        radius = radius * 1.5f,
        center = center
    )
    // Tactile base circle
    drawCircle(
        color = Color(0xFF2E8B57), // Dark lip
        radius = radius + 2f,
        center = Offset(center.x, center.y + 2f)
    )
    drawCircle(
        color = Mint,
        radius = radius,
        center = center
    )
    // Inner 4-point sparkle star
    val starColor = WhitePure
    val half = radius * 0.7f
    val thin = 4f
    drawLine(color = starColor, start = Offset(center.x, center.y - half), end = Offset(center.x, center.y + half), strokeWidth = thin, cap = StrokeCap.Round)
    drawLine(color = starColor, start = Offset(center.x - half, center.y), end = Offset(center.x + half, center.y), strokeWidth = thin, cap = StrokeCap.Round)
    // Mini diagonal glints
    val diag = half * 0.5f
    drawLine(color = starColor, start = Offset(center.x - diag, center.y - diag), end = Offset(center.x + diag, center.y + diag), strokeWidth = thin * 0.7f, cap = StrokeCap.Round)
    drawLine(color = starColor, start = Offset(center.x + diag, center.y - diag), end = Offset(center.x - diag, center.y + diag), strokeWidth = thin * 0.7f, cap = StrokeCap.Round)
}

private fun DrawScope.drawBlockerX(center: Offset, radius: Float) {
    // Shadow
    drawCircle(
        color = Color(0x662B2240),
        radius = radius + 3f,
        center = Offset(center.x, center.y + 3f)
    )
    // Dark Coral 3D lip
    drawCircle(
        color = Color(0xFFC94A4A),
        radius = radius,
        center = Offset(center.x, center.y + 2f)
    )
    // Coral top disc
    drawCircle(
        color = Coral,
        radius = radius,
        center = center
    )
    // White X
    val xSize = radius * 0.5f
    val stroke = 5f
    drawLine(
        color = WhitePure,
        start = Offset(center.x - xSize, center.y - xSize),
        end = Offset(center.x + xSize, center.y + xSize),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
    drawLine(
        color = WhitePure,
        start = Offset(center.x + xSize, center.y - xSize),
        end = Offset(center.x - xSize, center.y + xSize),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
}

