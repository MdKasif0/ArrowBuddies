package com.kasif.arrowbuddies.feature.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.buddies.BuddyAvatar
import com.kasif.arrowbuddies.core.buddies.model.BuddyExpression
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.designsystem.component.CandyButton
import com.kasif.arrowbuddies.core.designsystem.component.CandyButtonVariant
import com.kasif.arrowbuddies.core.designsystem.component.GameDialog
import com.kasif.arrowbuddies.core.designsystem.component.ToyPanel
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.designsystem.theme.Coral
import com.kasif.arrowbuddies.core.designsystem.theme.Mango
import com.kasif.arrowbuddies.core.designsystem.theme.Mint
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure
import com.kasif.arrowbuddies.engine.model.Direction

enum class MechanicType(val id: String) {
    MUD("mud"),
    TURNTABLE("turntable"),
    CAGE("cage"),
    PORTAL("portal"),
    SLEEPY("sleepy")
}

@Composable
fun MechanicIntroDialog(
    mechanic: MechanicType,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val colors = ArrowBuddiesThemeTokens.colors

    val titleRes = when (mechanic) {
        MechanicType.MUD -> R.string.mechanic_mud_title
        MechanicType.TURNTABLE -> R.string.mechanic_turntable_title
        MechanicType.CAGE -> R.string.mechanic_cage_title
        MechanicType.PORTAL -> R.string.mechanic_portal_title
        MechanicType.SLEEPY -> R.string.mechanic_sleepy_title
    }

    val descRes = when (mechanic) {
        MechanicType.MUD -> R.string.mechanic_mud_desc
        MechanicType.TURNTABLE -> R.string.mechanic_turntable_desc
        MechanicType.CAGE -> R.string.mechanic_cage_desc
        MechanicType.PORTAL -> R.string.mechanic_portal_desc
        MechanicType.SLEEPY -> R.string.mechanic_sleepy_desc
    }

    GameDialog(
        visible = true,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        ToyPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Badge
                Text(
                    text = stringResource(R.string.mechanic_intro_title),
                    style = typography.caption.copy(fontSize = 13.sp),
                    color = Mango
                )

                Text(
                    text = stringResource(titleRes),
                    style = typography.title,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                // Animated Mini-Demo on 3x3 board
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3E9D5), RoundedCornerShape(16.dp))
                        .border(2.dp, Color(0xFFDECDB4), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    MiniDemoCanvas(mechanic = mechanic)
                }

                // Description
                Text(
                    text = stringResource(descRes),
                    style = typography.body,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                CandyButton(
                    text = stringResource(R.string.mechanic_intro_got_it),
                    onClick = onDismiss,
                    variant = CandyButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Animated mini-demo illustrating the mechanic on a small 3x3 grid.
 */
@Composable
private fun MiniDemoCanvas(mechanic: MechanicType) {
    val infiniteTransition = rememberInfiniteTransition(label = "MiniDemoLoop")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Progress"
    )

    Canvas(modifier = Modifier.size(140.dp)) {
        val cellSize = size.width / 3f

        // Draw 3x3 checkered background
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                val color = if ((r + c) % 2 == 0) Color(0xFFA5E693) else Color(0xFF94DE81)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(c * cellSize, r * cellSize),
                    size = Size(cellSize - 1f, cellSize - 1f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
        }

        // Draw mechanic specific element at center (1, 1)
        val center = Offset(cellSize * 1.5f, cellSize * 1.5f)
        val radius = cellSize * 0.42f

        when (mechanic) {
            MechanicType.MUD -> {
                // Brown mud puddle
                drawOval(
                    color = Color(0xFF6B4226),
                    topLeft = Offset(center.x - radius, center.y - radius * 0.75f),
                    size = Size(radius * 2f, radius * 1.5f)
                )
                drawOval(
                    color = Color(0xFF8B5A2B),
                    topLeft = Offset(center.x - radius * 0.85f, center.y - radius * 0.65f),
                    size = Size(radius * 1.7f, radius * 1.3f)
                )
                // Buddy moves from left (0,1) and stops on mud (1,1)
                val buddyX = if (progress < 0.5f) {
                    cellSize * 0.5f + (cellSize * (progress / 0.5f))
                } else {
                    cellSize * 1.5f
                }
                drawCircle(color = WhitePure, radius = 10f, center = Offset(buddyX, center.y))
                drawCircle(color = Coral, radius = 4f, center = Offset(buddyX + 4f, center.y))
            }

            MechanicType.TURNTABLE -> {
                // Turntable disc
                drawCircle(color = Color(0xFF8B5A2B), radius = radius + 2f, center = center)
                drawCircle(color = Color(0xFFC48B57), radius = radius, center = center)
                // Clockwise carved arrow
                drawArc(
                    color = Color(0xFF5A3414),
                    startAngle = -90f + (progress * 360f),
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 0.6f, center.y - radius * 0.6f),
                    size = Size(radius * 1.2f, radius * 1.2f),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }

            MechanicType.CAGE -> {
                // Padlock & Bars
                val cageRect = androidx.compose.ui.geometry.Rect(
                    center.x - radius, center.y - radius,
                    center.x + radius, center.y + radius
                )
                drawRoundRect(
                    color = Color(0xFF8E9AAF),
                    topLeft = Offset(cageRect.left, cageRect.top),
                    size = Size(cageRect.width, cageRect.height),
                    style = Stroke(width = 3f)
                )
                for (i in 1..2) {
                    val barX = cageRect.left + (cageRect.width * (i / 3f))
                    drawLine(Color(0xFF8E9AAF), Offset(barX, cageRect.top), Offset(barX, cageRect.bottom), strokeWidth = 2.5f)
                }
                // Gold lock in center
                drawCircle(color = Color(0xFFFFD700), radius = 10f, center = center)
                drawCircle(color = Color(0xFF2B2240), radius = 3f, center = center)
            }

            MechanicType.PORTAL -> {
                // Portal 1 at (0, 1) and Portal 2 at (2, 1)
                val p1 = Offset(cellSize * 0.5f, center.y)
                val p2 = Offset(cellSize * 2.5f, center.y)
                drawCircle(color = Color(0x6600E5FF), radius = 16f, center = p1)
                drawCircle(color = Color(0xFF00E5FF), radius = 10f, center = p1, style = Stroke(width = 3f))
                drawCircle(color = Color(0x6600E5FF), radius = 16f, center = p2)
                drawCircle(color = Color(0xFF00E5FF), radius = 10f, center = p2, style = Stroke(width = 3f))
            }

            MechanicType.SLEEPY -> {
                // Nightcap and Zzz
                drawCircle(color = Color(0xFFFFF7EC), radius = 16f, center = center)
                drawCircle(color = Color(0xFF7B61FF), radius = 8f, center = Offset(center.x - 6f, center.y - 12f))
                // Zzz dot
                drawCircle(color = Mint, radius = 3f, center = Offset(center.x + 10f, center.y - 10f))
                drawCircle(color = Mint, radius = 4f, center = Offset(center.x + 16f, center.y - 18f))
            }
        }
    }
}

