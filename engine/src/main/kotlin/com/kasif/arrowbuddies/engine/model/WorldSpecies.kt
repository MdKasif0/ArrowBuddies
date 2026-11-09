package com.kasif.arrowbuddies.engine.model

/**
 * Standard animal species roster per world (3 species per world across 8 worlds).
 */
object WorldSpecies {

    val WORLD_SPECIES: Map<Int, List<String>> = mapOf(
        1 to listOf("BUNNY", "CHICK", "PIGLET"),
        2 to listOf("PUPPY", "LAMB", "CALF"),
        3 to listOf("MONKEY", "TIGER_CUB", "PARROT"),
        4 to listOf("PENGUIN", "POLAR_CUB", "ARCTIC_FOX"),
        5 to listOf("TURTLE", "OTTER", "CRAB"),
        6 to listOf("OWL", "HEDGEHOG", "RACCOON"),
        7 to listOf("CAMEL", "FENNEC_FOX", "MEERKAT"),
        8 to listOf("RED_PANDA", "ELEPHANT_CALF", "PEACOCK")
    )

    fun forWorld(world: Int): List<String> {
        return WORLD_SPECIES[world] ?: listOf("BUNNY", "CHICK", "PIGLET")
    }
}
