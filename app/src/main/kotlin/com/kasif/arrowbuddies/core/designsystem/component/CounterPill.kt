package com.kasif.arrowbuddies.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesMotion
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.designsystem.theme.FullRoundedShape
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure

/**
 * A tactile pill displaying an icon and counter (hearts, berries, rescue count).
 * - Number rolls with vertical odometer animation when changed
 * - Icon pops/bounces with a spring when the value updates
 */
@Composable
fun CounterPill(
    count: Int,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    backgroundColor: Color = WhitePure,
    borderColor: Color = Color(0xFFE4D5BC)
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val colors = ArrowBuddiesThemeTokens.colors

    // Icon Pop scale animation
    val iconScale = remember { Animatable(1f) }

    LaunchedEffect(count) {
        iconScale.animateTo(1.35f, animationSpec = ArrowBuddiesMotion.Snappy)
        iconScale.animateTo(1f, animationSpec = ArrowBuddiesMotion.Bouncy)
    }

    Box(
        modifier = modifier
            .shadow(elevation = 3.dp, shape = FullRoundedShape, spotColor = colors.textPrimary.copy(alpha = 0.15f))
            .background(backgroundColor, shape = FullRoundedShape)
            .border(1.5.dp, borderColor, shape = FullRoundedShape)
            .clip(FullRoundedShape)
            .height(40.dp)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Popping Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer {
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                    }
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Odometer Rolling Number
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically { height -> -height } togetherWith slideOutVertically { height -> height }
                    } else {
                        slideInVertically { height -> height } togetherWith slideOutVertically { height -> -height }
                    }
                },
                label = "counterOdometer"
            ) { targetCount ->
                Text(
                    text = targetCount.toString(),
                    style = typography.counter.copy(color = colors.textPrimary)
                )
            }
        }
    }
}

@Preview(name = "Counter Pills", showBackground = true, backgroundColor = 0xFFFFF7EC)
@Composable
private fun CounterPillPreview() {
    ArrowBuddiesTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CounterPill(
                count = 3,
                icon = Icons.Default.Favorite,
                iconTint = ArrowBuddiesThemeTokens.colors.coral.main
            )
            CounterPill(
                count = 150,
                icon = Icons.Default.Favorite,
                iconTint = ArrowBuddiesThemeTokens.colors.mango.main
            )
        }
    }
}
