package com.kasif.arrowbuddies.core.designsystem.component

import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kasif.arrowbuddies.core.designsystem.modifier.LocalHaptics
import com.kasif.arrowbuddies.core.designsystem.modifier.LocalSound
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesMotion
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.designsystem.theme.FullRoundedShape
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure

/**
 * Chunky 3D toggle switch with sound and haptic feedback.
 */
@Composable
fun ToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    width: Dp = 60.dp,
    height: Dp = 34.dp
) {
    val colors = ArrowBuddiesThemeTokens.colors
    val soundManager = LocalSound.current
    val hapticsManager = LocalHaptics.current

    val thumbSize = height - 6.dp
    val maxOffset = width - thumbSize - 6.dp

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) maxOffset else 0.dp,
        animationSpec = ArrowBuddiesMotion.BouncyDp,
        label = "toggleThumbOffset"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) colors.mint.main else Color(0xFFE2D4BE),
        label = "toggleTrackColor"
    )

    val trackLipColor by animateColorAsState(
        targetValue = if (checked) colors.mint.lip else Color(0xFFC7B69C),
        label = "toggleTrackLipColor"
    )

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .shadow(elevation = 3.dp, shape = FullRoundedShape)
            .background(trackColor, shape = FullRoundedShape)
            .border(2.dp, trackLipColor, shape = FullRoundedShape)
            .clip(FullRoundedShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = {
                    hapticsManager?.tick()
                    soundManager.playPop()
                    onCheckedChange(!checked)
                }
            )
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Chunky 3D Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.roundToPx(), 0) }
                .size(thumbSize)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            WhitePure,
                            Color(0xFFF3ECE0)
                        )
                    ),
                    shape = CircleShape
                )
                .border(1.5.dp, Color(0xFFD6C8B5), shape = CircleShape)
        )
    }
}

@Preview(name = "Toggle Switch", showBackground = true, backgroundColor = 0xFFFFF7EC)
@Composable
private fun ToggleSwitchPreview() {
    ArrowBuddiesTheme {
        Box(modifier = Modifier.padding(20.dp)) {
            ToggleSwitch(checked = true, onCheckedChange = {})
        }
    }
}
