package com.kasif.arrowbuddies.core.designsystem.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable

@Immutable
object ArrowBuddiesMotion {
    // Named Spring Specs
    val Bouncy: SpringSpec<Float> = spring(
        dampingRatio = 0.5f,
        stiffness = Spring.StiffnessMedium
    )

    val Snappy: SpringSpec<Float> = spring(
        dampingRatio = 0.75f,
        stiffness = Spring.StiffnessHigh
    )

    val Gentle: SpringSpec<Float> = spring(
        dampingRatio = 0.9f,
        stiffness = Spring.StiffnessLow
    )

    fun <T> bouncy(): SpringSpec<T> = spring(
        dampingRatio = 0.5f,
        stiffness = Spring.StiffnessMedium
    )

    fun <T> snappy(): SpringSpec<T> = spring(
        dampingRatio = 0.75f,
        stiffness = Spring.StiffnessHigh
    )

    fun <T> gentle(): SpringSpec<T> = spring(
        dampingRatio = 0.9f,
        stiffness = Spring.StiffnessLow
    )

    // Standard Durations
    const val DurationQuickMs = 120
    const val DurationNormalMs = 220
    const val DurationRelaxedMs = 400

    val TweenQuick: AnimationSpec<Float> = tween(durationMillis = DurationQuickMs, easing = FastOutSlowInEasing)
    val TweenNormal: AnimationSpec<Float> = tween(durationMillis = DurationNormalMs, easing = FastOutSlowInEasing)
    val TweenRelaxed: AnimationSpec<Float> = tween(durationMillis = DurationRelaxedMs, easing = FastOutSlowInEasing)
}
