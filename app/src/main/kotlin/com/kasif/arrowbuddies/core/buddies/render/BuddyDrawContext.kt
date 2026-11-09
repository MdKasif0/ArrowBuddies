package com.kasif.arrowbuddies.core.buddies.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.buddies.model.EarType
import com.kasif.arrowbuddies.core.buddies.model.SpecialFeature
import com.kasif.arrowbuddies.core.buddies.model.TailType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Precomputed paths and brushes cached once per (Size, BuddySpecies) combination.
 * Built within `drawWithCache` to ensure 60fps performance with zero frame allocations.
 */
class BuddyDrawContext(
    val size: Size,
    val species: BuddySpecies
) {
    val width = size.width
    val height = size.height
    val minDim = minOf(width, height)

    // Geometry anchors
    val centerX = width * 0.5f
    val centerY = height * 0.52f
    val bodyRadius = minDim * 0.38f

    // Standard Outline & Highlight colors
    val outlineColor = Color(0xFF2B2240) // Ink dark outline
    val outlineWidth = maxOf(1.5f, minDim * 0.028f)

    // Body radial gradient: lighter highlight top-left, rich body mid, lip shade bottom-right
    val bodyBrush: Brush = Brush.radialGradient(
        colors = listOf(
            species.bodyColor.lighten(0.18f),
            species.bodyColor,
            species.bodyColor.darken(0.18f)
        ),
        center = Offset(centerX - bodyRadius * 0.35f, centerY - bodyRadius * 0.35f),
        radius = bodyRadius * 1.35f
    )

    val bellyBrush: Brush = Brush.radialGradient(
        colors = listOf(
            species.bellyColor.lighten(0.12f),
            species.bellyColor
        ),
        center = Offset(centerX, centerY + bodyRadius * 0.25f),
        radius = bodyRadius * 0.75f
    )

    // Main body path (slightly squashed lovable round blob)
    val bodyPath = Path().apply {
        val rx = bodyRadius * 1.02f
        val ry = bodyRadius * 0.94f
        addOval(Rect(centerX - rx, centerY - ry, centerX + rx, centerY + ry))
    }

    // Belly patch path
    val bellyPath = Path().apply {
        val brx = bodyRadius * 0.58f
        val bry = bodyRadius * 0.44f
        val bcy = centerY + bodyRadius * 0.32f
        addOval(Rect(centerX - brx, bcy - bry, centerX + brx, bcy + bry))
    }

    // Soft contact shadow path under body
    val shadowPath = Path().apply {
        val srx = bodyRadius * 0.85f
        val sry = bodyRadius * 0.22f
        val scy = centerY + bodyRadius * 0.88f
        addOval(Rect(centerX - srx, scy - sry, centerX + srx, scy + sry))
    }

    // Ear paths (left & right)
    val leftEarPath = Path()
    val rightEarPath = Path()
    val leftInnerEarPath = Path()
    val rightInnerEarPath = Path()

    // Tail path
    val tailPath = Path()

    // Special feature paths
    val specialFeaturePath = Path()
    val secondaryFeaturePath = Path()

    init {
        buildEars()
        buildTail()
        buildSpecialFeature()
    }

    private fun buildEars() {
        val er = bodyRadius * 0.45f
        when (species.earType) {
            EarType.LONG_RABBIT -> {
                // Tall rabbit ears
                val earW = bodyRadius * 0.32f
                val earH = bodyRadius * 0.95f
                val earTopY = centerY - bodyRadius * 0.75f - earH

                leftEarPath.apply {
                    moveTo(centerX - bodyRadius * 0.45f, centerY - bodyRadius * 0.5f)
                    cubicTo(
                        centerX - bodyRadius * 0.65f, earTopY + earH * 0.4f,
                        centerX - bodyRadius * 0.55f, earTopY,
                        centerX - bodyRadius * 0.30f, earTopY
                    )
                    cubicTo(
                        centerX - bodyRadius * 0.15f, earTopY,
                        centerX - bodyRadius * 0.20f, earTopY + earH * 0.5f,
                        centerX - bodyRadius * 0.25f, centerY - bodyRadius * 0.6f
                    )
                    close()
                }
                leftInnerEarPath.apply {
                    val scale = 0.65f
                    moveTo(centerX - bodyRadius * 0.40f, centerY - bodyRadius * 0.55f)
                    cubicTo(
                        centerX - bodyRadius * 0.52f, earTopY + earH * 0.4f,
                        centerX - bodyRadius * 0.46f, earTopY + earH * 0.15f,
                        centerX - bodyRadius * 0.30f, earTopY + earH * 0.15f
                    )
                    cubicTo(
                        centerX - bodyRadius * 0.20f, earTopY + earH * 0.15f,
                        centerX - bodyRadius * 0.24f, earTopY + earH * 0.45f,
                        centerX - bodyRadius * 0.28f, centerY - bodyRadius * 0.6f
                    )
                    close()
                }

                // Symmetrical Right ear
                rightEarPath.apply {
                    moveTo(centerX + bodyRadius * 0.45f, centerY - bodyRadius * 0.5f)
                    cubicTo(
                        centerX + bodyRadius * 0.65f, earTopY + earH * 0.4f,
                        centerX + bodyRadius * 0.55f, earTopY,
                        centerX + bodyRadius * 0.30f, earTopY
                    )
                    cubicTo(
                        centerX + bodyRadius * 0.15f, earTopY,
                        centerX + bodyRadius * 0.20f, earTopY + earH * 0.5f,
                        centerX + bodyRadius * 0.25f, centerY - bodyRadius * 0.6f
                    )
                    close()
                }
                rightInnerEarPath.apply {
                    moveTo(centerX + bodyRadius * 0.40f, centerY - bodyRadius * 0.55f)
                    cubicTo(
                        centerX + bodyRadius * 0.52f, earTopY + earH * 0.4f,
                        centerX + bodyRadius * 0.46f, earTopY + earH * 0.15f,
                        centerX + bodyRadius * 0.30f, earTopY + earH * 0.15f
                    )
                    cubicTo(
                        centerX + bodyRadius * 0.20f, earTopY + earH * 0.15f,
                        centerX + bodyRadius * 0.24f, earTopY + earH * 0.45f,
                        centerX + bodyRadius * 0.28f, centerY - bodyRadius * 0.6f
                    )
                    close()
                }
            }

            EarType.ROUND_BEAR -> {
                // Round cute cup ears (polar bear, panda, raccoon, hedgehog)
                val earRadius = bodyRadius * 0.32f
                val lx = centerX - bodyRadius * 0.68f
                val rx = centerX + bodyRadius * 0.68f
                val ey = centerY - bodyRadius * 0.68f

                leftEarPath.addOval(Rect(lx - earRadius, ey - earRadius, lx + earRadius, ey + earRadius))
                rightEarPath.addOval(Rect(rx - earRadius, ey - earRadius, rx + earRadius, ey + earRadius))

                val innerR = earRadius * 0.60f
                leftInnerEarPath.addOval(Rect(lx - innerR, ey - innerR, lx + innerR, ey + innerR))
                rightInnerEarPath.addOval(Rect(rx - innerR, ey - innerR, rx + innerR, ey + innerR))
            }

            EarType.POINTY_FOX -> {
                // Pointy triangle ears (arctic fox, tiger cub, piglet)
                val baseLeftX = centerX - bodyRadius * 0.85f
                val baseRightX = centerX - bodyRadius * 0.25f
                val baseY = centerY - bodyRadius * 0.55f
                val tipX = centerX - bodyRadius * 0.62f
                val tipY = centerY - bodyRadius * 1.15f

                leftEarPath.apply {
                    moveTo(baseLeftX, baseY)
                    lineTo(tipX, tipY)
                    lineTo(baseRightX, baseY)
                    close()
                }
                leftInnerEarPath.apply {
                    moveTo(baseLeftX + bodyRadius * 0.08f, baseY - bodyRadius * 0.05f)
                    lineTo(tipX, tipY + bodyRadius * 0.16f)
                    lineTo(baseRightX - bodyRadius * 0.08f, baseY - bodyRadius * 0.05f)
                    close()
                }

                // Symmetrical Right ear
                val rBaseLeftX = centerX + bodyRadius * 0.25f
                val rBaseRightX = centerX + bodyRadius * 0.85f
                val rTipX = centerX + bodyRadius * 0.62f
                rightEarPath.apply {
                    moveTo(rBaseLeftX, baseY)
                    lineTo(rTipX, tipY)
                    lineTo(rBaseRightX, baseY)
                    close()
                }
                rightInnerEarPath.apply {
                    moveTo(rBaseLeftX + bodyRadius * 0.08f, baseY - bodyRadius * 0.05f)
                    lineTo(rTipX, tipY + bodyRadius * 0.16f)
                    lineTo(rBaseRightX - bodyRadius * 0.08f, baseY - bodyRadius * 0.05f)
                    close()
                }
            }

            EarType.BIG_TRIANGLE -> {
                // Enormous Fennec Fox ears
                val baseLeftX = centerX - bodyRadius * 0.95f
                val baseRightX = centerX - bodyRadius * 0.20f
                val baseY = centerY - bodyRadius * 0.45f
                val tipX = centerX - bodyRadius * 0.80f
                val tipY = centerY - bodyRadius * 1.55f

                leftEarPath.apply {
                    moveTo(baseLeftX, baseY)
                    cubicTo(baseLeftX - bodyRadius * 0.15f, centerY - bodyRadius, tipX - bodyRadius * 0.1f, tipY + bodyRadius * 0.2f, tipX, tipY)
                    cubicTo(tipX + bodyRadius * 0.2f, tipY + bodyRadius * 0.3f, baseRightX, centerY - bodyRadius * 0.8f, baseRightX, baseY)
                    close()
                }
                leftInnerEarPath.apply {
                    val shrink = bodyRadius * 0.10f
                    moveTo(baseLeftX + shrink, baseY)
                    lineTo(tipX, tipY + shrink * 1.8f)
                    lineTo(baseRightX - shrink, baseY)
                    close()
                }

                val rBaseLeftX = centerX + bodyRadius * 0.20f
                val rBaseRightX = centerX + bodyRadius * 0.95f
                val rTipX = centerX + bodyRadius * 0.80f
                rightEarPath.apply {
                    moveTo(rBaseLeftX, baseY)
                    cubicTo(rBaseLeftX, centerY - bodyRadius * 0.8f, rTipX - bodyRadius * 0.2f, tipY + bodyRadius * 0.3f, rTipX, tipY)
                    cubicTo(rTipX + bodyRadius * 0.1f, tipY + bodyRadius * 0.2f, rBaseRightX + bodyRadius * 0.15f, centerY - bodyRadius, rBaseRightX, baseY)
                    close()
                }
                rightInnerEarPath.apply {
                    val shrink = bodyRadius * 0.10f
                    moveTo(rBaseLeftX + shrink, baseY)
                    lineTo(rTipX, tipY + shrink * 1.8f)
                    lineTo(rBaseRightX - shrink, baseY)
                    close()
                }
            }

            EarType.FLOPPY_DOG -> {
                // Puppy / Lamb / Camel floppy ears draping along flanks
                val earW = bodyRadius * 0.38f
                val earH = bodyRadius * 0.75f
                val ey = centerY - bodyRadius * 0.4f

                leftEarPath.apply {
                    val lx = centerX - bodyRadius * 0.72f
                    moveTo(lx, ey)
                    cubicTo(lx - earW, ey + earH * 0.3f, lx - earW * 1.1f, ey + earH, lx - earW * 0.4f, ey + earH)
                    cubicTo(lx + earW * 0.2f, ey + earH, lx + earW * 0.3f, ey + earH * 0.4f, lx, ey)
                    close()
                }
                rightEarPath.apply {
                    val rx = centerX + bodyRadius * 0.72f
                    moveTo(rx, ey)
                    cubicTo(rx + earW, ey + earH * 0.3f, rx + earW * 1.1f, ey + earH, rx + earW * 0.4f, ey + earH)
                    cubicTo(rx - earW * 0.2f, ey + earH, rx - earW * 0.3f, ey + earH * 0.4f, rx, ey)
                    close()
                }
            }

            EarType.CUP_MONKEY -> {
                // Lateral round cupped ears
                val earR = bodyRadius * 0.36f
                val lx = centerX - bodyRadius * 0.95f
                val rx = centerX + bodyRadius * 0.95f
                val ey = centerY - bodyRadius * 0.15f

                leftEarPath.addOval(Rect(lx - earR, ey - earR, lx + earR, ey + earR))
                rightEarPath.addOval(Rect(rx - earR, ey - earR, rx + earR, ey + earR))

                val innerR = earR * 0.65f
                leftInnerEarPath.addOval(Rect(lx - innerR, ey - innerR, lx + innerR, ey + innerR))
                rightInnerEarPath.addOval(Rect(rx - innerR, ey - innerR, rx + innerR, ey + innerR))
            }

            EarType.HORN_CALF -> {
                // Cute calf horns & lateral ears
                val hlx = centerX - bodyRadius * 0.55f
                val hrx = centerX + bodyRadius * 0.55f
                val hy = centerY - bodyRadius * 0.85f

                // Left horn
                leftEarPath.apply {
                    moveTo(hlx - bodyRadius * 0.12f, hy + bodyRadius * 0.15f)
                    lineTo(hlx - bodyRadius * 0.25f, hy - bodyRadius * 0.30f)
                    lineTo(hlx + bodyRadius * 0.12f, hy)
                    close()
                }
                // Right horn
                rightEarPath.apply {
                    moveTo(hrx + bodyRadius * 0.12f, hy + bodyRadius * 0.15f)
                    lineTo(hrx + bodyRadius * 0.25f, hy - bodyRadius * 0.30f)
                    lineTo(hrx - bodyRadius * 0.12f, hy)
                    close()
                }
                // Lateral ears
                val elx = centerX - bodyRadius * 0.88f
                val erx = centerX + bodyRadius * 0.88f
                val ey = centerY - bodyRadius * 0.40f
                leftInnerEarPath.addOval(Rect(elx - bodyRadius * 0.25f, ey - bodyRadius * 0.16f, elx + bodyRadius * 0.25f, ey + bodyRadius * 0.16f))
                rightInnerEarPath.addOval(Rect(erx - bodyRadius * 0.25f, ey - bodyRadius * 0.16f, erx + bodyRadius * 0.25f, ey + bodyRadius * 0.16f))
            }

            EarType.FEATHER_HORNS -> {
                // Owl feather crest horns
                val hlx = centerX - bodyRadius * 0.55f
                val hrx = centerX + bodyRadius * 0.55f
                val hy = centerY - bodyRadius * 0.75f

                leftEarPath.apply {
                    moveTo(hlx - bodyRadius * 0.25f, hy + bodyRadius * 0.25f)
                    lineTo(hlx - bodyRadius * 0.45f, hy - bodyRadius * 0.45f)
                    lineTo(hlx + bodyRadius * 0.15f, hy)
                    close()
                }
                rightEarPath.apply {
                    moveTo(hrx + bodyRadius * 0.25f, hy + bodyRadius * 0.25f)
                    lineTo(hrx + bodyRadius * 0.45f, hy - bodyRadius * 0.45f)
                    lineTo(hrx - bodyRadius * 0.15f, hy)
                    close()
                }
            }

            EarType.FAN_ELEPHANT -> {
                // Giant flapper elephant ears
                val earW = bodyRadius * 0.65f
                val earH = bodyRadius * 0.85f
                val lx = centerX - bodyRadius * 0.75f
                val rx = centerX + bodyRadius * 0.75f
                val ey = centerY - bodyRadius * 0.20f

                leftEarPath.addOval(Rect(lx - earW, ey - earH * 0.5f, lx + earW * 0.2f, ey + earH * 0.5f))
                rightEarPath.addOval(Rect(rx - earW * 0.2f, ey - earH * 0.5f, rx + earW, ey + earH * 0.5f))

                val inW = earW * 0.65f
                val inH = earH * 0.65f
                leftInnerEarPath.addOval(Rect(lx - inW * 0.9f, ey - inH * 0.5f, lx, ey + inH * 0.5f))
                rightInnerEarPath.addOval(Rect(rx, ey - inH * 0.5f, rx + inW * 0.9f, ey + inH * 0.5f))
            }

            EarType.NONE -> {
                // Chick, Penguin, Crab, Turtle, Peacock — handled in SpecialFeature
            }
        }
    }

    private fun buildTail() {
        val tx = centerX + bodyRadius * 0.78f
        val ty = centerY + bodyRadius * 0.55f

        when (species.tailType) {
            TailType.PUFF_BALL -> {
                val pr = bodyRadius * 0.26f
                tailPath.addOval(Rect(tx - pr, ty - pr, tx + pr, ty + pr))
            }
            TailType.WAG_DOG -> {
                tailPath.apply {
                    moveTo(tx - bodyRadius * 0.1f, ty)
                    cubicTo(tx + bodyRadius * 0.25f, ty - bodyRadius * 0.35f, tx + bodyRadius * 0.45f, ty - bodyRadius * 0.3f, tx + bodyRadius * 0.35f, ty)
                    close()
                }
            }
            TailType.CURLY_PIG -> {
                tailPath.apply {
                    val cr = bodyRadius * 0.20f
                    addOval(Rect(tx, ty - cr, tx + cr * 2, ty + cr))
                }
            }
            TailType.LONG_CURL -> {
                tailPath.apply {
                    moveTo(tx, ty)
                    cubicTo(tx + bodyRadius * 0.45f, ty - bodyRadius * 0.5f, tx + bodyRadius * 0.55f, ty - bodyRadius * 0.9f, tx + bodyRadius * 0.25f, ty - bodyRadius * 0.85f)
                    cubicTo(tx + bodyRadius * 0.15f, ty - bodyRadius * 0.80f, tx + bodyRadius * 0.35f, ty - bodyRadius * 0.55f, tx, ty + bodyRadius * 0.1f)
                    close()
                }
            }
            TailType.RINGED_BUSHY, TailType.BUSHY_BRUSH -> {
                tailPath.apply {
                    val brw = bodyRadius * 0.45f
                    val brh = bodyRadius * 0.85f
                    moveTo(tx - bodyRadius * 0.1f, ty)
                    cubicTo(tx + brw, ty - brh * 0.6f, tx + brw * 1.1f, ty - brh, tx + brw * 0.4f, ty - brh)
                    cubicTo(tx - brw * 0.1f, ty - brh * 0.8f, tx, ty - brh * 0.3f, tx - bodyRadius * 0.1f, ty)
                    close()
                }
            }
            TailType.TAPERED_OTTER -> {
                tailPath.apply {
                    moveTo(tx - bodyRadius * 0.1f, ty)
                    lineTo(tx + bodyRadius * 0.65f, ty + bodyRadius * 0.20f)
                    lineTo(tx, ty + bodyRadius * 0.25f)
                    close()
                }
            }
            TailType.FAN_PEACOCK -> {
                // Majestic peacock fan feathers behind body
                tailPath.apply {
                    for (i in -3..3) {
                        val angle = (i * 22f) * (PI / 180f)
                        val r = bodyRadius * 1.35f
                        val fx = centerX + (sin(angle) * r).toFloat()
                        val fy = centerY - (cos(angle) * r).toFloat()
                        addOval(Rect(fx - bodyRadius * 0.18f, fy - bodyRadius * 0.18f, fx + bodyRadius * 0.18f, fy + bodyRadius * 0.18f))
                    }
                }
            }
            TailType.TINY_TUFT -> {
                tailPath.apply {
                    moveTo(tx, ty)
                    lineTo(tx + bodyRadius * 0.35f, ty + bodyRadius * 0.25f)
                    addOval(Rect(tx + bodyRadius * 0.25f, ty + bodyRadius * 0.20f, tx + bodyRadius * 0.45f, ty + bodyRadius * 0.40f))
                }
            }
            TailType.NONE -> {
                // No tail rendered
            }
        }
    }

    private fun buildSpecialFeature() {
        when (species.specialFeature) {
            SpecialFeature.SNOUT_PIGLET, SpecialFeature.SNOUT_CALF -> {
                // Piglet / Calf snout
                val sw = bodyRadius * 0.48f
                val sh = bodyRadius * 0.32f
                val sy = centerY + bodyRadius * 0.18f
                specialFeaturePath.addOval(Rect(centerX - sw, sy - sh, centerX + sw, sy + sh))
            }
            SpecialFeature.BEAK_CHICK -> {
                // Chick 3-tuft crest & small triangular beak
                val crestY = centerY - bodyRadius * 0.90f
                specialFeaturePath.apply {
                    moveTo(centerX - bodyRadius * 0.15f, crestY)
                    lineTo(centerX, crestY - bodyRadius * 0.35f)
                    lineTo(centerX + bodyRadius * 0.15f, crestY)
                    close()
                }
                // Beak
                val by = centerY + bodyRadius * 0.10f
                secondaryFeaturePath.apply {
                    moveTo(centerX - bodyRadius * 0.16f, by)
                    lineTo(centerX, by + bodyRadius * 0.22f)
                    lineTo(centerX + bodyRadius * 0.16f, by)
                    close()
                }
            }
            SpecialFeature.BEAK_PARROT -> {
                // Downward curved parrot hook beak & crest
                val by = centerY + bodyRadius * 0.05f
                specialFeaturePath.apply {
                    moveTo(centerX - bodyRadius * 0.18f, by)
                    cubicTo(centerX - bodyRadius * 0.22f, by + bodyRadius * 0.42f, centerX, by + bodyRadius * 0.48f, centerX, by + bodyRadius * 0.45f)
                    cubicTo(centerX, by + bodyRadius * 0.35f, centerX + bodyRadius * 0.18f, by + bodyRadius * 0.25f, centerX + bodyRadius * 0.18f, by)
                    close()
                }
            }
            SpecialFeature.BEAK_PENGUIN -> {
                // Penguin bright triangular beak
                val by = centerY + bodyRadius * 0.12f
                specialFeaturePath.apply {
                    moveTo(centerX - bodyRadius * 0.18f, by)
                    lineTo(centerX, by + bodyRadius * 0.26f)
                    lineTo(centerX + bodyRadius * 0.18f, by)
                    close()
                }
            }
            SpecialFeature.BEAK_OWL -> {
                // Owl spectacle feather rings around eyes
                val ringR = bodyRadius * 0.38f
                val ly = centerY - bodyRadius * 0.05f
                specialFeaturePath.addOval(Rect(centerX - bodyRadius * 0.42f - ringR, ly - ringR, centerX - bodyRadius * 0.42f + ringR, ly + ringR))
                specialFeaturePath.addOval(Rect(centerX + bodyRadius * 0.42f - ringR, ly - ringR, centerX + bodyRadius * 0.42f + ringR, ly + ringR))

                // Yellow owl beak
                val by = centerY + bodyRadius * 0.12f
                secondaryFeaturePath.apply {
                    moveTo(centerX - bodyRadius * 0.14f, by)
                    lineTo(centerX, by + bodyRadius * 0.24f)
                    lineTo(centerX + bodyRadius * 0.14f, by)
                    close()
                }
            }
            SpecialFeature.TURTLE_SHELL -> {
                // Turtle shell rim & flippers
                val flipperW = bodyRadius * 0.35f
                val flipperH = bodyRadius * 0.20f
                val fy = centerY + bodyRadius * 0.40f
                specialFeaturePath.addOval(Rect(centerX - bodyRadius * 1.15f, fy - flipperH, centerX - bodyRadius * 0.70f, fy + flipperH))
                specialFeaturePath.addOval(Rect(centerX + bodyRadius * 0.70f, fy - flipperH, centerX + bodyRadius * 1.15f, fy + flipperH))
            }
            SpecialFeature.CRAB_CLAWS -> {
                // Crab dual pincers raised left and right
                val clawR = bodyRadius * 0.35f
                val cxL = centerX - bodyRadius * 1.05f
                val cxR = centerX + bodyRadius * 1.05f
                val cy = centerY - bodyRadius * 0.20f
                specialFeaturePath.addOval(Rect(cxL - clawR, cy - clawR, cxL + clawR, cy + clawR))
                specialFeaturePath.addOval(Rect(cxR - clawR, cy - clawR, cxR + clawR, cy + clawR))

                // Eyestalk bumps on top
                val eyestalkR = bodyRadius * 0.18f
                val eyY = centerY - bodyRadius * 0.85f
                secondaryFeaturePath.addOval(Rect(centerX - bodyRadius * 0.35f - eyestalkR, eyY - eyestalkR, centerX - bodyRadius * 0.35f + eyestalkR, eyY + eyestalkR))
                secondaryFeaturePath.addOval(Rect(centerX + bodyRadius * 0.35f - eyestalkR, eyY - eyestalkR, centerX + bodyRadius * 0.35f + eyestalkR, eyY + eyestalkR))
            }
            SpecialFeature.HEDGEHOG_QUILLS -> {
                // Crown of radiating quills behind head
                specialFeaturePath.apply {
                    for (i in -4..4) {
                        val angle = (i * 20f - 90f) * (PI / 180f)
                        val rBase = bodyRadius * 0.90f
                        val rTip = bodyRadius * 1.25f
                        val bx = centerX + (cos(angle) * rBase).toFloat()
                        val by = centerY + (sin(angle) * rBase).toFloat()
                        val tx = centerX + (cos(angle) * rTip).toFloat()
                        val ty = centerY + (sin(angle) * rTip).toFloat()
                        moveTo(bx - bodyRadius * 0.10f, by)
                        lineTo(tx, ty)
                        lineTo(bx + bodyRadius * 0.10f, by)
                        close()
                    }
                }
            }
            SpecialFeature.RACCOON_MASK -> {
                // Horizontal bandit eye mask across face
                val my = centerY - bodyRadius * 0.05f
                val mw = bodyRadius * 0.88f
                val mh = bodyRadius * 0.32f
                specialFeaturePath.addOval(Rect(centerX - mw, my - mh, centerX + mw, my + mh))
            }
            SpecialFeature.CAMEL_HUMP -> {
                // Dorsal curved camel hump
                val hx = centerX - bodyRadius * 0.25f
                val hy = centerY - bodyRadius * 0.85f
                val hr = bodyRadius * 0.40f
                specialFeaturePath.addOval(Rect(hx - hr, hy - hr, hx + hr, hy + hr))
            }
            SpecialFeature.MEERKAT_EYE_RINGS -> {
                // Dark rings around eyes
                val ringW = bodyRadius * 0.26f
                val ringH = bodyRadius * 0.32f
                val ey = centerY - bodyRadius * 0.05f
                specialFeaturePath.addOval(Rect(centerX - bodyRadius * 0.40f - ringW, ey - ringH, centerX - bodyRadius * 0.40f + ringW, ey + ringH))
                specialFeaturePath.addOval(Rect(centerX + bodyRadius * 0.40f - ringW, ey - ringH, centerX + bodyRadius * 0.40f + ringW, ey + ringH))
            }
            SpecialFeature.RED_PANDA_MASK -> {
                // White cheek crescents & brow dots
                val cy = centerY + bodyRadius * 0.10f
                val cw = bodyRadius * 0.30f
                val ch = bodyRadius * 0.20f
                specialFeaturePath.addOval(Rect(centerX - bodyRadius * 0.70f - cw, cy - ch, centerX - bodyRadius * 0.70f + cw, cy + ch))
                specialFeaturePath.addOval(Rect(centerX + bodyRadius * 0.70f - cw, cy - ch, centerX + bodyRadius * 0.70f + cw, cy + ch))
            }
            SpecialFeature.ELEPHANT_TRUNK -> {
                // Curled upward elephant trunk
                val ty = centerY + bodyRadius * 0.08f
                specialFeaturePath.apply {
                    moveTo(centerX - bodyRadius * 0.18f, ty)
                    cubicTo(
                        centerX - bodyRadius * 0.20f, ty + bodyRadius * 0.65f,
                        centerX + bodyRadius * 0.35f, ty + bodyRadius * 0.65f,
                        centerX + bodyRadius * 0.35f, ty + bodyRadius * 0.30f
                    )
                    cubicTo(
                        centerX + bodyRadius * 0.35f, ty + bodyRadius * 0.18f,
                        centerX + bodyRadius * 0.18f, ty + bodyRadius * 0.18f,
                        centerX + bodyRadius * 0.18f, ty + bodyRadius * 0.30f
                    )
                    cubicTo(
                        centerX + bodyRadius * 0.18f, ty + bodyRadius * 0.50f,
                        centerX, ty + bodyRadius * 0.50f,
                        centerX + bodyRadius * 0.05f, ty
                    )
                    close()
                }
            }
            SpecialFeature.PEACOCK_CREST -> {
                // 3 golden bobble stems on crown
                val cy = centerY - bodyRadius * 0.85f
                specialFeaturePath.apply {
                    for (i in -1..1) {
                        val sx = centerX + i * (bodyRadius * 0.20f)
                        val sy = cy - bodyRadius * 0.25f
                        moveTo(centerX, cy)
                        lineTo(sx, sy)
                    }
                }
            }
            SpecialFeature.TIGER_STRIPES, SpecialFeature.NONE -> {
                // Rendered as procedural stripes in Face/Body renderer
            }
        }
    }
}

// Color utility extensions
private fun Color.lighten(factor: Float): Color = Color(
    red = (red + (1f - red) * factor).coerceIn(0f, 1f),
    green = (green + (1f - green) * factor).coerceIn(0f, 1f),
    blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
    alpha = alpha
)

private fun Color.darken(factor: Float): Color = Color(
    red = (red * (1f - factor)).coerceIn(0f, 1f),
    green = (green * (1f - factor)).coerceIn(0f, 1f),
    blue = (blue * (1f - factor)).coerceIn(0f, 1f),
    alpha = alpha
)

