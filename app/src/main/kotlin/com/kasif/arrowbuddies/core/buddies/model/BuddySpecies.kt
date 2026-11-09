package com.kasif.arrowbuddies.core.buddies.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.kasif.arrowbuddies.R

/**
 * All 24 animal buddy species across the 8 worlds of ArrowBuddies.
 * Each species defines its world, color palette, and procedural morphology.
 */
enum class BuddySpecies(
    val id: String,
    @StringRes val nameRes: Int,
    val worldIndex: Int,
    val bodyColor: Color,
    val bellyColor: Color,
    val accentColor: Color,
    val earType: EarType,
    val tailType: TailType,
    val specialFeature: SpecialFeature
) {
    // ----------------------------------------------------
    // World 1: Sunny Meadow (Basics & Rocks)
    // ----------------------------------------------------
    BUNNY(
        id = "bunny",
        nameRes = R.string.buddy_bunny,
        worldIndex = 1,
        bodyColor = Color(0xFFFFF7F0), // Soft warm cream
        bellyColor = Color(0xFFFFECE0),
        accentColor = Color(0xFFFF85A1), // Soft coral pink
        earType = EarType.LONG_RABBIT,
        tailType = TailType.PUFF_BALL,
        specialFeature = SpecialFeature.NONE
    ),
    CHICK(
        id = "chick",
        nameRes = R.string.buddy_chick,
        worldIndex = 1,
        bodyColor = Color(0xFFFFD54F), // Sunny yellow
        bellyColor = Color(0xFFFFF9C4),
        accentColor = Color(0xFFFF8F00), // Bright orange beak & feet
        earType = EarType.NONE,
        tailType = TailType.NONE,
        specialFeature = SpecialFeature.BEAK_CHICK
    ),
    PIGLET(
        id = "piglet",
        nameRes = R.string.buddy_piglet,
        worldIndex = 1,
        bodyColor = Color(0xFFFFB6C1), // Sweet baby pink
        bellyColor = Color(0xFFFFE0E6),
        accentColor = Color(0xFFFF6B8B), // Rose coral
        earType = EarType.POINTY_FOX,
        tailType = TailType.CURLY_PIG,
        specialFeature = SpecialFeature.SNOUT_PIGLET
    ),

    // ----------------------------------------------------
    // World 2: Happy Farm (Mud)
    // ----------------------------------------------------
    PUPPY(
        id = "puppy",
        nameRes = R.string.buddy_puppy,
        worldIndex = 2,
        bodyColor = Color(0xFFE0A96D), // Warm caramel tan
        bellyColor = Color(0xFFF9E7D0),
        accentColor = Color(0xFF7A4A28), // Deep cocoa ears/nose
        earType = EarType.FLOPPY_DOG,
        tailType = TailType.WAG_DOG,
        specialFeature = SpecialFeature.NONE
    ),
    LAMB(
        id = "lamb",
        nameRes = R.string.buddy_lamb,
        worldIndex = 2,
        bodyColor = Color(0xFFF7F4EB), // Wool white
        bellyColor = Color(0xFFFFFFFF),
        accentColor = Color(0xFF4A4E69), // Charcoal muzzle/ears
        earType = EarType.FLOPPY_DOG,
        tailType = TailType.PUFF_BALL,
        specialFeature = SpecialFeature.NONE
    ),
    CALF(
        id = "calf",
        nameRes = R.string.buddy_calf,
        worldIndex = 2,
        bodyColor = Color(0xFFF8F9FA), // Dairy white
        bellyColor = Color(0xFFEDF2F7),
        accentColor = Color(0xFF2B2D42), // Espresso cow spots
        earType = EarType.HORN_CALF,
        tailType = TailType.WAG_DOG,
        specialFeature = SpecialFeature.SNOUT_CALF
    ),

    // ----------------------------------------------------
    // World 3: Jungle Ruins (Turntables)
    // ----------------------------------------------------
    MONKEY(
        id = "monkey",
        nameRes = R.string.buddy_monkey,
        worldIndex = 3,
        bodyColor = Color(0xFF8D6E63), // Jungle brown
        bellyColor = Color(0xFFFFCCBC), // Peach face disc
        accentColor = Color(0xFF5D4037),
        earType = EarType.CUP_MONKEY,
        tailType = TailType.LONG_CURL,
        specialFeature = SpecialFeature.NONE
    ),
    TIGER_CUB(
        id = "tiger_cub",
        nameRes = R.string.buddy_tiger_cub,
        worldIndex = 3,
        bodyColor = Color(0xFFFF9F1C), // Vivid tiger amber
        bellyColor = Color(0xFFFFF3E0),
        accentColor = Color(0xFF261C14), // Dark tiger stripes
        earType = EarType.POINTY_FOX,
        tailType = TailType.RINGED_BUSHY,
        specialFeature = SpecialFeature.TIGER_STRIPES
    ),
    PARROT(
        id = "parrot",
        nameRes = R.string.buddy_parrot,
        worldIndex = 3,
        bodyColor = Color(0xFFFF4757), // Tropical scarlet
        bellyColor = Color(0xFFFFA502), // Sunny yellow
        accentColor = Color(0xFF2ED573), // Jungle green
        earType = EarType.NONE,
        tailType = TailType.NONE,
        specialFeature = SpecialFeature.BEAK_PARROT
    ),

    // ----------------------------------------------------
    // World 4: Snowy Peaks (Cages & Keys)
    // ----------------------------------------------------
    PENGUIN(
        id = "penguin",
        nameRes = R.string.buddy_penguin,
        worldIndex = 4,
        bodyColor = Color(0xFF2F3542), // Antarctic dark cowl
        bellyColor = Color(0xFFFFFFFF), // Crisp snow belly
        accentColor = Color(0xFFFF793F), // Tangerine beak & feet
        earType = EarType.NONE,
        tailType = TailType.NONE,
        specialFeature = SpecialFeature.BEAK_PENGUIN
    ),
    POLAR_CUB(
        id = "polar_cub",
        nameRes = R.string.buddy_polar_cub,
        worldIndex = 4,
        bodyColor = Color(0xFFF1F2F6), // Polar snow
        bellyColor = Color(0xFFDFE4EA),
        accentColor = Color(0xFF70A1FF), // Arctic blue accent
        earType = EarType.ROUND_BEAR,
        tailType = TailType.PUFF_BALL,
        specialFeature = SpecialFeature.NONE
    ),
    ARCTIC_FOX(
        id = "arctic_fox",
        nameRes = R.string.buddy_arctic_fox,
        worldIndex = 4,
        bodyColor = Color(0xFFF7F9FA), // Frost white
        bellyColor = Color(0xFFE2E8F0),
        accentColor = Color(0xFF57606F), // Slate ear tips
        earType = EarType.POINTY_FOX,
        tailType = TailType.BUSHY_BRUSH,
        specialFeature = SpecialFeature.NONE
    ),

    // ----------------------------------------------------
    // World 5: Coral Coast (Portals)
    // ----------------------------------------------------
    TURTLE(
        id = "turtle",
        nameRes = R.string.buddy_turtle,
        worldIndex = 5,
        bodyColor = Color(0xFF2ED573), // Seafoam green
        bellyColor = Color(0xFFECCC68), // Pale lime belly
        accentColor = Color(0xFF1E824C), // Deep emerald shell
        earType = EarType.NONE,
        tailType = TailType.NONE,
        specialFeature = SpecialFeature.TURTLE_SHELL
    ),
    OTTER(
        id = "otter",
        nameRes = R.string.buddy_otter,
        worldIndex = 5,
        bodyColor = Color(0xFF795548), // River otter brown
        bellyColor = Color(0xFFD7CCC8), // Cream throat/belly
        accentColor = Color(0xFFFFA000), // Honey golden accent
        earType = EarType.ROUND_BEAR,
        tailType = TailType.TAPERED_OTTER,
        specialFeature = SpecialFeature.NONE
    ),
    CRAB(
        id = "crab",
        nameRes = R.string.buddy_crab,
        worldIndex = 5,
        bodyColor = Color(0xFFFF5252), // Coral crimson
        bellyColor = Color(0xFFFF8A80),
        accentColor = Color(0xFFD32F2F), // Claws & eyestalks
        earType = EarType.NONE,
        tailType = TailType.NONE,
        specialFeature = SpecialFeature.CRAB_CLAWS
    ),

    // ----------------------------------------------------
    // World 6: Moonlit Forest (Sleepy Buddies)
    // ----------------------------------------------------
    OWL(
        id = "owl",
        nameRes = R.string.buddy_owl,
        worldIndex = 6,
        bodyColor = Color(0xFF574B90), // Twilight indigo
        bellyColor = Color(0xFFE8D7F1), // Soft lavender
        accentColor = Color(0xFFF5CD79), // Moonbeam yellow beak
        earType = EarType.FEATHER_HORNS,
        tailType = TailType.NONE,
        specialFeature = SpecialFeature.BEAK_OWL
    ),
    HEDGEHOG(
        id = "hedgehog",
        nameRes = R.string.buddy_hedgehog,
        worldIndex = 6,
        bodyColor = Color(0xFF5D4037), // Deep hedgehog brown
        bellyColor = Color(0xFFD7CCC8), // Soft tan snout & belly
        accentColor = Color(0xFF3E2723), // Quill tips
        earType = EarType.ROUND_BEAR,
        tailType = TailType.NONE,
        specialFeature = SpecialFeature.HEDGEHOG_QUILLS
    ),
    RACCOON(
        id = "raccoon",
        nameRes = R.string.buddy_raccoon,
        worldIndex = 6,
        bodyColor = Color(0xFF78909C), // Slate heather gray
        bellyColor = Color(0xFFECEFF1),
        accentColor = Color(0xFF263238), // Bandit mask black
        earType = EarType.ROUND_BEAR,
        tailType = TailType.RINGED_BUSHY,
        specialFeature = SpecialFeature.RACCOON_MASK
    ),

    // ----------------------------------------------------
    // World 7: Desert Oasis (Mixed)
    // ----------------------------------------------------
    CAMEL(
        id = "camel",
        nameRes = R.string.buddy_camel,
        worldIndex = 7,
        bodyColor = Color(0xFFE5A96A), // Golden dune sand
        bellyColor = Color(0xFFF7E7D2),
        accentColor = Color(0xFFB87333), // Desert terracotta
        earType = EarType.FLOPPY_DOG,
        tailType = TailType.TINY_TUFT,
        specialFeature = SpecialFeature.CAMEL_HUMP
    ),
    FENNEC_FOX(
        id = "fennec_fox",
        nameRes = R.string.buddy_fennec_fox,
        worldIndex = 7,
        bodyColor = Color(0xFFF6D8AC), // Desert peach cream
        bellyColor = Color(0xFFFFF6E6),
        accentColor = Color(0xFFE0876A), // Warm apricot ear interior
        earType = EarType.BIG_TRIANGLE,
        tailType = TailType.BUSHY_BRUSH,
        specialFeature = SpecialFeature.NONE
    ),
    MEERKAT(
        id = "meerkat",
        nameRes = R.string.buddy_meerkat,
        worldIndex = 7,
        bodyColor = Color(0xFFC49E72), // Kalahari tan
        bellyColor = Color(0xFFF0E2CD),
        accentColor = Color(0xFF4A3728), // Dark eye rings & ear tips
        earType = EarType.CUP_MONKEY,
        tailType = TailType.TAPERED_OTTER,
        specialFeature = SpecialFeature.MEERKAT_EYE_RINGS
    ),

    // ----------------------------------------------------
    // World 8: Sky Islands (Mixed Finale)
    // ----------------------------------------------------
    RED_PANDA(
        id = "red_panda",
        nameRes = R.string.buddy_red_panda,
        worldIndex = 8,
        bodyColor = Color(0xFFD35400), // Cinnamon rust orange
        bellyColor = Color(0xFF2C1B18), // Dark espresso chest
        accentColor = Color(0xFFFFFFFF), // White cheek marks & brow dots
        earType = EarType.ROUND_BEAR,
        tailType = TailType.RINGED_BUSHY,
        specialFeature = SpecialFeature.RED_PANDA_MASK
    ),
    ELEPHANT_CALF(
        id = "elephant_calf",
        nameRes = R.string.buddy_elephant_calf,
        worldIndex = 8,
        bodyColor = Color(0xFF8395A7), // Soft periwinkle slate
        bellyColor = Color(0xFFC8D6E5),
        accentColor = Color(0xFF54A0FF), // Sky blue water accent
        earType = EarType.FAN_ELEPHANT,
        tailType = TailType.TINY_TUFT,
        specialFeature = SpecialFeature.ELEPHANT_TRUNK
    ),
    PEACOCK(
        id = "peacock",
        nameRes = R.string.buddy_peacock,
        worldIndex = 8,
        bodyColor = Color(0xFF00A8FF), // Royal celestial teal
        bellyColor = Color(0xFF00CEC9), // Emerald cyan
        accentColor = Color(0xFFFBC531), // Golden eye spot
        earType = EarType.NONE,
        tailType = TailType.FAN_PEACOCK,
        specialFeature = SpecialFeature.PEACOCK_CREST
    );

    companion object {
        fun byWorld(world: Int): List<BuddySpecies> = entries.filter { it.worldIndex == world }
    }
}
