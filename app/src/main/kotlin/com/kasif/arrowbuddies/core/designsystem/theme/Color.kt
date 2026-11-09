package com.kasif.arrowbuddies.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// ==========================================
// ArrowBuddies Premium Game Palette & Shades
// ==========================================

// Core Brand Colors
val Cream = Color(0xFFFFF7EC)
val Mango = Color(0xFFFFB627)
val Coral = Color(0xFFFF6B6B)
val Mint = Color(0xFF3DD6A3)
val Sky = Color(0xFF4CC9F0)
val Grape = Color(0xFF7B61FF)
val Ink = Color(0xFF2B2240)

// Pure White & Translucent Overlays
val WhitePure = Color(0xFFFFFFFF)
val WhiteHighlight = Color(0x66FFFFFF) // 40% white for top glossy shine
val WhiteShine = Color(0x99FFFFFF)     // 60% white for intense highlights
val ShadowSoft = Color(0x262B2240)     // 15% Ink for soft shadows
val ShadowDeep = Color(0x4D2B2240)     // 30% Ink for 3D bottom drops
val ScrimDim = Color(0x80171224)       // 50% Ink for dialog scrim

/**
 * A tactile 3D color triplet with:
 * - [main]: The base vibrant color
 * - [lip]: Darker bottom bevel/lip (provides the 3D toy depth)
 * - [highlight]: Lighter top reflection/rim
 */
@Immutable
data class TactileColor(
    val main: Color,
    val lip: Color,
    val highlight: Color,
    val onColor: Color = Ink
)

// Tactile sets for all major colors
val TactileMango = TactileColor(
    main = Mango,
    lip = Color(0xFFD98200),
    highlight = Color(0xFFFFCE6B),
    onColor = Ink
)

val TactileCoral = TactileColor(
    main = Coral,
    lip = Color(0xFFD43838),
    highlight = Color(0xFFFFA6A6),
    onColor = WhitePure
)

val TactileMint = TactileColor(
    main = Mint,
    lip = Color(0xFF1EA87A),
    highlight = Color(0xFF75EDC6),
    onColor = Ink
)

val TactileSky = TactileColor(
    main = Sky,
    lip = Color(0xFF1E9FC6),
    highlight = Color(0xFF8CE0F7),
    onColor = Ink
)

val TactileGrape = TactileColor(
    main = Grape,
    lip = Color(0xFF5339D6),
    highlight = Color(0xFFAFA1FF),
    onColor = WhitePure
)

val TactileCream = TactileColor(
    main = Cream,
    lip = Color(0xFFE5D5BD),
    highlight = WhitePure,
    onColor = Ink
)

val TactileInk = TactileColor(
    main = Ink,
    lip = Color(0xFF171224),
    highlight = Color(0xFF4D3F6E),
    onColor = WhitePure
)

@Immutable
data class ArrowBuddiesColors(
    val background: Color = Cream,
    val surface: Color = WhitePure,
    val surfaceVariant: Color = Color(0xFFF7EEDF),
    val textPrimary: Color = Ink,
    val textSecondary: Color = Color(0xFF5D5470),
    val textMuted: Color = Color(0xFF8B839E),
    val textOnDark: Color = WhitePure,

    // Tactile color sets
    val mango: TactileColor = TactileMango,
    val coral: TactileColor = TactileCoral,
    val mint: TactileColor = TactileMint,
    val sky: TactileColor = TactileSky,
    val grape: TactileColor = TactileGrape,
    val cream: TactileColor = TactileCream,
    val ink: TactileColor = TactileInk,

    // Semantics
    val primary: TactileColor = TactileMango,
    val secondary: TactileColor = TactileSky,
    val danger: TactileColor = TactileCoral,
    val success: TactileColor = TactileMint,
    val special: TactileColor = TactileGrape
)
