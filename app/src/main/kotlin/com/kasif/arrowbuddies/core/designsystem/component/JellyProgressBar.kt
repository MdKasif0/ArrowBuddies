package com.kasif.arrowbuddies.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesMotion
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.designsystem.theme.FullRoundedShape
import com.kasif.arrowbuddies.core.designsystem.theme.TactileColor

/**
 * A jelly progress bar with:
 * - 3D pill track
 * - Bouncy spring fill animation that squishes and wobbles on change
 * - Top glossy reflection strip
 */
@Composable
fun JellyProgressBar(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    height: Dp = 22.dp,
    tactileColor: TactileColor = ArrowBuddiesThemeTokens.colors.mint,
    trackColor: Color = Color(0xFFEADBCE)
) {
    val clampedProgress = progress.coerceIn(0f, 1f)

    // Bouncy spring animation for squishy jelly wobble
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = ArrowBuddiesMotion.Bouncy,
        label = "jellyProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(elevation = 3.dp, shape = FullRoundedShape)
            .background(trackColor, shape = FullRoundedShape)
            .border(2.dp, Color(0xFFD6C5B2), shape = FullRoundedShape)
            .clip(FullRoundedShape)
    ) {
        if (animatedProgress > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                tactileColor.highlight,
                                tactileColor.main
                            )
                        ),
                        shape = FullRoundedShape
                    )
                    .clip(FullRoundedShape)
            ) {
                // Top Glossy Shine
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height * 0.45f)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.55f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}

@Preview(name = "Jelly Progress Bar", showBackground = true, backgroundColor = 0xFFFFF7EC)
@Composable
private fun JellyProgressBarPreview() {
    ArrowBuddiesTheme {
        Box(modifier = Modifier.padding(20.dp)) {
            JellyProgressBar(progress = 0.65f)
        }
    }
}
