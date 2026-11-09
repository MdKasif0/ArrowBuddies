package com.kasif.arrowbuddies.engine.model

/**
 * Fundamental movement and orientation direction for game elements (Buddies, Portals, Lasers).
 * Grid coordinate system: (0, 0) is top-left.
 * UP is -Y, DOWN is +Y, LEFT is -X, RIGHT is +X.
 */
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
     * Clockwise 90-degree turn (e.g. used by Turntable obstacle mechanic).
     */
    fun turnClockwise90(): Direction = when (this) {
        UP -> RIGHT
        RIGHT -> DOWN
        DOWN -> LEFT
        LEFT -> UP
    }

    /**
     * Counter-clockwise 90-degree turn.
     */
    fun turnCounterClockwise90(): Direction = when (this) {
        UP -> LEFT
        LEFT -> DOWN
        DOWN -> RIGHT
        RIGHT -> UP
    }

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

