package com.kasif.arrowbuddies.engine.generator

/**
 * Parameters guiding procedural puzzle generation in ArrowBuddies.
 *
 * @param seed Deterministic seed for [kotlin.random.Random].
 * @param cols Grid column count ($\le 7$).
 * @param rows Grid row count ($\le 9$).
 * @param buddyCount Target count of buddies to place.
 * @param allowedMechanics Set of mechanics permitted ("rock", "mud", "turntable", "cage", "portal", "sleepy").
 * @param obstacleBudget Maximum count of static/terrain obstacles to place.
 * @param targetScoreRange Desired difficulty score range [min..max] clamped between 0 and 100.
 * @param speciesPool List of animal species names available in this world.
 * @param world World index (1 to 8).
 * @param index Level index within world (1 to 30).
 * @param isBigRescue True for levels 15 and 30 featuring a larger board and one Golden Buddy.
 */
data class GenParams(
    val seed: Long,
    val cols: Int,
    val rows: Int,
    val buddyCount: Int,
    val allowedMechanics: Set<String>,
    val obstacleBudget: Int,
    val targetScoreRange: ClosedFloatingPointRange<Double>,
    val speciesPool: List<String>,
    val world: Int = 1,
    val index: Int = 4,
    val isBigRescue: Boolean = false
)
