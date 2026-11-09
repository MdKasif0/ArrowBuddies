package com.kasif.arrowbuddies.engine.model

import kotlinx.serialization.Serializable

/**
 * Discrete grid coordinate on the game board.
 * (0, 0) is top-left cell.
 */
@Serializable
data class Pos(
    val x: Int,
    val y: Int
) {
    /**
     * Translates position by 1 cell in the specified [Direction].
     */
    operator fun plus(dir: Direction): Pos = Pos(x + dir.dx, y + dir.dy)

    /**
     * Translates position by [other] vector.
     */
    operator fun plus(other: Pos): Pos = Pos(x + other.x, y + other.y)

    /**
     * Vector subtraction.
     */
    operator fun minus(other: Pos): Pos = Pos(x - other.x, y - other.y)

    /**
     * Manhattan distance (L1 norm) to [other].
     */
    fun manhattanDistance(other: Pos): Int = kotlin.math.abs(x - other.x) + kotlin.math.abs(y - other.y)

    /**
     * True if [other] is orthogonally adjacent (Manhattan distance == 1).
     */
    fun isOrthogonalTo(other: Pos): Boolean {
        val dx = kotlin.math.abs(x - other.x)
        val dy = kotlin.math.abs(y - other.y)
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1)
    }

    override fun toString(): String = "($x, $y)"
}

