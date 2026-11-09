package com.kasif.arrowbuddies.engine.model

import kotlinx.serialization.Serializable

/**
 * An individual animal buddy on the puzzle board.
 * Occupies exactly 1 grid cell and faces in a specific [Direction].
 *
 * @param id Unique identifier on the board (e.g. "b1", "bunny_1").
 * @param species Animal species identifier (e.g. "BUNNY", "CHICK", "PIGLET").
 * @param pos Current grid cell position [Pos].
 * @param dir Active movement facing direction [Direction].
 * @param cageColor Reserved (World 4): If non-null, buddy is locked in a cage of this color.
 * @param keyColor Reserved (World 4): If non-null, buddy carries a key that unlocks matching cages upon rescue.
 * @param sleepy Reserved (World 6): If true, buddy is asleep and awakens when an adjacent neighbor departs.
 */
@Serializable
data class Buddy(
    val id: String,
    val species: String,
    val pos: Pos,
    val dir: Direction,
    val cageColor: String? = null,
    val keyColor: String? = null,
    val sleepy: Boolean = false
) {
    /**
     * True if the buddy is currently immobilized (caged or asleep).
     */
    val isImmobilized: Boolean
        get() = cageColor != null || sleepy
}
