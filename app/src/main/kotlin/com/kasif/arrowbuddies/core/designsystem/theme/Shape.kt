package com.kasif.arrowbuddies.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ArrowBuddiesCornerRadius(
    val small: Dp = 12.dp,
    val medium: Dp = 20.dp,
    val large: Dp = 28.dp,
    val full: Dp = 999.dp
)

val ArrowBuddiesShapes = ArrowBuddiesCornerRadius()

val SmallRoundedShape = RoundedCornerShape(ArrowBuddiesShapes.small)
val MediumRoundedShape = RoundedCornerShape(ArrowBuddiesShapes.medium)
val LargeRoundedShape = RoundedCornerShape(ArrowBuddiesShapes.large)
val FullRoundedShape = RoundedCornerShape(ArrowBuddiesShapes.full)
