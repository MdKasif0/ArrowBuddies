package com.kasif.arrowbuddies.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure

/**
 * A tactile rounded "toy / clay" card panel with:
 * - Soft layered shadow
 * - Subtle bevel border
 * - 2dp inner top highlight (light coming from the top)
 */
@Composable
fun ToyPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = ArrowBuddiesThemeTokens.radius.large,
    backgroundColor: Color = WhitePure,
    borderColor: Color = Color(0xFFEEDFC7),
    content: @Composable BoxScope.() -> Unit
) {
    val colors = ArrowBuddiesThemeTokens.colors
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = shape,
                spotColor = colors.textPrimary.copy(alpha = 0.12f)
            )
            .background(backgroundColor, shape = shape)
            .border(2.dp, borderColor, shape = shape)
            .clip(shape)
    ) {
        // Inner 2dp top highlight strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color.White.copy(alpha = 0.2f)
                        )
                    )
                )
        )

        content()
    }
}

@Preview(name = "Toy Panel", showBackground = true, backgroundColor = 0xFFFFF7EC)
@Composable
private fun ToyPanelPreview() {
    ArrowBuddiesTheme {
        ToyPanel(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Toy Panel Content",
                style = ArrowBuddiesThemeTokens.typography.title,
                color = ArrowBuddiesThemeTokens.colors.textPrimary,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

