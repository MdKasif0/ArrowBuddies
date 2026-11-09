package com.kasif.arrowbuddies.core.buddies.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.kasif.arrowbuddies.core.buddies.model.BuddyExpression
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.buddies.model.SpecialFeature
import com.kasif.arrowbuddies.engine.model.Direction
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural Vector Buddy Renderer.
 * Draws cute, tactile animal buddies with distinct silhouettes entirely with DrawScope vector math.
 */
object BuddyRenderer {

    fun renderBuddy(
        drawScope: DrawScope,
        ctx: BuddyDrawContext,
        direction: Direction,
        expression: BuddyExpression,
        timeMs: Long,
        phaseSeed: Int = 0,
        animatedAngleDegrees: Float = direction.angleDegrees
    ) {
        with(drawScope) {
            val localTime = timeMs + phaseSeed

            // 1. Idle Life & Charm Math (Breathing, Bob, Blinking, Ear Twitch)
            val breatheCycle = (localTime % 2600) / 2600f
            val breatheScale = if (expression == BuddyExpression.IDLE) {
                1.0f + 0.025f * sin(breatheCycle * 2 * PI).toFloat()
            } else if (expression == BuddyExpression.DASHING) {
                0.90f // Squash
            } else {
                1.0f
            }

            val bobY = if (expression == BuddyExpression.IDLE) {
                (sin(breatheCycle * 2 * PI) * ctx.minDim * 0.015f).toFloat()
            } else {
                0f
            }

            // Blinking logic: blinks every ~3800ms for 140ms
            val blinkCycle = localTime % 3800
            val isBlinking = (expression == BuddyExpression.IDLE) && (blinkCycle in 3660..3800)

            // Ear twitch: subtle 5-degree twitch every 4400ms for 160ms
            val twitchCycle = localTime % 4400
            val earTwitchAngle = if (expression == BuddyExpression.IDLE && twitchCycle in 4240..4400) {
                (sin((twitchCycle - 4240) / 160f * 2 * PI) * 5f).toFloat()
            } else {
                0f
            }

            // Directional Lean (6 degrees towards movement direction)
            val leanAngle = when (direction) {
                Direction.LEFT -> -6f
                Direction.RIGHT -> 6f
                else -> 0f
            }

            // 2. Draw Contact Shadow (unscaled contact base)
            drawPath(
                path = ctx.shadowPath,
                color = Color.Black.copy(alpha = 0.16f),
                style = Fill
            )

            // 3. Render Body with Breathing, Bob, and Lean transforms
            translate(left = 0f, top = bobY) {
                rotate(degrees = leanAngle, pivot = Offset(ctx.centerX, ctx.centerY)) {
                    scale(
                        scaleX = breatheScale,
                        scaleY = breatheScale,
                        pivot = Offset(ctx.centerX, ctx.centerY + ctx.bodyRadius * 0.5f)
                    ) {
                        // A. Back Anatomy: Peacock Fan / Hedgehog Quills / Camel Hump / Tail
                        drawBackFeatures(this, ctx)

                        // B. Ears with occasional twitch
                        drawEars(this, ctx, earTwitchAngle)

                        // C. Main Body Blob (with rich radial gradient and 1.5dp dark outline)
                        drawBodyBlob(this, ctx)

                        // D. Belly Patch
                        drawBelly(this, ctx)

                        // E. Species Special Anatomical Marks (Stripes, Masks, Snouts, Beaks, Claws)
                        drawSpecialFeatures(this, ctx)

                        // F. Expressive Face & Emotion Overlays
                        BuddyFaceRenderer.drawFace(
                            drawScope = this,
                            ctx = ctx,
                            direction = direction,
                            expression = expression,
                            isBlinking = isBlinking,
                            timeMs = localTime
                        )
                    }
                }
            }

            // 4. Direction Badge (Rendered on top for gameplay readability at 36dp)
            DirectionBadgeRenderer.drawBadge(
                drawScope = this,
                ctx = ctx,
                direction = direction,
                animatedAngleDegrees = animatedAngleDegrees,
                accentColor = ctx.species.accentColor
            )
        }
    }

    private fun drawBackFeatures(drawScope: DrawScope, ctx: BuddyDrawContext) {
        with(drawScope) {
            // Tail
            if (!ctx.tailPath.isEmpty) {
                drawPath(
                    path = ctx.tailPath,
                    color = ctx.species.bodyColor,
                    style = Fill
                )
                drawPath(
                    path = ctx.tailPath,
                    color = ctx.outlineColor,
                    style = Stroke(width = ctx.outlineWidth)
                )
            }

            // Camel Hump or Hedgehog Quills or Turtle Shell
            when (ctx.species.specialFeature) {
                SpecialFeature.CAMEL_HUMP -> {
                    drawPath(ctx.specialFeaturePath, color = ctx.species.bodyColor, style = Fill)
                    drawPath(ctx.specialFeaturePath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))
                }
                SpecialFeature.HEDGEHOG_QUILLS -> {
                    drawPath(ctx.specialFeaturePath, color = ctx.species.accentColor, style = Fill)
                    drawPath(ctx.specialFeaturePath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))
                }
                SpecialFeature.TURTLE_SHELL -> {
                    // Turtle flippers
                    drawPath(ctx.specialFeaturePath, color = ctx.species.bodyColor, style = Fill)
                    drawPath(ctx.specialFeaturePath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))
                }
                SpecialFeature.PEACOCK_CREST -> {
                    // Crest bobbles
                    drawPath(ctx.specialFeaturePath, color = ctx.species.accentColor, style = Stroke(width = ctx.outlineWidth * 1.2f, cap = StrokeCap.Round))
                }
                else -> {}
            }
        }
    }

    private fun drawEars(drawScope: DrawScope, ctx: BuddyDrawContext, twitchAngle: Float) {
        with(drawScope) {
            // Left Ear
            if (!ctx.leftEarPath.isEmpty) {
                rotate(degrees = -twitchAngle, pivot = Offset(ctx.centerX - ctx.bodyRadius * 0.5f, ctx.centerY - ctx.bodyRadius * 0.5f)) {
                    drawPath(ctx.leftEarPath, color = ctx.species.bodyColor, style = Fill)
                    if (!ctx.leftInnerEarPath.isEmpty) {
                        drawPath(ctx.leftInnerEarPath, color = ctx.species.accentColor, style = Fill)
                    }
                    drawPath(ctx.leftEarPath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))
                }
            }

            // Right Ear
            if (!ctx.rightEarPath.isEmpty) {
                rotate(degrees = twitchAngle, pivot = Offset(ctx.centerX + ctx.bodyRadius * 0.5f, ctx.centerY - ctx.bodyRadius * 0.5f)) {
                    drawPath(ctx.rightEarPath, color = ctx.species.bodyColor, style = Fill)
                    if (!ctx.rightInnerEarPath.isEmpty) {
                        drawPath(ctx.rightInnerEarPath, color = ctx.species.accentColor, style = Fill)
                    }
                    drawPath(ctx.rightEarPath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))
                }
            }
        }
    }

    private fun drawBodyBlob(drawScope: DrawScope, ctx: BuddyDrawContext) {
        with(drawScope) {
            // Base Body Blob Fill with 3D Radial Gradient
            drawPath(
                path = ctx.bodyPath,
                brush = ctx.bodyBrush,
                style = Fill
            )

            // Top-left glossy specular highlight rim
            val highlightPath = Path().apply {
                val rx = ctx.bodyRadius * 0.90f
                val ry = ctx.bodyRadius * 0.82f
                val cx = ctx.centerX - ctx.bodyRadius * 0.08f
                val cy = ctx.centerY - ctx.bodyRadius * 0.12f
                addOval(Rect(cx - rx, cy - ry, cx + rx, cy + ry))
            }
            drawPath(
                path = highlightPath,
                color = Color.White.copy(alpha = 0.18f),
                style = Stroke(width = ctx.outlineWidth * 1.5f)
            )

            // Darker outline for high contrast readability on any world board
            drawPath(
                path = ctx.bodyPath,
                color = ctx.outlineColor,
                style = Stroke(width = ctx.outlineWidth)
            )
        }
    }

    private fun drawBelly(drawScope: DrawScope, ctx: BuddyDrawContext) {
        with(drawScope) {
            if (ctx.species != BuddySpecies.TURTLE) {
                drawPath(
                    path = ctx.bellyPath,
                    brush = ctx.bellyBrush,
                    style = Fill
                )
            }
        }
    }

    private fun drawSpecialFeatures(drawScope: DrawScope, ctx: BuddyDrawContext) {
        with(drawScope) {
            when (ctx.species.specialFeature) {
                SpecialFeature.SNOUT_PIGLET, SpecialFeature.SNOUT_CALF -> {
                    // Piglet / Calf snout
                    val snoutColor = if (ctx.species == BuddySpecies.PIGLET) ctx.species.accentColor else ctx.species.bellyColor
                    drawPath(ctx.specialFeaturePath, color = snoutColor, style = Fill)
                    drawPath(ctx.specialFeaturePath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))

                    // Nostrils
                    val nostrilR = ctx.bodyRadius * 0.045f
                    val nostrilSpacing = ctx.bodyRadius * 0.12f
                    val sy = ctx.centerY + ctx.bodyRadius * 0.18f
                    drawCircle(color = ctx.outlineColor, radius = nostrilR, center = Offset(ctx.centerX - nostrilSpacing, sy))
                    drawCircle(color = ctx.outlineColor, radius = nostrilR, center = Offset(ctx.centerX + nostrilSpacing, sy))

                    if (ctx.species == BuddySpecies.CALF) {
                        // Eye spot patch
                        drawCircle(
                            color = ctx.species.accentColor.copy(alpha = 0.8f),
                            radius = ctx.bodyRadius * 0.22f,
                            center = Offset(ctx.centerX - ctx.bodyRadius * 0.38f, ctx.centerY - ctx.bodyRadius * 0.12f)
                        )
                    }
                }

                SpecialFeature.BEAK_CHICK -> {
                    // Chick top crest & orange beak
                    drawPath(ctx.specialFeaturePath, color = ctx.species.accentColor, style = Fill)
                    drawPath(ctx.specialFeaturePath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))

                    drawPath(ctx.secondaryFeaturePath, color = ctx.species.accentColor, style = Fill)
                    drawPath(ctx.secondaryFeaturePath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))
                }

                SpecialFeature.BEAK_PARROT -> {
                    // Parrot hook beak
                    drawPath(ctx.specialFeaturePath, color = ctx.species.accentColor, style = Fill)
                    drawPath(ctx.specialFeaturePath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))
                }

                SpecialFeature.BEAK_PENGUIN -> {
                    // Penguin beak
                    drawPath(ctx.specialFeaturePath, color = ctx.species.accentColor, style = Fill)
                    drawPath(ctx.specialFeaturePath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))
                }

                SpecialFeature.BEAK_OWL -> {
                    // Owl spectacle rings & beak
                    drawPath(ctx.specialFeaturePath, color = ctx.species.bellyColor, style = Fill)
                    drawPath(ctx.specialFeaturePath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))

                    drawPath(ctx.secondaryFeaturePath, color = ctx.species.accentColor, style = Fill)
                    drawPath(ctx.secondaryFeaturePath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))
                }

                SpecialFeature.CRAB_CLAWS -> {
                    // Claws and eyestalks
                    drawPath(ctx.specialFeaturePath, color = ctx.species.accentColor, style = Fill)
                    drawPath(ctx.specialFeaturePath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))

                    drawPath(ctx.secondaryFeaturePath, color = ctx.species.accentColor, style = Fill)
                    drawPath(ctx.secondaryFeaturePath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))
                }

                SpecialFeature.RACCOON_MASK -> {
                    // Bandit mask
                    drawPath(ctx.specialFeaturePath, color = ctx.species.accentColor, style = Fill)
                }

                SpecialFeature.MEERKAT_EYE_RINGS -> {
                    // Meerkat sunglasses eye rings
                    drawPath(ctx.specialFeaturePath, color = ctx.species.accentColor, style = Fill)
                }

                SpecialFeature.RED_PANDA_MASK -> {
                    // White cheek badges
                    drawPath(ctx.specialFeaturePath, color = Color.White.copy(alpha = 0.90f), style = Fill)
                }

                SpecialFeature.ELEPHANT_TRUNK -> {
                    // Trunk
                    drawPath(ctx.specialFeaturePath, color = ctx.species.bodyColor, style = Fill)
                    drawPath(ctx.specialFeaturePath, color = ctx.outlineColor, style = Stroke(width = ctx.outlineWidth))
                }

                SpecialFeature.TIGER_STRIPES -> {
                    // 3 curved dark stripes on each flank
                    val stripeColor = ctx.species.accentColor
                    val sy = ctx.centerY - ctx.bodyRadius * 0.15f
                    for (i in -1..1) {
                        val dy = i * ctx.bodyRadius * 0.20f
                        // Left cheek stripe
                        drawLine(
                            color = stripeColor,
                            start = Offset(ctx.centerX - ctx.bodyRadius * 0.95f, sy + dy),
                            end = Offset(ctx.centerX - ctx.bodyRadius * 0.65f, sy + dy),
                            strokeWidth = ctx.outlineWidth * 1.5f,
                            cap = StrokeCap.Round
                        )
                        // Right cheek stripe
                        drawLine(
                            color = stripeColor,
                            start = Offset(ctx.centerX + ctx.bodyRadius * 0.95f, sy + dy),
                            end = Offset(ctx.centerX + ctx.bodyRadius * 0.65f, sy + dy),
                            strokeWidth = ctx.outlineWidth * 1.5f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                SpecialFeature.TURTLE_SHELL,
                SpecialFeature.HEDGEHOG_QUILLS,
                SpecialFeature.CAMEL_HUMP,
                SpecialFeature.PEACOCK_CREST,
                SpecialFeature.NONE -> {
                    // Drawn in back anatomy or not applicable
                }
            }
        }
    }
}

