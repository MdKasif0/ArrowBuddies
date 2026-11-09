package com.kasif.arrowbuddies.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalArrowBuddiesColors = staticCompositionLocalOf { ArrowBuddiesColors() }
val LocalArrowBuddiesSpacing = staticCompositionLocalOf { ArrowBuddiesSpacing() }
val LocalArrowBuddiesRadius = staticCompositionLocalOf { ArrowBuddiesCornerRadius() }

private val MaterialLightColors = lightColorScheme(
    primary = Mango,
    onPrimary = Ink,
    primaryContainer = CreamDark,
    onPrimaryContainer = Ink,
    secondary = Mint,
    onSecondary = Ink,
    secondaryContainer = Mint.copy(alpha = 0.2f),
    onSecondaryContainer = Ink,
    tertiary = Sky,
    onTertiary = Ink,
    background = Cream,
    onBackground = Ink,
    surface = WhitePure,
    onSurface = Ink,
    surfaceVariant = CreamDark,
    onSurfaceVariant = InkMuted,
    error = Coral,
    onError = WhitePure
)

@Composable
fun ArrowBuddiesTheme(
    content: @Composable () -> Unit
) {
    // Fixed bright theme: Never follow dark mode
    val colors = ArrowBuddiesColors()
    val spacing = ArrowBuddiesSpacing()
    val radius = ArrowBuddiesCornerRadius()

    CompositionLocalProvider(
        LocalArrowBuddiesColors provides colors,
        LocalArrowBuddiesSpacing provides spacing,
        LocalArrowBuddiesRadius provides radius
    ) {
        MaterialTheme(
            colorScheme = MaterialLightColors,
            typography = Typography,
            content = content
        )
    }
}

object ArrowBuddiesThemeTokens {
    val colors: ArrowBuddiesColors
        @Composable
        @ReadOnlyComposable
        get() = LocalArrowBuddiesColors.current

    val spacing: ArrowBuddiesSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalArrowBuddiesSpacing.current

    val radius: ArrowBuddiesCornerRadius
        @Composable
        @ReadOnlyComposable
        get() = LocalArrowBuddiesRadius.current
}

