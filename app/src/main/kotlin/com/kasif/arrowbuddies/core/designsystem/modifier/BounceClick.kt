package com.kasif.arrowbuddies.core.designsystem.modifier

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import com.kasif.arrowbuddies.core.audio.DefaultSoundManager
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesMotion
import com.kasif.arrowbuddies.core.haptics.HapticsManager

val LocalSound = compositionLocalOf<SoundManager> { DefaultSoundManager() }
val LocalHaptics = compositionLocalOf<HapticsManager?> { null }

/**
 * Applies a squishy, tactile spring press scale animation on touch.
 * Scales down to [pressedScale] on finger down, pops back with Bouncy spring on release,
 * and triggers haptic tick and pop sound hooks.
 */
fun Modifier.bounceClick(
    pressedScale: Float = 0.94f,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val soundManager = LocalSound.current
    val hapticsManager = LocalHaptics.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = if (isPressed) ArrowBuddiesMotion.Snappy else ArrowBuddiesMotion.Bouncy,
        label = "bounceClickScale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed && enabled) {
            hapticsManager?.tick()
            soundManager.playPop()
        }
    }

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Visual press scale modifier for components that manage their own gesture or tap logic.
 */
fun Modifier.pressScale(
    isPressed: Boolean,
    pressedScale: Float = 0.95f
): Modifier = composed {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = if (isPressed) ArrowBuddiesMotion.Snappy else ArrowBuddiesMotion.Bouncy,
        label = "pressScale"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

