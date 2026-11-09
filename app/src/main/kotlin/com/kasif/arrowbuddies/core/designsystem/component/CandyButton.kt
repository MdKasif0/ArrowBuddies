package com.kasif.arrowbuddies.core.designsystem.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kasif.arrowbuddies.core.designsystem.modifier.LocalHaptics
import com.kasif.arrowbuddies.core.designsystem.modifier.LocalSound
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesMotion
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.designsystem.theme.Ink
import com.kasif.arrowbuddies.core.designsystem.theme.TactileColor
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure

enum class CandyButtonVariant {
    Primary,    // Mango
    Secondary,  // Sky
    Danger,     // Coral
    Success,    // Mint
    Special,    // Grape
    Ghost       // Cream
}

/**
 * A 3D "jelly" candy button with:
 * - A 6dp bottom lip that creates tactile depth
 * - Top face with vertical gradient and top glossy highlight strip
 * - Press down mechanic (face moves down onto the lip, scales to 0.95)
 * - Haptic tick and audio pop hooks
 * - Minimum height 56dp, min touch target 48dp
 */
@Composable
fun CandyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: CandyButtonVariant = CandyButtonVariant.Primary,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    minHeight: Dp = 56.dp,
    cornerRadius: Dp = ArrowBuddiesThemeTokens.radius.medium
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

    val lipHeight = 6.dp
    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed && enabled) 5.dp else 0.dp,
        animationSpec = if (isPressed) ArrowBuddiesMotion.SnappyDp else ArrowBuddiesMotion.BouncyDp,
        label = "candyButtonOffsetY"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = if (isPressed) ArrowBuddiesMotion.Snappy else ArrowBuddiesMotion.Bouncy,
        label = "candyButtonScale"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .defaultMinSize(minWidth = 120.dp, minHeight = minHeight + lipHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // 1. Bottom Lip Layer (3D base)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeight)
                .offset(y = lipHeight)
                .shadow(elevation = 6.dp, shape = shape, spotColor = colors.textPrimary.copy(alpha = 0.25f))
                .background(tactileColor.lip, shape = shape)
        )

        // 2. Top Interactive Face (translates down on press)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeight)
                .offset(y = pressOffsetY)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            tactileColor.highlight.copy(alpha = 0.85f),
                            tactileColor.main
                        )
                    ),
                    shape = shape
                )
                .clip(shape)
        ) {
            // 3. Top Glossy Highlight Strip (light from the top)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(minHeight * 0.45f)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
                    )
            )

            // 4. Content (Text + Optional Icon)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(minHeight)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = tactileColor.onColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                val textShadow = if (tactileColor.onColor == WhitePure) {
                    Shadow(
                        color = Ink.copy(alpha = 0.4f),
                        offset = Offset(0f, 2f),
                        blurRadius = 3f
                    )
                } else null

                Text(
                    text = text,
                    style = typography.button.copy(
                        color = tactileColor.onColor,
                        shadow = textShadow
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(name = "Candy Buttons", showBackground = true, backgroundColor = 0xFFFFF7EC)
@Composable
private fun CandyButtonPreview() {
    ArrowBuddiesTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CandyButton(
                text = "Play",
                variant = CandyButtonVariant.Primary,
                leadingIcon = Icons.Default.PlayArrow,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            CandyButton(
                text = "Danger",
                variant = CandyButtonVariant.Danger,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }
    }
}
