package com.kasif.arrowbuddies.core.buddies.model

/**
 * Procedural ear morphology used to construct distinct silhouettes for each species.
 */
enum class EarType {
    NONE,
    LONG_RABBIT,     // Bunny
    ROUND_BEAR,      // Polar Cub, Red Panda, Raccoon, Otter
    POINTY_FOX,      // Arctic Fox, Tiger Cub
    BIG_TRIANGLE,    // Fennec Fox
    FLOPPY_DOG,      // Puppy, Lamb, Camel
    CUP_MONKEY,      // Monkey, Meerkat
    HORN_CALF,       // Calf
    FEATHER_HORNS,   // Owl
    FAN_ELEPHANT     // Elephant Calf
}

/**
 * Procedural tail morphology rendered behind or beside the buddy body.
 */
enum class TailType {
    NONE,
    PUFF_BALL,       // Bunny, Lamb, Polar Cub
    WAG_DOG,         // Puppy, Calf
    CURLY_PIG,       // Piglet
    LONG_CURL,       // Monkey
    RINGED_BUSHY,    // Raccoon, Red Panda
    BUSHY_BRUSH,     // Arctic Fox, Fennec Fox
    TAPERED_OTTER,   // Otter, Meerkat
    FAN_PEACOCK,     // Peacock
    TINY_TUFT        // Camel, Elephant Calf
}

/**
 * Distinguishing anatomical feature for each species ensuring immediate silhouette recognition.
 */
enum class SpecialFeature {
    NONE,
    SNOUT_PIGLET,       // Rounded snout with dual nostrils
    BEAK_CHICK,         // Petite triangle beak & 3-tuft crest
    SNOUT_CALF,         // Wide dairy muzzle & cow eye spot
    TIGER_STRIPES,      // Cheek and crown tiger stripes
    BEAK_PARROT,        // Downward curved tropical parrot beak & crest
    BEAK_PENGUIN,       // Tuxedo cowl & orange beak
    TURTLE_SHELL,       // Domed scute shell & swim flippers
    CRAB_CLAWS,         // Raised dual front pincers & eyestalks
    BEAK_OWL,           // Eye spectacle rings & downward yellow beak
    HEDGEHOG_QUILLS,    // Radiating crown of rounded quills
    RACCOON_MASK,       // Horizontal bandit eye mask
    CAMEL_HUMP,         // Dorsal curved hump
    MEERKAT_EYE_RINGS,  // Dark sunglass eye rings
    RED_PANDA_MASK,     // White cheek markings & brow dots
    ELEPHANT_TRUNK,     // Upward curled front trunk
    PEACOCK_CREST       // 3-bobble celestial crest crown
}

