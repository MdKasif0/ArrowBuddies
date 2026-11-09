package com.kasif.arrowbuddies.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ArrowBuddiesSpacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val huge: Dp = 48.dp,
    val minTouchTarget: Dp = 48.dp,
    val boardCellMin: Dp = 44.dp
)

@Immutable
data class ArrowBuddiesCornerRadius(
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val round: Dp = 999.dp
)

