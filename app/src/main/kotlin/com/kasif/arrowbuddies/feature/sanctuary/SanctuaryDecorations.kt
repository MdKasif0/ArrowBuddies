package com.kasif.arrowbuddies.feature.sanctuary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kasif.arrowbuddies.core.designsystem.theme.Coral
import com.kasif.arrowbuddies.core.designsystem.theme.Cream
import com.kasif.arrowbuddies.core.designsystem.theme.Grape
import com.kasif.arrowbuddies.core.designsystem.theme.Ink
import com.kasif.arrowbuddies.core.designsystem.theme.Mango
import com.kasif.arrowbuddies.core.designsystem.theme.Mint
import com.kasif.arrowbuddies.core.designsystem.theme.Sky
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural sanctuary decorations drawn exclusively in Compose Canvas with subtle idle physics.
 */
@Composable
fun SanctuaryDecor(
    decorId: String,
    size: Dp = 100.dp,
    timeMs: Long = 0L,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            when (decorId) {
                "decor_pond_1" -> drawKoiPond(w, h, timeMs)
                "decor_swing_2" -> drawTreeSwing(w, h, timeMs)
                "decor_flower_bed_3" -> drawFlowerBed(w, h, timeMs)
                "decor_firepit_4" -> drawFirepit(w, h, timeMs)
                "decor_hammock_5" -> drawHammock(w, h, timeMs)
                "decor_lanterns_6" -> drawGlowLanterns(w, h, timeMs)
                "decor_kite_7" -> drawOasisKite(w, h, timeMs)
                "decor_rangoli_mat_8" -> drawRangoliAndMangoTree(w, h, timeMs)
                else -> drawKoiPond(w, h, timeMs)
            }
        }
    }
}

private fun DrawScope.drawKoiPond(w: Float, h: Float, timeMs: Long) {
    val cx = w * 0.5f
    val cy = h * 0.55f
    val rx = w * 0.44f
    val ry = h * 0.32f

    // Stone border ring
    drawOval(
        color = Color(0xFF7A6B5D),
        topLeft = Offset(cx - rx - 5f, cy - ry - 4f),
        size = Size(rx * 2 + 10f, ry * 2 + 8f)
    )

    // Clear turquoise pond water
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF5CE1E6), Color(0xFF0096C7), Color(0xFF023E8A)),
            center = Offset(cx, cy),
            radius = rx
        ),
        topLeft = Offset(cx - rx, cy - ry),
        size = Size(rx * 2, ry * 2)
    )

    // Lily pad
    val padX = cx - rx * 0.4f
    val padY = cy - ry * 0.2f
    drawCircle(color = Color(0xFF2D6A4F), radius = w * 0.12f, center = Offset(padX, padY))
    drawCircle(color = Coral, radius = w * 0.04f, center = Offset(padX, padY)) // Pink lotus

    // Swimming Koi fish
    val angle = (timeMs / 800.0)
    val fishX = cx + (rx * 0.55f * cos(angle)).toFloat()
    val fishY = cy + (ry * 0.55f * sin(angle)).toFloat()
    val fishHeading = Math.toDegrees(angle + Math.PI / 2).toFloat()

    rotate(fishHeading, pivot = Offset(fishX, fishY)) {
        // Orange fish body
        drawOval(
            color = Mango,
            topLeft = Offset(fishX - 7f, fishY - 14f),
            size = Size(14f, 28f)
        )
        // White belly patch
        drawCircle(color = WhitePure, radius = 4f, center = Offset(fishX, fishY - 3f))
    }
}

private fun DrawScope.drawTreeSwing(w: Float, h: Float, timeMs: Long) {
    val swayAngle = (sin(timeMs / 450.0) * 5.0).toFloat()
    val pivotX = w * 0.5f
    val pivotY = h * 0.15f

    // Tree branch
    drawLine(
        color = Color(0xFF5C4033),
        start = Offset(w * 0.1f, pivotY),
        end = Offset(w * 0.9f, pivotY),
        strokeWidth = 10f,
        cap = StrokeCap.Round
    )

    // Branch leaves
    drawCircle(color = Mint, radius = w * 0.15f, center = Offset(w * 0.25f, pivotY - 8f))
    drawCircle(color = Color(0xFF2D6A4F), radius = w * 0.13f, center = Offset(w * 0.75f, pivotY - 6f))

    rotate(swayAngle, pivot = Offset(pivotX, pivotY)) {
        val seatY = h * 0.78f
        val ropeLeft = pivotX - w * 0.18f
        val ropeRight = pivotX + w * 0.18f

        // Ropes
        drawLine(color = Color(0xFFC7A75C), start = Offset(ropeLeft, pivotY), end = Offset(ropeLeft, seatY), strokeWidth = 3f)
        drawLine(color = Color(0xFFC7A75C), start = Offset(ropeRight, pivotY), end = Offset(ropeRight, seatY), strokeWidth = 3f)

        // Wooden seat plank
        drawRoundRect(
            color = Color(0xFF8B5A2B),
            topLeft = Offset(ropeLeft - 10f, seatY),
            size = Size((ropeRight - ropeLeft) + 20f, 10f),
            cornerRadius = CornerRadius(3f, 3f)
        )
    }
}

private fun DrawScope.drawFlowerBed(w: Float, h: Float, timeMs: Long) {
    val moundY = h * 0.65f
    // Soil mound
    drawOval(
        color = Color(0xFF5A3921),
        topLeft = Offset(w * 0.08f, moundY - 16f),
        size = Size(w * 0.84f, h * 0.35f)
    )

    // Colorful flowers
    val colors = listOf(Coral, Mango, Grape, Color(0xFFFF4081))
    for (i in 0..5) {
        val fx = w * 0.20f + (i * w * 0.12f)
        val fy = moundY - 10f + (sin(i * 1.5) * 8f).toFloat()

        // Green stem
        drawLine(color = Color(0xFF2D6A4F), start = Offset(fx, moundY), end = Offset(fx, fy), strokeWidth = 3f)
        // Petals
        drawCircle(color = colors[i % colors.size], radius = 9f, center = Offset(fx, fy))
        // Center yellow pistil
        drawCircle(color = Color(0xFFFFEE55), radius = 3.5f, center = Offset(fx, fy))
    }

    // Fluttering butterfly
    val bX = w * 0.5f + (cos(timeMs / 400.0) * w * 0.25f).toFloat()
    val bY = h * 0.30f + (sin(timeMs / 300.0) * h * 0.10f).toFloat()
    drawCircle(color = Color(0xFFFFD54F), radius = 6f, center = Offset(bX - 5f, bY))
    drawCircle(color = Color(0xFFFFD54F), radius = 6f, center = Offset(bX + 5f, bY))
}

private fun DrawScope.drawFirepit(w: Float, h: Float, timeMs: Long) {
    val cx = w * 0.5f
    val cy = h * 0.65f

    // Stone ring
    for (i in 0..7) {
        val angle = i * (Math.PI * 2 / 8)
        val sx = cx + (w * 0.32f * cos(angle)).toFloat()
        val sy = cy + (h * 0.22f * sin(angle)).toFloat()
        drawCircle(color = Color(0xFF7E7265), radius = 11f, center = Offset(sx, sy))
    }

    // Crossed logs
    drawLine(color = Color(0xFF4E2F1D), start = Offset(cx - 24f, cy - 8f), end = Offset(cx + 24f, cy + 8f), strokeWidth = 8f)
    drawLine(color = Color(0xFF4E2F1D), start = Offset(cx - 20f, cy + 10f), end = Offset(cx + 20f, cy - 10f), strokeWidth = 8f)

    // Animated dancing fire flames
    val flicker = (sin(timeMs / 120.0) * 6f).toFloat()
    val flamePath = Path().apply {
        moveTo(cx - 16f, cy + 4f)
        cubicTo(cx - 18f, cy - 14f, cx - 8f, cy - 28f - flicker, cx, cy - 40f - flicker)
        cubicTo(cx + 8f, cy - 28f - flicker, cx + 18f, cy - 14f, cx + 16f, cy + 4f)
        close()
    }
    drawPath(flamePath, Mango)

    // Inner bright yellow flame core
    val corePath = Path().apply {
        moveTo(cx - 8f, cy + 4f)
        cubicTo(cx - 10f, cy - 8f, cx, cy - 22f - flicker * 0.5f, cx, cy - 26f - flicker * 0.5f)
        cubicTo(cx, cy - 22f - flicker * 0.5f, cx + 10f, cy - 8f, cx + 8f, cy + 4f)
        close()
    }
    drawPath(corePath, Color(0xFFFFF176))
}

private fun DrawScope.drawHammock(w: Float, h: Float, timeMs: Long) {
    val sway = (sin(timeMs / 500.0) * 4f).toFloat()

    // Dual wooden support posts
    drawLine(color = Color(0xFF8B5A2B), start = Offset(w * 0.15f, h * 0.25f), end = Offset(w * 0.15f, h * 0.85f), strokeWidth = 7f)
    drawLine(color = Color(0xFF8B5A2B), start = Offset(w * 0.85f, h * 0.25f), end = Offset(w * 0.85f, h * 0.85f), strokeWidth = 7f)

    // Curved hammock cloth
    val hammockPath = Path().apply {
        moveTo(w * 0.15f, h * 0.40f)
        cubicTo(w * 0.35f, h * 0.75f + sway, w * 0.65f, h * 0.75f + sway, w * 0.85f, h * 0.40f)
        cubicTo(w * 0.65f, h * 0.68f + sway, w * 0.35f, h * 0.68f + sway, w * 0.15f, h * 0.40f)
        close()
    }
    drawPath(hammockPath, Mint)
    drawPath(hammockPath, Ink, style = Stroke(width = 2.5f))
}

private fun DrawScope.drawGlowLanterns(w: Float, h: Float, timeMs: Long) {
    val cx = w * 0.5f
    val postBottom = h * 0.85f

    // Iron post
    drawLine(color = Ink, start = Offset(cx, h * 0.25f), end = Offset(cx, postBottom), strokeWidth = 5f)
    // Crossbar
    drawLine(color = Ink, start = Offset(cx - w * 0.25f, h * 0.28f), end = Offset(cx + w * 0.25f, h * 0.28f), strokeWidth = 4f)

    // Hanging lanterns on left and right
    val lanternPositions = listOf(cx - w * 0.22f, cx + w * 0.22f)
    val pulse = (sin(timeMs / 250.0) * 0.15f + 0.85f).toFloat()

    for (lx in lanternPositions) {
        val ly = h * 0.48f
        // Hanging chain
        drawLine(color = Ink, start = Offset(lx, h * 0.28f), end = Offset(lx, ly - 14f), strokeWidth = 2f)

        // Radial glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFEE55).copy(alpha = 0.5f * pulse), Color.Transparent),
                center = Offset(lx, ly),
                radius = 32f
            ),
            radius = 32f,
            center = Offset(lx, ly)
        )

        // Paper lantern body
        drawOval(
            color = Coral,
            topLeft = Offset(lx - 12f, ly - 14f),
            size = Size(24f, 28f)
        )
        drawOval(
            color = Color(0xFFFFEE55),
            topLeft = Offset(lx - 6f, ly - 8f),
            size = Size(12f, 16f)
        )
    }
}

private fun DrawScope.drawOasisKite(w: Float, h: Float, timeMs: Long) {
    val sway = (sin(timeMs / 350.0) * 12f).toFloat()
    val kx = w * 0.55f + sway * 0.6f
    val ky = h * 0.32f + sway * 0.3f

    // Tether line
    drawLine(
        color = Color(0xFFC7A75C),
        start = Offset(w * 0.20f, h * 0.85f),
        end = Offset(kx, ky + 18f),
        strokeWidth = 2.5f
    )

    // Diamond kite
    val kw = 36f
    val kh = 46f
    val kitePath = Path().apply {
        moveTo(kx, ky - kh * 0.5f)
        lineTo(kx + kw * 0.5f, ky)
        lineTo(kx, ky + kh * 0.5f)
        lineTo(kx - kw * 0.5f, ky)
        close()
    }

    drawPath(kitePath, Mango)
    drawPath(kitePath, Ink, style = Stroke(width = 2.5f))

    // Kite tail with bows
    val tailPath = Path().apply {
        moveTo(kx, ky + kh * 0.5f)
        cubicTo(kx - 10f + sway, ky + kh + 10f, kx + 10f + sway, ky + kh + 25f, kx + sway, ky + kh + 40f)
    }
    drawPath(tailPath, Color(0xFFC7A75C), style = Stroke(width = 2f))
    drawCircle(color = Coral, radius = 4f, center = Offset(kx + sway * 0.5f, ky + kh + 20f))
}

private fun DrawScope.drawRangoliAndMangoTree(w: Float, h: Float, timeMs: Long) {
    val cx = w * 0.5f
    val groundY = h * 0.65f

    // 1. Mango Tree on left
    val treeX = w * 0.30f
    // Trunk
    drawLine(color = Color(0xFF5C4033), start = Offset(treeX, groundY), end = Offset(treeX, groundY - 45f), strokeWidth = 14f, cap = StrokeCap.Round)
    // Foliage crown
    drawCircle(color = Color(0xFF2D6A4F), radius = 32f, center = Offset(treeX, groundY - 55f))
    drawCircle(color = Mint, radius = 24f, center = Offset(treeX + 10f, groundY - 65f))
    // Golden ripe mangoes hanging
    drawCircle(color = Mango, radius = 5f, center = Offset(treeX - 12f, groundY - 50f))
    drawCircle(color = Mango, radius = 5f, center = Offset(treeX + 14f, groundY - 45f))
    drawCircle(color = Mango, radius = 5f, center = Offset(treeX, groundY - 65f))

    // 2. Intricate Rangoli floor art on right
    val rx = w * 0.72f
    val ry = groundY + 4f
    val rangoliRadius = 24f

    // Outer flower petals
    for (i in 0..7) {
        val angle = i * (Math.PI * 2 / 8)
        val px = rx + (rangoliRadius * cos(angle)).toFloat()
        val py = ry + (rangoliRadius * 0.6f * sin(angle)).toFloat()
        drawCircle(color = Coral, radius = 6f, center = Offset(px, py))
    }

    // Inner star circle
    drawCircle(color = Mango, radius = 12f, center = Offset(rx, ry))
    drawCircle(color = WhitePure, radius = 6f, center = Offset(rx, ry))
}

