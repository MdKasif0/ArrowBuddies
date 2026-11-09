package com.kasif.arrowbuddies.engine.model

import kotlinx.serialization.Serializable

/**
 * Immutable definition of an ArrowBuddies puzzle level.
 *
 * @param id Unique identifier (e.g. "w1-l01").
 * @param cols Grid column count ($\le 7$).
 * @param rows Grid row count ($\le 9$).
 * @param tiles Map of non-empty terrain tiles indexed by [Pos].
 * @param buddies Initial collection of buddies placed on the board.
 * @param world World index (1 to 8).
 * @param index 1-based level index within the world (1 to 30).
 */
@Serializable
data class LevelDef(
    val id: String,
    val cols: Int,
    val rows: Int,
    val tiles: Map<Pos, Tile>,
    val buddies: List<Buddy>,
    val world: Int,
    val index: Int
) {
    /**
     * Checks if [pos] falls within the grid boundaries.
     */
    fun isInside(pos: Pos): Boolean = pos.x in 0 until cols && pos.y in 0 until rows

    /**
     * Returns the [Tile] at [pos], defaulting to [Tile.Empty] if unspecified.
     */
    fun tileAt(pos: Pos): Tile = tiles[pos] ?: Tile.Empty

    /**
     * Checks if [pos] is on an outer edge of the grid.
     */
    fun isEdge(pos: Pos): Boolean {
        if (!isInside(pos)) return false
        return pos.x == 0 || pos.x == cols - 1 || pos.y == 0 || pos.y == rows - 1
    }

    /**
     * Returns positions of all portal tiles matching [pairId].
     */
    fun portalPositions(pairId: Int): List<Pos> {
        return tiles.filter { it.value is Tile.Portal && (it.value as Tile.Portal).pairId == pairId }.keys.toList()
    }
}
