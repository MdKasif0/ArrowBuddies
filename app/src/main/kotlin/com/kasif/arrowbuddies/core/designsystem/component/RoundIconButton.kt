package com.kasif.arrowbuddies.core.designsystem.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasif.arrowbuddies.core.designsystem.modifier.LocalHaptics
import com.kasif.arrowbuddies.core.designsystem.modifier.LocalSound
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesMotion
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.designsystem.theme.TactileColor
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure

/**
 * 3D circular 52dp button for HUD actions (pause, settings, boosters).
 * Includes tactile 3D lip, top highlight, press depression, sound/haptic hook,
 * and optional count badge for boosters.
 */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    variant: CandyButtonVariant = CandyButtonVariant.Ghost,
    badgeCount: Int? = null,
    size: Dp = 52.dp,
    enabled: Boolean = true
) {
    val colors = ArrowBuddiesThemeTokens.colors
    val typography = ArrowBuddiesThemeTokens.typography
    val soundManager = LocalSound.current
    val hapticsManager = LocalHaptics.current

    val tactileColor: TactileColor = when (variant) {
        CandyButtonVariant.Primary -> colors.mango
        CandyButtonVariant.Secondary -> colors.sky
        CandyButtonVariant.Danger -> colors.coral
        CandyButtonVariant.Success -> colors.mint
        CandyButtonVariant.Special -> colors.grape
        CandyButtonVariant.Ghost -> colors.cream
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed && enabled) {
            hapticsManager?.tick()
            soundManager.playPop()
        }
    }

    val lipHeight = 4.dp
    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed && enabled) 3.5.dp else 0.dp,
        animationSpec = if (isPressed) ArrowBuddiesMotion.SnappyDp else ArrowBuddiesMotion.BouncyDp,
        label = "roundButtonOffsetY"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = if (isPressed) ArrowBuddiesMotion.Snappy else ArrowBuddiesMotion.Bouncy,
        label = "roundButtonScale"
    )

    Box(
        modifier = modifier
            .size(size + lipHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Bottom Lip Layer
        Box(
            modifier = Modifier
                .size(size)
                .offset(y = lipHeight)
                .shadow(elevation = 3.dp, shape = CircleShape, spotColor = colors.textPrimary.copy(alpha = 0.20f))
                .background(tactileColor.lip, shape = CircleShape)
        )

        // Top Interactive Face
        Box(
            modifier = Modifier
                .size(size)
                .offset(y = pressOffsetY)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            tactileColor.highlight.copy(alpha = 0.85f),
                            tactileColor.main
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 1.5.dp,
                    color = if (variant == CandyButtonVariant.Ghost) Color(0xFFE2D6C0) else tactileColor.lip.copy(alpha = 0.35f),
                    shape = CircleShape
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Top Glossy Shine (radial or top gradient)
            Box(
                modifier = Modifier
                    .size(size)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.45f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = 35f
                        )
                    )
            )

            // Icon
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (variant == CandyButtonVariant.Ghost) colors.textPrimary else tactileColor.onColor,
                modifier = Modifier.size(size * 0.48f)
            )
        }

        // Optional Count Badge (top-right corner)
        if (badgeCount != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(22.dp)
                    .shadow(elevation = 3.dp, shape = CircleShape)
                    .background(colors.coral.main, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeCount.toString(),
                    style = typography.caption.copy(
                        color = WhitePure,
                        fontSize = 11.sp,
                        lineHeight = 11.sp
                    )
                )
            }
        }
    }
}

@Preview(name = "Round Icon Button", showBackground = true, backgroundColor = 0xFFFFF7EC)
@Composable
private fun RoundIconButtonPreview() {
    ArrowBuddiesTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            RoundIconButton(
                icon = Icons.Default.Settings,
                variant = CandyButtonVariant.Primary,
                badgeCount = 3,
                onClick = {}
            )
        }
    }
}
