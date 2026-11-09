package com.kasif.arrowbuddies.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.buddies.BuddyAvatar
import com.kasif.arrowbuddies.core.buddies.model.BuddyExpression
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.designsystem.component.AnimatedSkyBackground
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.designsystem.theme.Coral
import com.kasif.arrowbuddies.core.designsystem.theme.Cream
import com.kasif.arrowbuddies.core.designsystem.theme.Grape
import com.kasif.arrowbuddies.core.designsystem.theme.Ink
import com.kasif.arrowbuddies.core.designsystem.theme.Mango
import com.kasif.arrowbuddies.core.designsystem.theme.Mint
import com.kasif.arrowbuddies.core.designsystem.theme.Sky
import com.kasif.arrowbuddies.engine.model.Direction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    uiState: SplashUiState,
    onNavigateToGame: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = ArrowBuddiesThemeTokens.typography
    val letters = listOf("A", "r", "r", "o", "w", "B", "u", "d", "d", "i", "e", "s")
    val letterColors = listOf(Mango, Sky, Coral, Mint, Grape, Mango, Sky, Coral, Mint, Grape, Mango, Coral)

    val letterAnimatables = remember { letters.map { Animatable(-80f) } }
    val buddiesPeekAnim = remember { Animatable(160f) }

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveRotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waving"
    )

    fun navigateNext() {
        if (uiState.isFirstLaunch) {
            onNavigateToGame(uiState.initialLevelId)
        } else {
            onNavigateToHome()
        }
    }

    LaunchedEffect(Unit) {
        // Animate letters bouncing in one by one
        letters.indices.forEach { index ->
            launch {
                delay(index * 45L)
                letterAnimatables[index].animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }

        // Animate buddies peeking up
        launch {
            delay(250L)
            buddiesPeekAnim.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // Auto-advance after 1.2s branded intro
        delay(1200L)
        navigateNext()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { navigateNext() }
            )
    ) {
        AnimatedSkyBackground(
            modifier = Modifier.fillMaxSize(),
            skyTop = Cream,
            skyBottom = Cream
        )

        // Center: Bouncing "ArrowBuddies" Title
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                letters.forEachIndexed { index, letter ->
                    Text(
                        text = letter,
                        style = typography.display.copy(
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = letterColors[index],
                        modifier = Modifier
                            .offset { IntOffset(x = 0, y = letterAnimatables[index].value.toInt()) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.splash_tagline),
                style = typography.subtitle,
                color = Ink.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.action_tap_to_skip),
                style = typography.caption,
                color = Ink.copy(alpha = 0.40f)
            )
        }

        // Bottom: 3 Peeking Buddies (Bunny, Chick, Piglet) waving
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset { IntOffset(x = 0, y = buddiesPeekAnim.value.toInt()) }
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            BuddyAvatar(
                species = BuddySpecies.BUNNY,
                direction = Direction.UP,
                expression = BuddyExpression.HAPPY,
                size = 72.dp,
                modifier = Modifier.graphicsLayer { rotationZ = waveRotation }
            )
            BuddyAvatar(
                species = BuddySpecies.CHICK,
                direction = Direction.UP,
                expression = BuddyExpression.WINK,
                size = 76.dp,
                modifier = Modifier.graphicsLayer { rotationZ = -waveRotation }
            )
            BuddyAvatar(
                species = BuddySpecies.PIGLET,
                direction = Direction.UP,
                expression = BuddyExpression.HAPPY,
                size = 72.dp,
                modifier = Modifier.graphicsLayer { rotationZ = waveRotation }
            )
        }
    }
}
