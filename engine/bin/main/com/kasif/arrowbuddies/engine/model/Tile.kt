package com.kasif.arrowbuddies.engine.model

import kotlinx.serialization.Serializable

/**
 * Static or interactive terrain element occupying a cell on the game board.
 */
@Serializable
sealed interface Tile {
    /**
     * Clear, open cell through which buddies travel unobstructed.
     */
    @Serializable
    data object Empty : Tile

    /**
     * Static obstacle: blocks buddy travel. Hitting a rock triggers a bonk.
     */
    @Serializable
    data object Rock : Tile

    /**
     * Reserved (World 2): Mud terrain. A buddy entering mud stops on it as a valid move without losing a heart.
     */
    @Serializable
    data object Mud : Tile

    /**
     * Reserved (World 3): Turntable terrain. Rotates the buddy 90 degrees clockwise as it enters.
     */
    @Serializable
    data object Turntable : Tile

    /**
     * Reserved (World 5): Wormhole portal pair. Entering portal [pairId] transports the buddy to its twin.
     */
    @Serializable
    data class Portal(val pairId: Int) : Tile
}

