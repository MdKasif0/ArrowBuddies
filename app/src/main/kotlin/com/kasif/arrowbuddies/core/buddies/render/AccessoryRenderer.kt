package com.kasif.arrowbuddies.core.buddies.render

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.kasif.arrowbuddies.core.designsystem.theme.Coral
import com.kasif.arrowbuddies.core.designsystem.theme.Ink
import com.kasif.arrowbuddies.core.designsystem.theme.Mango
import com.kasif.arrowbuddies.core.designsystem.theme.Mint
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure
import com.kasif.arrowbuddies.core.model.BuddyAccessory

object AccessoryRenderer {

    fun renderAccessory(
        drawScope: DrawScope,
        accessory: BuddyAccessory,
        w: Float,
        h: Float,
        timeMs: Long
    ) {
        if (accessory == BuddyAccessory.NONE) return

        with(drawScope) {
            when (accessory) {
                BuddyAccessory.BOW -> drawBow(w, h)
                BuddyAccessory.SCARF -> drawScarf(w, h)
                BuddyAccessory.CROWN -> drawCrown(w, h, timeMs)
                BuddyAccessory.NONE -> Unit
            }
        }
    }

    private fun DrawScope.drawBow(w: Float, h: Float) {
        // Cute pink/coral bow positioned near top-right of head
        val cx = w * 0.70f
        val cy = h * 0.20f
        val bowSize = w * 0.24f

        val leftLoop = Path().apply {
            moveTo(cx, cy)
            cubicTo(cx - bowSize, cy - bowSize * 0.7f, cx - bowSize, cy + bowSize * 0.7f, cx, cy)
            close()
        }
        val rightLoop = Path().apply {
            moveTo(cx, cy)
            cubicTo(cx + bowSize, cy - bowSize * 0.7f, cx + bowSize, cy + bowSize * 0.7f, cx, cy)
            close()
        }

        // Drop shadow
        drawCircle(color = Ink.copy(alpha = 0.2f), radius = bowSize * 0.6f, center = Offset(cx, cy + 3f))

        // Left & right loops
        drawPath(leftLoop, Coral)
        drawPath(leftLoop, Ink, style = Stroke(width = 2.5f))
        drawPath(rightLoop, Coral)
        drawPath(rightLoop, Ink, style = Stroke(width = 2.5f))

        // Center knot
        drawCircle(color = Color(0xFFFF4081), radius = bowSize * 0.32f, center = Offset(cx, cy))
        drawCircle(color = Ink, radius = bowSize * 0.32f, center = Offset(cx, cy), style = Stroke(width = 2.5f))

        // Specular highlight
        drawCircle(color = WhitePure.copy(alpha = 0.8f), radius = bowSize * 0.12f, center = Offset(cx - 2f, cy - 2f))
    }

    private fun DrawScope.drawScarf(w: Float, h: Float) {
        // Warm knitted scarf wrapped around neck/chest with dangling tail
        val cx = w * 0.5f
        val cy = h * 0.72f
        val scarfW = w * 0.65f
        val scarfH = h * 0.16f

        // Shadow under scarf
        drawRoundRect(
            color = Ink.copy(alpha = 0.2f),
            topLeft = Offset(cx - scarfW * 0.5f, cy - scarfH * 0.35f + 3f),
            size = Size(scarfW, scarfH),
            cornerRadius = CornerRadius(scarfH * 0.5f, scarfH * 0.5f)
        )

        // Main collar wrap
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Mint, Color(0xFF26B685)),
                startY = cy - scarfH * 0.5f,
                endY = cy + scarfH * 0.5f
            ),
            topLeft = Offset(cx - scarfW * 0.5f, cy - scarfH * 0.5f),
            size = Size(scarfW, scarfH),
            cornerRadius = CornerRadius(scarfH * 0.5f, scarfH * 0.5f)
        )
        drawRoundRect(
            color = Ink,
            topLeft = Offset(cx - scarfW * 0.5f, cy - scarfH * 0.5f),
            size = Size(scarfW, scarfH),
            cornerRadius = CornerRadius(scarfH * 0.5f, scarfH * 0.5f),
            style = Stroke(width = 2.5f)
        )

        // Dangling scarf tail on right
        val tailLeft = cx + scarfW * 0.15f
        val tailTop = cy + scarfH * 0.25f
        val tailW = scarfW * 0.28f
        val tailH = h * 0.22f

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Mint, Color(0xFF26B685)),
                startY = tailTop,
                endY = tailTop + tailH
            ),
            topLeft = Offset(tailLeft, tailTop),
            size = Size(tailW, tailH),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = Ink,
            topLeft = Offset(tailLeft, tailTop),
            size = Size(tailW, tailH),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = 2.5f)
        )

        // Scarf fringe lines
        for (i in 0..3) {
            val fx = tailLeft + (tailW * 0.25f * i) + 2f
            drawLine(
                color = Ink,
                start = Offset(fx, tailTop + tailH),
                end = Offset(fx, tailTop + tailH + 6f),
                strokeWidth = 2f
            )
        }
    }

    private fun DrawScope.drawCrown(w: Float, h: Float, timeMs: Long) {
        // 3D Regal Golden Crown on top of head
        val cx = w * 0.5f
        val baseTop = h * 0.14f
        val crownW = w * 0.45f
        val crownH = h * 0.22f

        val crownPath = Path().apply {
            moveTo(cx - crownW * 0.5f, baseTop)
            lineTo(cx - crownW * 0.55f, baseTop - crownH * 0.9f) // left peak
            lineTo(cx - crownW * 0.20f, baseTop - crownH * 0.4f) // left valley
            lineTo(cx, baseTop - crownH * 1.15f) // center peak (tallest)
            lineTo(cx + crownW * 0.20f, baseTop - crownH * 0.4f) // right valley
            lineTo(cx + crownW * 0.55f, baseTop - crownH * 0.9f) // right peak
            lineTo(cx + crownW * 0.5f, baseTop)
            close()
        }

        // Shadow under crown
        drawPath(crownPath, Ink.copy(alpha = 0.2f))

        // Gold gradient fill
        drawPath(
            path = crownPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFEE55), Mango, Color(0xFFD48800)),
                startY = baseTop - crownH * 1.15f,
                endY = baseTop
            )
        )
        // Outline
        drawPath(path = crownPath, color = Ink, style = Stroke(width = 2.8f))

        // Crown base band
        drawRoundRect(
            color = Color(0xFFFFD54F),
            topLeft = Offset(cx - crownW * 0.5f, baseTop - 4f),
            size = Size(crownW, 8f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = Ink,
            topLeft = Offset(cx - crownW * 0.5f, baseTop - 4f),
            size = Size(crownW, 8f),
            cornerRadius = CornerRadius(3f, 3f),
            style = Stroke(width = 2f)
        )

        // Jewels on peaks
        val rubyColor = Color(0xFFFF1744)
        drawCircle(color = rubyColor, radius = 3.5f, center = Offset(cx - crownW * 0.55f, baseTop - crownH * 0.9f))
        drawCircle(color = rubyColor, radius = 4.5f, center = Offset(cx, baseTop - crownH * 1.15f))
        drawCircle(color = rubyColor, radius = 3.5f, center = Offset(cx + crownW * 0.55f, baseTop - crownH * 0.9f))

        // Subtle glint sparkle on center jewel
        val shimmer = (kotlin.math.sin(timeMs / 250.0).toFloat() * 0.5f) + 0.5f
        drawCircle(color = WhitePure.copy(alpha = 0.8f * shimmer), radius = 2f, center = Offset(cx - 1.5f, baseTop - crownH * 1.15f - 1.5f))
    }
}

