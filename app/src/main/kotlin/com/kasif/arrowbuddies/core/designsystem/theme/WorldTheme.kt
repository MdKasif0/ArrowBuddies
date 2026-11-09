package com.kasif.arrowbuddies.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class WorldTheme(
    val id: Int,
    val name: String,
    val mechanic: String,
    val species: List<String>,
    val skyTop: Color,
    val skyBottom: Color,
    val boardBase: Color,
    val tileCheckerA: Color,
    val tileCheckerB: Color,
    val accent: TactileColor
)

val World1SunnyMeadow = WorldTheme(
    id = 1,
    name = "Sunny Meadow",
    mechanic = "Basics & Rocks",
    species = listOf("Bunny", "Chick", "Piglet"),
    skyTop = Color(0xFF6BD7F7),
    skyBottom = Color(0xFFE2F8FE),
    boardBase = Color(0xFF86D672),
    tileCheckerA = Color(0xFFA5E693),
    tileCheckerB = Color(0xFF94DE81),
    accent = TactileMango
)

val World2HappyFarm = WorldTheme(
    id = 2,
    name = "Happy Farm",
    mechanic = "Mud",
    species = listOf("Puppy", "Lamb", "Calf"),
    skyTop = Color(0xFFFFC078),
    skyBottom = Color(0xFFFFF1DC),
    boardBase = Color(0xFFD49C65),
    tileCheckerA = Color(0xFFE8BA85),
    tileCheckerB = Color(0xFFDCA972),
    accent = TactileMint
)

val World3JungleRuins = WorldTheme(
    id = 3,
    name = "Jungle Ruins",
    mechanic = "Turntables",
    species = listOf("Monkey", "Tiger Cub", "Parrot"),
    skyTop = Color(0xFF38B89E),
    skyBottom = Color(0xFFD6F6EE),
    boardBase = Color(0xFF5A8E72),
    tileCheckerA = Color(0xFF78AC91),
    tileCheckerB = Color(0xFF689E81),
    accent = TactileMango
)

val World4SnowyPeaks = WorldTheme(
    id = 4,
    name = "Snowy Peaks",
    mechanic = "Cages & Keys",
    species = listOf("Penguin", "Polar Cub", "Arctic Fox"),
    skyTop = Color(0xFF8AB9E9),
    skyBottom = Color(0xFFEDF5FF),
    boardBase = Color(0xFFB3CDE4),
    tileCheckerA = Color(0xFFE2EFFF),
    tileCheckerB = Color(0xFFD1E4F7),
    accent = TactileSky
)

val World5CoralCoast = WorldTheme(
    id = 5,
    name = "Coral Coast",
    mechanic = "Portals",
    species = listOf("Turtle", "Otter", "Crab"),
    skyTop = Color(0xFF31C4D4),
    skyBottom = Color(0xFFD3FBFF),
    boardBase = Color(0xFF4DB9BC),
    tileCheckerA = Color(0xFF8AE1E3),
    tileCheckerB = Color(0xFF6DD4D6),
    accent = TactileCoral
)

val World6MoonlitForest = WorldTheme(
    id = 6,
    name = "Moonlit Forest",
    mechanic = "Sleepy Buddies",
    species = listOf("Owl", "Hedgehog", "Raccoon"),
    skyTop = Color(0xFF26204E),
    skyBottom = Color(0xFF635698),
    boardBase = Color(0xFF382F63),
    tileCheckerA = Color(0xFF534882),
    tileCheckerB = Color(0xFF453A71),
    accent = TactileGrape
)

val World7DesertOasis = WorldTheme(
    id = 7,
    name = "Desert Oasis",
    mechanic = "Mixed Mechanics",
    species = listOf("Camel", "Fennec Fox", "Meerkat"),
    skyTop = Color(0xFFFF9268),
    skyBottom = Color(0xFFFFE5D9),
    boardBase = Color(0xFFE89857),
    tileCheckerA = Color(0xFFFFB67A),
    tileCheckerB = Color(0xFFF2A464),
    accent = TactileMint
)

val World8SkyIslands = WorldTheme(
    id = 8,
    name = "Sky Islands",
    mechanic = "Mixed Finale",
    species = listOf("Red Panda", "Elephant Calf", "Peacock"),
    skyTop = Color(0xFF9076E8),
    skyBottom = Color(0xFFFFDAEC),
    boardBase = Color(0xFF8F7BC9),
    tileCheckerA = Color(0xFFB5A4E8),
    tileCheckerB = Color(0xFFA290DB),
    accent = TactileMango
)

val AllWorldThemes = listOf(
    World1SunnyMeadow,
    World2HappyFarm,
    World3JungleRuins,
    World4SnowyPeaks,
    World5CoralCoast,
    World6MoonlitForest,
    World7DesertOasis,
    World8SkyIslands
)

fun worldThemeFor(worldId: Int): WorldTheme = AllWorldThemes.getOrElse((worldId - 1).coerceAtLeast(0)) { World1SunnyMeadow }


