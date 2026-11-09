package com.kasif.arrowbuddies.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalArrowBuddiesColors = staticCompositionLocalOf { ArrowBuddiesColors() }
val LocalArrowBuddiesTypography = staticCompositionLocalOf { ArrowBuddiesTypography() }
val LocalArrowBuddiesSpacing = staticCompositionLocalOf { ArrowBuddiesSpacing() }
val LocalArrowBuddiesRadius = staticCompositionLocalOf { ArrowBuddiesCornerRadius() }
val LocalArrowBuddiesWorldTheme = staticCompositionLocalOf { World1SunnyMeadow }

private val MaterialLightColors = lightColorScheme(
    primary = Mango,
    onPrimary = Ink,
    primaryContainer = Color(0xFFF7EEDF),
    onPrimaryContainer = Ink,
    secondary = Sky,
    onSecondary = Ink,
    secondaryContainer = Color(0xFFD6F3FC),
    onSecondaryContainer = Ink,
    tertiary = Mint,
    onTertiary = Ink,
    background = Cream,
    onBackground = Ink,
    surface = WhitePure,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF7EEDF),
    onSurfaceVariant = Color(0xFF5D5470),
    error = Coral,
    onError = WhitePure
)

@Composable
fun ArrowBuddiesTheme(
    worldTheme: WorldTheme = World1SunnyMeadow,
    content: @Composable () -> Unit
) {
    // Fixed bright theme: Never follow dark mode
    val colors = ArrowBuddiesColors()
    val typography = ArrowBuddiesTypography()
    val spacing = ArrowBuddiesSpacing()
    val radius = ArrowBuddiesCornerRadius()

    CompositionLocalProvider(
        LocalArrowBuddiesColors provides colors,
        LocalArrowBuddiesTypography provides typography,
        LocalArrowBuddiesSpacing provides spacing,
        LocalArrowBuddiesRadius provides radius,
        LocalArrowBuddiesWorldTheme provides worldTheme
    ) {
        MaterialTheme(
            colorScheme = MaterialLightColors,
            typography = MaterialTypography,
            content = content
        )
    }
}

object ArrowBuddiesThemeTokens {
    val colors: ArrowBuddiesColors
        @Composable
        @ReadOnlyComposable
        get() = LocalArrowBuddiesColors.current

    val typography: ArrowBuddiesTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalArrowBuddiesTypography.current

    val spacing: ArrowBuddiesSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalArrowBuddiesSpacing.current

    val radius: ArrowBuddiesCornerRadius
        @Composable
        @ReadOnlyComposable
        get() = LocalArrowBuddiesRadius.current

    val worldTheme: WorldTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalArrowBuddiesWorldTheme.current

    val motion: ArrowBuddiesMotion
        get() = ArrowBuddiesMotion
}
