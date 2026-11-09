package com.kasif.arrowbuddies.engine.model

import kotlinx.serialization.Serializable

/**
 * Fundamental movement and orientation direction for game elements (Buddies, Portals, Lasers).
 * Grid coordinate system: (0, 0) is top-left.
 * UP is -Y, DOWN is +Y, LEFT is -X, RIGHT is +X.
 */
@Serializable
enum class Direction(
    val dx: Int,
    val dy: Int,
    val angleDegrees: Float
) {
    UP(0, -1, 0f),
    RIGHT(1, 0, 90f),
    DOWN(0, 1, 180f),
    LEFT(-1, 0, 270f);

    /**
     * 90-degree clockwise rotation.
     */
    fun rotateClockwise(): Direction = when (this) {
        UP -> RIGHT
        RIGHT -> DOWN
        DOWN -> LEFT
        LEFT -> UP
    }

    /**
     * Alias for [rotateClockwise].
     */
    fun turnClockwise90(): Direction = rotateClockwise()

    /**
     * 90-degree counter-clockwise rotation.
     */
    fun rotateCounterClockwise(): Direction = when (this) {
        UP -> LEFT
        LEFT -> DOWN
        DOWN -> RIGHT
        RIGHT -> UP
    }

    /**
     * Alias for [rotateCounterClockwise].
     */
    fun turnCounterClockwise90(): Direction = rotateCounterClockwise()

    /**
     * Opposite direction (180-degree reversal).
     */
    fun opposite(): Direction = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }
}
