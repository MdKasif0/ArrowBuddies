package com.kasif.arrowbuddies.core.buddies.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.kasif.arrowbuddies.core.buddies.model.BuddyExpression
import com.kasif.arrowbuddies.core.buddies.model.SpecialFeature
import com.kasif.arrowbuddies.engine.model.Direction
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders the expressive facial features, big glossy eyes, blush cheeks,
 * and emotional overlays for all Buddy expressions.
 */
object BuddyFaceRenderer {

    fun drawFace(
        drawScope: DrawScope,
        ctx: BuddyDrawContext,
        direction: Direction,
        expression: BuddyExpression,
        isBlinking: Boolean,
        timeMs: Long
    ) {
        with(drawScope) {
            val eyeR = ctx.bodyRadius * 0.17f
            val eyeSpacing = ctx.bodyRadius * 0.38f
            val eyeY = ctx.centerY - ctx.bodyRadius * 0.12f

            val leftEyeCenter = Offset(ctx.centerX - eyeSpacing, eyeY)
            val rightEyeCenter = Offset(ctx.centerX + eyeSpacing, eyeY)

            // Look offset based on movement direction
            val lookOffset = Offset(
                x = direction.dx * eyeR * 0.38f,
                y = direction.dy * eyeR * 0.38f
            )

            // 1. Blush Cheeks (drawn beneath eyes for soft toy feel)
            val blushR = ctx.bodyRadius * 0.18f
            val blushY = eyeY + eyeR * 1.35f
            val blushColor = Color(0xFFFF6B8B).copy(alpha = 0.42f)
            drawOval(
                color = blushColor,
                topLeft = Offset(ctx.centerX - eyeSpacing * 1.38f - blushR, blushY - blushR * 0.6f),
                size = androidx.compose.ui.geometry.Size(blushR * 2, blushR * 1.2f)
            )
            drawOval(
                color = blushColor,
                topLeft = Offset(ctx.centerX + eyeSpacing * 1.38f - blushR, blushY - blushR * 0.6f),
                size = androidx.compose.ui.geometry.Size(blushR * 2, blushR * 1.2f)
            )

            // 2. Eyes & Eyebrows per expression
            when (expression) {
                BuddyExpression.IDLE, BuddyExpression.READY -> {
                    if (isBlinking) {
                        drawClosedEyeArc(this, leftEyeCenter, eyeR, ctx.outlineColor, ctx.outlineWidth)
                        drawClosedEyeArc(this, rightEyeCenter, eyeR, ctx.outlineColor, ctx.outlineWidth)
                    } else {
                        drawGlossyEye(this, leftEyeCenter, eyeR, lookOffset, ctx.outlineColor)
                        drawGlossyEye(this, rightEyeCenter, eyeR, lookOffset, ctx.outlineColor)
                    }

                    if (expression == BuddyExpression.READY) {
                        // Confident focused angled brows (\ /)
                        val browW = eyeR * 1.2f
                        val browY = eyeY - eyeR * 1.35f
                        drawLine(
                            color = ctx.outlineColor,
                            start = Offset(leftEyeCenter.x - browW * 0.5f, browY - eyeR * 0.2f),
                            end = Offset(leftEyeCenter.x + browW * 0.5f, browY + eyeR * 0.2f),
                            strokeWidth = ctx.outlineWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = ctx.outlineColor,
                            start = Offset(rightEyeCenter.x - browW * 0.5f, browY + eyeR * 0.2f),
                            end = Offset(rightEyeCenter.x + browW * 0.5f, browY - eyeR * 0.2f),
                            strokeWidth = ctx.outlineWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }

                BuddyExpression.WORRIED -> {
                    // Wide nervous eyes + wavy raised brows + sweat drop
                    drawGlossyEye(this, leftEyeCenter, eyeR * 1.1f, lookOffset, ctx.outlineColor)
                    drawGlossyEye(this, rightEyeCenter, eyeR * 1.1f, lookOffset, ctx.outlineColor)

                    // Raised worried wavy brows
                    val browY = eyeY - eyeR * 1.55f
                    val browW = eyeR * 1.2f
                    drawLine(
                        color = ctx.outlineColor,
                        start = Offset(leftEyeCenter.x - browW * 0.5f, browY + eyeR * 0.25f),
                        end = Offset(leftEyeCenter.x + browW * 0.5f, browY - eyeR * 0.15f),
                        strokeWidth = ctx.outlineWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = ctx.outlineColor,
                        start = Offset(rightEyeCenter.x - browW * 0.5f, browY - eyeR * 0.15f),
                        end = Offset(rightEyeCenter.x + browW * 0.5f, browY + eyeR * 0.25f),
                        strokeWidth = ctx.outlineWidth,
                        cap = StrokeCap.Round
                    )

                    // Small cyan sweat drop on forehead
                    drawSweatDrop(this, Offset(ctx.centerX + eyeSpacing * 0.9f, eyeY - eyeR * 1.8f), eyeR * 0.7f)
                }

                BuddyExpression.DASHING -> {
                    // Focused squinting action eyes
                    drawSquintEye(this, leftEyeCenter, eyeR, ctx.outlineColor, ctx.outlineWidth)
                    drawSquintEye(this, rightEyeCenter, eyeR, ctx.outlineColor, ctx.outlineWidth)

                    // Trailing speed streak lines behind buddy
                    drawSpeedLines(this, ctx, direction, timeMs)
                }

                BuddyExpression.BONKED -> {
                    // Cartoon spiral swirly eyes (@ @)
                    drawSpiralEye(this, leftEyeCenter, eyeR * 0.95f, ctx.outlineColor, ctx.outlineWidth, timeMs)
                    drawSpiralEye(this, rightEyeCenter, eyeR * 0.95f, ctx.outlineColor, ctx.outlineWidth, timeMs)

                    // 3 orbiting yellow stars above head
                    drawOrbitingStars(this, ctx, timeMs)
                }

                BuddyExpression.HAPPY -> {
                    // Upward happy crescent arc eyes (^_^)
                    drawHappyEyeArc(this, leftEyeCenter, eyeR, ctx.outlineColor, ctx.outlineWidth)
                    drawHappyEyeArc(this, rightEyeCenter, eyeR, ctx.outlineColor, ctx.outlineWidth)

                    // Floating pink celebration hearts
                    drawFloatingHearts(this, ctx, timeMs)
                }

                BuddyExpression.SLEEPY -> {
                    // Closed peaceful sleeping lines
                    drawSleepingEye(this, leftEyeCenter, eyeR, ctx.outlineColor, ctx.outlineWidth)
                    drawSleepingEye(this, rightEyeCenter, eyeR, ctx.outlineColor, ctx.outlineWidth)

                    // Floating Zzz letters
                    drawFloatingZzz(this, ctx, timeMs)
                }

                BuddyExpression.CAGED -> {
                    // Sad glossy downturned eyes with pooling tear shimmer
                    drawGlossyEye(this, leftEyeCenter, eyeR, Offset(0f, eyeR * 0.25f), ctx.outlineColor)
                    drawGlossyEye(this, rightEyeCenter, eyeR, Offset(0f, eyeR * 0.25f), ctx.outlineColor)

                    // Sad curved brows
                    val browY = eyeY - eyeR * 1.35f
                    val browW = eyeR * 1.1f
                    drawLine(
                        color = ctx.outlineColor,
                        start = Offset(leftEyeCenter.x - browW * 0.5f, browY),
                        end = Offset(leftEyeCenter.x + browW * 0.5f, browY - eyeR * 0.25f),
                        strokeWidth = ctx.outlineWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = ctx.outlineColor,
                        start = Offset(rightEyeCenter.x - browW * 0.5f, browY - eyeR * 0.25f),
                        end = Offset(rightEyeCenter.x + browW * 0.5f, browY),
                        strokeWidth = ctx.outlineWidth,
                        cap = StrokeCap.Round
                    )

                    // Tear drop shimmer
                    drawCircle(
                        color = Color(0xFF70A1FF).copy(alpha = 0.85f),
                        radius = eyeR * 0.35f,
                        center = Offset(leftEyeCenter.x - eyeR * 0.4f, eyeY + eyeR * 1.1f)
                    )
                }
            }

            // 3. Mouth & Snout/Beak
            drawMouth(this, ctx, expression)
        }
    }

    private fun drawGlossyEye(
        drawScope: DrawScope,
        center: Offset,
        radius: Float,
        lookOffset: Offset,
        outlineColor: Color
    ) {
        with(drawScope) {
            // White sclera
            drawCircle(color = Color.White, radius = radius, center = center)

            // Large dark pupil
            val pupilR = radius * 0.82f
            val pupilCenter = center + lookOffset
            drawCircle(color = outlineColor, radius = pupilR, center = pupilCenter)

            // Specular Highlight 1 (large dot, top-left)
            val h1R = pupilR * 0.40f
            val h1Center = pupilCenter - Offset(pupilR * 0.32f, pupilR * 0.32f)
            drawCircle(color = Color.White, radius = h1R, center = h1Center)

            // Specular Highlight 2 (small dot, bottom-right)
            val h2R = pupilR * 0.20f
            val h2Center = pupilCenter + Offset(pupilR * 0.35f, pupilR * 0.35f)
            drawCircle(color = Color.White, radius = h2R, center = h2Center)
        }
    }

    private fun drawClosedEyeArc(
        drawScope: DrawScope,
        center: Offset,
        radius: Float,
        color: Color,
        strokeWidth: Float
    ) {
        with(drawScope) {
            val arcPath = Path().apply {
                moveTo(center.x - radius, center.y)
                quadraticTo(center.x, center.y + radius * 0.6f, center.x + radius, center.y)
            }
            drawPath(arcPath, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
        }
    }

    private fun drawHappyEyeArc(
        drawScope: DrawScope,
        center: Offset,
        radius: Float,
        color: Color,
        strokeWidth: Float
    ) {
        with(drawScope) {
            val arcPath = Path().apply {
                moveTo(center.x - radius, center.y + radius * 0.2f)
                quadraticTo(center.x, center.y - radius * 0.7f, center.x + radius, center.y + radius * 0.2f)
            }
            drawPath(arcPath, color = color, style = Stroke(width = strokeWidth * 1.25f, cap = StrokeCap.Round))
        }
    }

    private fun drawSquintEye(
        drawScope: DrawScope,
        center: Offset,
        radius: Float,
        color: Color,
        strokeWidth: Float
    ) {
        with(drawScope) {
            drawLine(
                color = color,
                start = Offset(center.x - radius * 1.1f, center.y - radius * 0.2f),
                end = Offset(center.x + radius * 1.1f, center.y + radius * 0.2f),
                strokeWidth = strokeWidth * 1.3f,
                cap = StrokeCap.Round
            )
        }
    }

    private fun drawSpiralEye(
        drawScope: DrawScope,
        center: Offset,
        radius: Float,
        color: Color,
        strokeWidth: Float,
        timeMs: Long
    ) {
        with(drawScope) {
            drawCircle(color = Color.White, radius = radius, center = center)
            val spiralPath = Path()
            val turns = 2.2f
            val steps = 30
            val angleOffset = (timeMs / 400f) % (2 * PI.toFloat())

            for (i in 0..steps) {
                val progress = i / steps.toFloat()
                val r = radius * 0.75f * progress
                val angle = progress * turns * 2 * PI.toFloat() + angleOffset
                val px = center.x + (cos(angle) * r)
                val py = center.y + (sin(angle) * r)
                if (i == 0) spiralPath.moveTo(px, py) else spiralPath.lineTo(px, py)
            }
            drawPath(spiralPath, color = color, style = Stroke(width = strokeWidth * 0.85f, cap = StrokeCap.Round))
        }
    }

    private fun drawSleepingEye(
        drawScope: DrawScope,
        center: Offset,
        radius: Float,
        color: Color,
        strokeWidth: Float
    ) {
        with(drawScope) {
            val sleepPath = Path().apply {
                moveTo(center.x - radius, center.y)
                quadraticTo(center.x, center.y + radius * 0.4f, center.x + radius, center.y)
            }
            drawPath(sleepPath, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
        }
    }

    private fun drawMouth(
        drawScope: DrawScope,
        ctx: BuddyDrawContext,
        expression: BuddyExpression
    ) {
        with(drawScope) {
            // If the species has a snout or beak, mouth is customized or integrated
            if (ctx.species.specialFeature == SpecialFeature.BEAK_CHICK ||
                ctx.species.specialFeature == SpecialFeature.BEAK_PARROT ||
                ctx.species.specialFeature == SpecialFeature.BEAK_PENGUIN ||
                ctx.species.specialFeature == SpecialFeature.BEAK_OWL ||
                ctx.species.specialFeature == SpecialFeature.ELEPHANT_TRUNK
            ) {
                return
            }

            val mouthY = ctx.centerY + ctx.bodyRadius * 0.18f
            val mouthW = ctx.bodyRadius * 0.22f

            when (expression) {
                BuddyExpression.IDLE, BuddyExpression.READY -> {
                    // Cute cat 'w' smile
                    val wPath = Path().apply {
                        moveTo(ctx.centerX - mouthW, mouthY - mouthW * 0.2f)
                        quadraticTo(ctx.centerX - mouthW * 0.5f, mouthY + mouthW * 0.4f, ctx.centerX, mouthY)
                        quadraticTo(ctx.centerX + mouthW * 0.5f, mouthY + mouthW * 0.4f, ctx.centerX + mouthW, mouthY - mouthW * 0.2f)
                    }
                    drawPath(wPath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth, cap = StrokeCap.Round))
                }

                BuddyExpression.HAPPY -> {
                    // Open joyful bean mouth
                    val openMouthPath = Path().apply {
                        moveTo(ctx.centerX - mouthW * 0.8f, mouthY)
                        quadraticTo(ctx.centerX, mouthY + mouthW * 1.1f, ctx.centerX + mouthW * 0.8f, mouthY)
                        close()
                    }
                    drawPath(openMouthPath, color = Color(0xFFFF6B6B), style = Fill)
                    drawPath(openMouthPath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))
                }

                BuddyExpression.WORRIED, BuddyExpression.CAGED -> {
                    // Small trembling sad mouth
                    val sadPath = Path().apply {
                        moveTo(ctx.centerX - mouthW * 0.7f, mouthY + mouthW * 0.3f)
                        quadraticTo(ctx.centerX, mouthY - mouthW * 0.2f, ctx.centerX + mouthW * 0.7f, mouthY + mouthW * 0.3f)
                    }
                    drawPath(sadPath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth, cap = StrokeCap.Round))
                }

                BuddyExpression.BONKED -> {
                    // Dizzy wavy mouth
                    val wavyPath = Path().apply {
                        moveTo(ctx.centerX - mouthW, mouthY)
                        quadraticTo(ctx.centerX - mouthW * 0.5f, mouthY + mouthW * 0.3f, ctx.centerX, mouthY)
                        quadraticTo(ctx.centerX + mouthW * 0.5f, mouthY - mouthW * 0.3f, ctx.centerX + mouthW, mouthY)
                    }
                    drawPath(wavyPath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth, cap = StrokeCap.Round))
                }

                BuddyExpression.DASHING, BuddyExpression.SLEEPY -> {
                    // Tiny focused line
                    drawLine(
                        color = ctx.outlineColor,
                        start = Offset(ctx.centerX - mouthW * 0.5f, mouthY),
                        end = Offset(ctx.centerX + mouthW * 0.5f, mouthY),
                        strokeWidth = ctx.outlineWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }

    private fun drawSweatDrop(drawScope: DrawScope, center: Offset, size: Float) {
        with(drawScope) {
            val dropPath = Path().apply {
                moveTo(center.x, center.y - size)
                cubicTo(center.x - size * 0.8f, center.y, center.x - size * 0.8f, center.y + size * 0.8f, center.x, center.y + size * 0.8f)
                cubicTo(center.x + size * 0.8f, center.y + size * 0.8f, center.x + size * 0.8f, center.y, center.x, center.y - size)
                close()
            }
            drawPath(dropPath, color = Color(0xFF4CC9F0), style = Fill)
            drawPath(dropPath, color = Color(0xFF2B2240).copy(alpha = 0.6f), style = Stroke(width = 1.2f))
        }
    }

    private fun drawOrbitingStars(drawScope: DrawScope, ctx: BuddyDrawContext, timeMs: Long) {
        with(drawScope) {
            val orbitRadiusX = ctx.bodyRadius * 0.85f
            val orbitRadiusY = ctx.bodyRadius * 0.35f
            val orbitCenter = Offset(ctx.centerX, ctx.centerY - ctx.bodyRadius * 0.95f)

            for (i in 0..2) {
                val starAngle = (timeMs / 500f) + (i * 2 * PI.toFloat() / 3f)
                val starX = orbitCenter.x + cos(starAngle) * orbitRadiusX
                val starY = orbitCenter.y + sin(starAngle) * orbitRadiusY
                draw5PointStar(this, Offset(starX, starY), ctx.bodyRadius * 0.16f, Color(0xFFFFD54F))
            }
        }
    }

    private fun draw5PointStar(drawScope: DrawScope, center: Offset, radius: Float, color: Color) {
        with(drawScope) {
            val starPath = Path()
            val innerR = radius * 0.42f
            for (step in 0..9) {
                val angle = (step * 36f - 90f) * (PI.toFloat() / 180f)
                val r = if (step % 2 == 0) radius else innerR
                val px = center.x + cos(angle) * r
                val py = center.y + sin(angle) * r
                if (step == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
            }
            starPath.close()
            drawPath(starPath, color = color, style = Fill)
            drawPath(starPath, color = Color(0xFF2B2240).copy(alpha = 0.7f), style = Stroke(width = 1.2f))
        }
    }

    private fun drawFloatingHearts(drawScope: DrawScope, ctx: BuddyDrawContext, timeMs: Long) {
        with(drawScope) {
            val heartColor = Color(0xFFFF6B8B)
            for (i in 0..1) {
                val phase = (timeMs + i * 1200) % 2400 / 2400f
                val side = if (i == 0) -1 else 1
                val hx = ctx.centerX + side * (ctx.bodyRadius * 0.85f + phase * ctx.bodyRadius * 0.25f)
                val hy = ctx.centerY - ctx.bodyRadius * 0.2f - (phase * ctx.bodyRadius * 0.75f)
                val hr = ctx.bodyRadius * (0.16f * (1f - phase * 0.4f))

                val heartPath = Path().apply {
                    moveTo(hx, hy + hr * 0.4f)
                    cubicTo(hx - hr, hy - hr * 0.5f, hx - hr * 1.1f, hy + hr * 0.6f, hx, hy + hr * 1.2f)
                    cubicTo(hx + hr * 1.1f, hy + hr * 0.6f, hx + hr, hy - hr * 0.5f, hx, hy + hr * 0.4f)
                    close()
                }
                drawPath(heartPath, color = heartColor.copy(alpha = 1f - phase * 0.7f), style = Fill)
            }
        }
    }

    private fun drawFloatingZzz(drawScope: DrawScope, ctx: BuddyDrawContext, timeMs: Long) {
        with(drawScope) {
            val zColor = Color(0xFF7B61FF)
            for (i in 0..2) {
                val cycle = (timeMs + i * 800) % 2400 / 2400f
                val zx = ctx.centerX + ctx.bodyRadius * 0.45f + (cycle * ctx.bodyRadius * 0.55f)
                val zy = ctx.centerY - ctx.bodyRadius * 0.30f - (cycle * ctx.bodyRadius * 1.15f)
                val zSize = ctx.bodyRadius * (0.18f + i * 0.06f)

                // Draw path "Z"
                val zPath = Path().apply {
                    moveTo(zx, zy)
                    lineTo(zx + zSize, zy)
                    lineTo(zx, zy + zSize)
                    lineTo(zx + zSize, zy + zSize)
                }
                drawPath(
                    zPath,
                    color = zColor.copy(alpha = 1f - cycle * 0.8f),
                    style = Stroke(width = ctx.outlineWidth * 0.9f, cap = StrokeCap.Round)
                )
            }
        }
    }

    private fun drawSpeedLines(
        drawScope: DrawScope,
        ctx: BuddyDrawContext,
        direction: Direction,
        timeMs: Long
    ) {
        with(drawScope) {
            // Trailing opposite to direction
            val streakColor = Color.White.copy(alpha = 0.85f)
            val lineLen = ctx.bodyRadius * 0.65f
            val oppX = -direction.dx.toFloat()
            val oppY = -direction.dy.toFloat()

            for (i in -1..1) {
                val offsetPerp = i * ctx.bodyRadius * 0.35f
                val perpX = -direction.dy.toFloat() * offsetPerp
                val perpY = direction.dx.toFloat() * offsetPerp

                val start = Offset(
                    x = ctx.centerX + (oppX * ctx.bodyRadius * 0.95f) + perpX,
                    y = ctx.centerY + (oppY * ctx.bodyRadius * 0.95f) + perpY
                )
                val end = start + Offset(oppX * lineLen, oppY * lineLen)

                drawLine(
                    color = streakColor,
                    start = start,
                    end = end,
                    strokeWidth = ctx.outlineWidth * 1.2f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
