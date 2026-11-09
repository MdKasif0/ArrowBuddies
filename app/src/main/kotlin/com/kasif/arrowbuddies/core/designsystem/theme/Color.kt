package com.kasif.arrowbuddies.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Palette from Game Spec
val Cream = Color(0xFFFFF7EC)
val Mango = Color(0xFFFFB627)
val Coral = Color(0xFFFF6B6B)
val Mint = Color(0xFF3DD6A3)
val Sky = Color(0xFF4CC9F0)
val Grape = Color(0xFF7B61FF)
val Ink = Color(0xFF2B2240)

// Complementary tints
val CreamDark = Color(0xFFF3E7D3)
val WhitePure = Color(0xFFFFFFFF)
val InkMuted = Color(0xFF5A526E)
val InkSubtle = Color(0xFF8E889E)

@Immutable
data class ArrowBuddiesColors(
    val background: Color = Cream,
    val surface: Color = WhitePure,
    val surfaceVariant: Color = CreamDark,
    val primary: Color = Mango,
    val secondary: Color = Mint,
    val tertiary: Color = Sky,
    val accent: Color = Coral,
    val highlight: Color = Grape,
    val textPrimary: Color = Ink,
    val textSecondary: Color = InkMuted,
    val textMuted: Color = InkSubtle,
    val onPrimary: Color = Ink,
    val onSurface: Color = Ink
)

